# frozen_string_literal: true

require "fileutils"
require "json"
require "securerandom"
require "time"

module Dora
  # A private, transport-neutral ingress queue. The event file is the source of
  # truth: created content is immutable and every lifecycle update is appended.
  class Handoff
    SCHEMA_VERSION = 1
    STATUSES = %w[READY CLAIMED COMPLETED BLOCKED CANCELLED SUPERSEDED].freeze
    TERMINAL = %w[COMPLETED BLOCKED CANCELLED SUPERSEDED].freeze
    PROJECT_ID = /\A[a-z][a-z0-9-]*\z/.freeze
    HANDOFF_ID = /\Ahandoff-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\z/.freeze
    CLIENT_REQUEST_ID = /\A[a-zA-Z0-9][a-zA-Z0-9._:-]{0,127}\z/.freeze
    MAX_TITLE = 200
    MAX_OBJECTIVE = 8_000
    MAX_ITEM = 1_000
    MAX_LIST = 20
    MAX_FEEDBACK_HISTORY = 20
    PHASES = %w[START DISCOVERY PLANNING IMPLEMENTING VERIFYING].freeze
    VERIFICATION_STATES = %w[NOT_STARTED IMPLEMENTED_UNVERIFIED VERIFICATION_IN_PROGRESS VERIFICATION_FAILED VERIFIED].freeze
    FEEDBACK_PHASE_TRANSITIONS = {
      nil => PHASES,
      "START" => %w[START DISCOVERY PLANNING IMPLEMENTING VERIFYING],
      "DISCOVERY" => %w[DISCOVERY PLANNING IMPLEMENTING VERIFYING],
      "PLANNING" => %w[PLANNING IMPLEMENTING VERIFYING],
      "IMPLEMENTING" => %w[IMPLEMENTING VERIFYING],
      "VERIFYING" => %w[VERIFYING]
    }.freeze
    VERIFICATION_STATE_ORDER = VERIFICATION_STATES.each_with_index.to_h.freeze
    PROGRESS_PHASES = {
      "START" => {"phase" => "START", "label" => "Start", "summary" => "Handoff has been claimed and is starting."},
      "DISCOVERY" => {"phase" => "ANALYSIS", "label" => "Analysis", "summary" => "Handoff analysis is in progress."},
      "PLANNING" => {"phase" => "ANALYSIS", "label" => "Analysis", "summary" => "Handoff analysis is in progress."},
      "IMPLEMENTING" => {"phase" => "IMPLEMENTATION", "label" => "Implementation", "summary" => "Handoff implementation is in progress."},
      "VERIFYING" => {"phase" => "VERIFICATION", "label" => "Verification", "summary" => "Handoff verification is in progress."}
    }.freeze
    RUNNER_FAILURE_CODES = %w[keychain_unavailable runtime_preflight_failed launcher_dependency_unavailable launcher_start_failed launcher_preflight_failed codex_execution_failed launcher_exited_nonzero completion_missing].freeze

    def self.default_state_root
      File.expand_path(ENV.fetch("DORA_BRIDGE_STATE_ROOT", File.join(ENV.fetch("XDG_STATE_HOME", File.join(Dir.home, ".local", "share")), "dora-bridge")))
    end

    def initialize(state_root: self.class.default_state_root, now: -> { Time.now.utc })
      @state_root = File.expand_path(state_root)
      @now = now
      ensure_root!
    end

    def create!(project:, title:, objective:, acceptance_criteria:, constraints:, references:, created_by:, client_request_id:, supersedes: nil, follows_up: nil, brief: nil)
      content = created_content(project: project, title: title, objective: objective, acceptance_criteria: acceptance_criteria, constraints: constraints, references: references, created_by: created_by, client_request_id: client_request_id, supersedes: supersedes, follows_up: follows_up, brief: brief)
      with_lock do
        records = records_unlocked
        existing = records.values.find { |record| record.fetch("client_request_id") == content.fetch("client_request_id") }
        if existing
          fail!("handoff idempotency key conflicts with different content") unless immutable_content(existing) == immutable_content(content)

          return read_model(existing)
        end
        validate_lineage!(records, content)

        event = {"kind" => "created", "at" => @now.call.utc.iso8601, "handoff" => content.merge("id" => new_id, "created_at" => @now.call.utc.iso8601, "status" => "READY")}
        append_event_unlocked!(event)
        read_model(records_unlocked.fetch(event.fetch("handoff").fetch("id")))
      end
    end

    def list(project: nil)
      validate_project!(project) if project
      with_lock { records_unlocked.values.select { |record| !project || record.fetch("project") == project }.sort_by { |record| [record.fetch("created_at"), record.fetch("id")] }.map { |record| read_model(record) } }
    end

    def get!(id:, project: nil)
      validate_id!(id)
      validate_project!(project) if project
      with_lock do
        record = records_unlocked[id]
        fail!("handoff is not found") unless record && (!project || record.fetch("project") == project)

        read_model(record)
      end
    end

    # Deliberately smaller than the normal handoff read model. This is the only
    # projection intended to let an external reader understand lifecycle outcome
    # without receiving immutable handoff content or append-only event details.
    def lifecycle_readback!(id:, project:)
      validate_id!(id)
      validate_project!(project)
      with_lock do
        record = records_unlocked[id]
        fail!("handoff is not found") unless record && record.fetch("project") == project

        lifecycle_readback(record)
      end
    end

    # This preserves the existing V2.2 status fields while adding the smaller V3.2
    # safe progress projection. The progress object contains no feedback prose.
    def status_readback!(id:, project:)
      validate_id!(id)
      validate_project!(project)
      with_lock do
        record = records_unlocked[id]
        fail!("handoff is not found") unless record && record.fetch("project") == project

        read_model(record).slice("id", "project", "status", "collaboration", "delivery", "completion", "blocked", "cancelled", "superseded_by", "lifecycle").merge("progress" => progress_readback(record))
      end
    end

    def next_ready!(project:)
      validate_project!(project)
      with_lock do
        candidates = records_unlocked.values.select { |record| record.fetch("project") == project && record.fetch("status") == "READY" }.sort_by { |record| [record.fetch("created_at"), record.fetch("id")] }
        candidates.empty? ? nil : read_model(candidates.first)
      end
    end

    def claim!(id:, project:, claimed_by:)
      transition!(id: id, project: project, action: "claim", actor: validate_actor!(claimed_by), allowed: ["READY"], status: "CLAIMED", data: {"claimed_by" => claimed_by})
    end

    def link_delivery!(id:, project:, master_plan:, work_plan: nil)
      delivery = {"master_plan" => validate_reference!(master_plan)}
      delivery["work_plan"] = validate_reference!(work_plan) if work_plan
      transition!(id: id, project: project, action: "link_delivery", actor: "codex", allowed: ["CLAIMED"], status: nil, data: {"delivery" => delivery})
    end

    def block!(id:, project:, reason:)
      transition!(id: id, project: project, action: "block", actor: "codex", allowed: ["READY", "CLAIMED"], status: "BLOCKED", data: {"blocked" => {"reason" => validate_text!(reason, MAX_ITEM, "blocked reason")}})
    end

    def block_runner_failure!(id:, project:, failure:)
      transition!(id: id, project: project, action: "block_runner_failure", actor: "codex", allowed: ["CLAIMED"], status: "BLOCKED", data: {"blocked" => {"reason" => "Local Codex launch failed.", "runner_failure" => validate_runner_failure!(failure)}})
    end

    def feedback!(id:, project:, feedback:)
      validate_id!(id); validate_project!(project)
      with_lock do
        record = records_unlocked[id]
        fail!("handoff is not found") unless record && record.fetch("project") == project
        fail!("handoff lifecycle transition is invalid") unless record.fetch("status") == "CLAIMED"
        fail!("handoff feedback history is full") if Array(record["feedback_history"]).length >= MAX_FEEDBACK_HISTORY
        fail!("handoff feedback phase and verification state are invalid") if feedback.is_a?(Hash) && feedback["verification_state"] == "VERIFIED"
        collaboration = validate_feedback!(feedback, current: record["collaboration"])
        append_event_unlocked!({"kind" => "lifecycle", "id" => id, "action" => "feedback", "actor" => "codex", "at" => @now.call.utc.iso8601, "status" => nil, "data" => {"collaboration" => collaboration}})
        read_model(records_unlocked.fetch(id))
      end
    end

    def block_owner_decision!(id:, project:, owner_decision:)
      decision = validate_owner_decision!(owner_decision)
      transition!(id: id, project: project, action: "block_owner_decision", actor: "codex", allowed: ["CLAIMED"], status: "BLOCKED", data: {"blocked" => {"reason" => "Owner decision required", "owner_decision" => decision}})
    end

    def cancel!(id:, project:, reason:)
      transition!(id: id, project: project, action: "cancel", actor: "owner", allowed: ["READY", "CLAIMED"], status: "CANCELLED", data: {"cancelled" => {"reason" => validate_text!(reason, MAX_ITEM, "cancel reason")}})
    end

    def supersede!(id:, project:, superseded_by:)
      transition!(id: id, project: project, action: "supersede", actor: "owner", allowed: ["READY", "CLAIMED"], status: "SUPERSEDED", data: {"superseded_by" => validate_id!(superseded_by)})
    end

    def complete!(id:, project:, verification_references:, completion_result: nil)
      references = validate_references!(verification_references, "verification references")
      completion = {"verification_references" => references}
      completion["result"] = validate_completion_result!(completion_result) if completion_result
      transition!(id: id, project: project, action: "complete", actor: "codex", allowed: ["CLAIMED"], status: "COMPLETED", data: {"completion" => completion}, require_delivery: true)
    end

    private

    def lifecycle_readback(record)
      verification_state = record.dig("collaboration", "verification_state") || "NOT_STARTED"
      outcome_category = if record.fetch("status") == "COMPLETED"
                           "COMPLETED"
                         elsif verification_state == "VERIFICATION_FAILED"
                           "VERIFICATION_FAILED"
                         else
                           record.fetch("status")
                         end
      {
        "handoff_id" => record.fetch("id"),
        "status" => record.fetch("status"),
        "outcome_category" => outcome_category,
        "verification" => {
          "state" => verification_state,
          "evidence_references" => Array(record.dig("completion", "verification_references"))
        },
        "progress" => progress_readback(record),
        "summary" => lifecycle_summary(outcome_category)
      }.freeze
    end

    def progress_readback(record)
      status = record.fetch("status")
      verification_state = record.dig("collaboration", "verification_state") || "NOT_STARTED"
      projection = if status == "COMPLETED"
                     {"phase" => "COMPLETED", "label" => "Completed", "summary" => "Handoff completed with Dora verification evidence."}
                   elsif status == "BLOCKED"
                     {"phase" => "BLOCKED", "label" => "Blocked", "summary" => "Handoff is blocked and requires owner review."}
                   elsif verification_state == "VERIFICATION_FAILED"
                     {"phase" => "VERIFICATION", "label" => "Verification", "summary" => "Verification did not pass; the handoff remains claimed for owner recovery."}
                   elsif record.dig("collaboration", "phase")
                     PROGRESS_PHASES.fetch(record.dig("collaboration", "phase"))
                   elsif status == "CLAIMED"
                     {"phase" => "INCOMPLETE", "label" => "In progress", "summary" => "Handoff is claimed; no validated progress phase has been recorded."}
                   else
                     {"phase" => "INCOMPLETE", "label" => "Waiting", "summary" => "Handoff is ready for the owner-local workflow."}
                   end
      projection.merge("material_change_token" => [status, projection.fetch("phase"), verification_state].join(":"))
    end

    def lifecycle_summary(outcome_category)
      {
        "READY" => "Handoff is ready for the owner-local workflow.",
        "CLAIMED" => "Handoff is claimed and lifecycle work is in progress.",
        "COMPLETED" => "Handoff completed with Dora verification evidence.",
        "BLOCKED" => "Handoff is blocked and requires owner review.",
        "VERIFICATION_FAILED" => "Verification did not pass; the handoff remains claimed for owner recovery.",
        "CANCELLED" => "Handoff was cancelled by the owner.",
        "SUPERSEDED" => "Handoff was superseded by a follow-up."
      }.fetch(outcome_category)
    end

    def created_content(project:, title:, objective:, acceptance_criteria:, constraints:, references:, created_by:, client_request_id:, supersedes: nil, follows_up: nil, brief: nil)
      result = {
        "schema_version" => SCHEMA_VERSION,
        "project" => validate_project!(project),
        "title" => validate_text!(title, MAX_TITLE, "title"),
        "objective" => validate_text!(objective, MAX_OBJECTIVE, "objective"),
        "acceptance_criteria" => validate_text_list!(acceptance_criteria, "acceptance criteria"),
        "constraints" => validate_text_list!(constraints, "constraints"),
        "references" => validate_references!(references, "references"),
        "created_by" => validate_actor!(created_by),
        "client_request_id" => validate_client_request_id!(client_request_id)
      }
      result["supersedes"] = validate_id!(supersedes) if supersedes
      result["follows_up"] = validate_id!(follows_up) if follows_up
      result["brief"] = validate_brief!(brief) if brief
      result
    end

    def immutable_content(record)
      record.slice("schema_version", "project", "title", "objective", "acceptance_criteria", "constraints", "references", "created_by", "client_request_id", "supersedes", "follows_up", "brief").compact
    end

    def transition!(id:, project:, action:, actor:, allowed:, status:, data:, require_delivery: false, feedback_limit: false)
      validate_id!(id); validate_project!(project)
      with_lock do
        record = records_unlocked[id]
        fail!("handoff is not found") unless record && record.fetch("project") == project
        fail!("handoff lifecycle transition is invalid") unless allowed.include?(record.fetch("status"))
        fail!("handoff must link a Dora delivery before completion") if require_delivery && !record["delivery"]
        fail!("handoff feedback history is full") if feedback_limit && Array(record["feedback_history"]).length >= MAX_FEEDBACK_HISTORY
        event = {"kind" => "lifecycle", "id" => id, "action" => action, "actor" => actor, "at" => @now.call.utc.iso8601, "status" => status, "data" => data}
        append_event_unlocked!(event)
        read_model(records_unlocked.fetch(id))
      end
    end

    def read_model(record)
      result = record.slice("schema_version", "id", "project", "title", "objective", "acceptance_criteria", "constraints", "references", "created_at", "created_by", "client_request_id", "supersedes", "follows_up", "brief", "status", "claimed_by", "delivery", "completion", "blocked", "cancelled", "superseded_by", "collaboration")
      result["feedback_history"] = Array(record["feedback_history"]).dup if record["feedback_history"]
      result["lifecycle"] = Array(record["events"]).map { |event| event.slice("action", "actor", "at", "status") }
      result.freeze
    end

    def records_unlocked
      events = if File.exist?(events_path)
                 assert_private_file!(events_path)
                 File.readlines(events_path, chomp: true).reject(&:empty?).map { |line| JSON.parse(line) }
               else
                 []
               end
      reduce_events(events)
    rescue JSON::ParserError
      fail!("handoff store is invalid")
    end

    def reduce_events(events)
      events.each_with_object({}) do |event, records|
        fail!("handoff store is invalid") unless event.is_a?(Hash)
        if event["kind"] == "created"
          handoff = event["handoff"]
          validate_created_event!(handoff)
          fail!("handoff store is invalid") if records.key?(handoff.fetch("id"))
          records[handoff.fetch("id")] = handoff.merge("events" => [{"action" => "created", "actor" => handoff.fetch("created_by"), "at" => handoff.fetch("created_at"), "status" => "READY"}])
        elsif event["kind"] == "lifecycle"
          record = records[event["id"]]
          fail!("handoff store is invalid") unless record
          apply_lifecycle_event!(record, event)
        else
          fail!("handoff store is invalid")
        end
      end
    end

    def apply_lifecycle_event!(record, event)
      fail!("handoff store is invalid") unless event["action"].is_a?(String) && event["actor"].is_a?(String) && parse_time?(event["at"])
      status = event["status"]
      fail!("handoff store is invalid") unless status.nil? || STATUSES.include?(status)
      data = event["data"]
      fail!("handoff store is invalid") unless data.is_a?(Hash)
        validate_v2_2_lifecycle_event!(record, event)
      record["status"] = status if status
      record.merge!(data)
      if event["action"] == "feedback"
        record["feedback_history"] ||= []
        record.fetch("feedback_history") << data.fetch("collaboration")
      elsif event["action"] == "complete"
        current = record["collaboration"] || {"phase" => "VERIFYING", "milestone" => "Dora verification recorded", "verification_state" => "NOT_STARTED", "deviations" => [], "residual_risks" => []}
        record["collaboration"] = current.merge("phase" => "VERIFYING", "verification_state" => "VERIFIED")
      end
      record.fetch("events") << event.slice("action", "actor", "at", "status").compact
    end

    def append_event_unlocked!(event)
      assert_private_file!(events_path) if File.exist?(events_path)
      File.open(events_path, File::WRONLY | File::CREAT | File::APPEND, 0o600) do |file|
        File.chmod(0o600, events_path)
        file.write(JSON.generate(event) + "\n")
        file.flush
        file.fsync
      end
    end

    def with_lock
      File.open(lock_path, File::RDWR | File::CREAT, 0o600) do |file|
        File.chmod(0o600, lock_path)
        file.flock(File::LOCK_EX)
        yield
      ensure
        file.flock(File::LOCK_UN) if file
      end
    end

    def ensure_root!
      if File.exist?(@state_root) || File.symlink?(@state_root)
        fail!("handoff state root must not be a symlink") if File.symlink?(@state_root)
      else
        FileUtils.mkdir_p(@state_root, mode: 0o700)
      end
      fail!("handoff state root is invalid") unless File.directory?(@state_root)
      File.chmod(0o700, @state_root) if File.stat(@state_root).mode & 0o077 != 0
      assert_private_directory!(@state_root)
    end

    def assert_private_directory!(path)
      stat = File.lstat(path)
      fail!("handoff state root is invalid") if stat.symlink? || !stat.directory? || stat.uid != Process.uid || stat.mode & 0o077 != 0
    end

    def assert_private_file!(path)
      stat = File.lstat(path)
      fail!("handoff store is invalid") if stat.symlink? || !stat.file? || stat.uid != Process.uid || stat.mode & 0o077 != 0
    end

    def events_path
      File.join(@state_root, "handoff-events.jsonl")
    end

    def lock_path
      File.join(@state_root, ".handoff.lock")
    end

    def new_id
      "handoff-#{SecureRandom.uuid}"
    end

    def validate_created_event!(record)
      fail!("handoff store is invalid") unless record.is_a?(Hash) && record["schema_version"].to_i == SCHEMA_VERSION && HANDOFF_ID.match?(record["id"].to_s) && STATUSES.include?(record["status"]) && parse_time?(record["created_at"])
      created_content(**record.slice("project", "title", "objective", "acceptance_criteria", "constraints", "references", "created_by", "client_request_id", "supersedes", "follows_up", "brief").transform_keys(&:to_sym))
    end

    def validate_project!(value)
      fail!("handoff project is invalid") unless value.is_a?(String) && PROJECT_ID.match?(value)
      value
    end

    def validate_id!(value)
      fail!("handoff id is invalid") unless value.is_a?(String) && HANDOFF_ID.match?(value)
      value
    end

    def validate_client_request_id!(value)
      fail!("handoff client_request_id is invalid") unless value.is_a?(String) && CLIENT_REQUEST_ID.match?(value)
      value
    end

    def validate_actor!(value)
      fail!("handoff actor is invalid") unless value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]{0,63}\z/)
      value
    end

    def validate_text!(value, maximum, label)
      fail!("handoff #{label} is invalid") unless value.is_a?(String) && !value.strip.empty? && value.length <= maximum
      value
    end

    def validate_text_list!(value, label)
      fail!("handoff #{label} is invalid") unless value.is_a?(Array) && value.length <= MAX_LIST
      value.map { |item| validate_text!(item, MAX_ITEM, label) }
    end

    def validate_references!(value, label)
      fail!("handoff #{label} is invalid") unless value.is_a?(Array) && value.length <= MAX_LIST
      value.map { |item| validate_reference!(item) }
    end

    def validate_brief!(value)
      fail!("handoff brief is invalid") unless value.is_a?(Hash) && value.keys.sort == %w[context expected_result locked_decisions non_goals open_decisions request_mode stop_conditions verification].sort
      mode = value["request_mode"]
      fail!("handoff brief request mode is invalid") unless %w[implementation owner_decision_required].include?(mode)
      result = {"request_mode" => mode, "expected_result" => validate_text!(value["expected_result"], MAX_ITEM, "brief expected result")}
      %w[locked_decisions non_goals context stop_conditions verification open_decisions].each { |key| result[key] = validate_text_list!(value[key], "brief #{key}") }
      fail!("handoff brief implementation requires verification") if mode == "implementation" && result["verification"].empty?
      fail!("handoff brief unresolved decisions are invalid") if (mode == "implementation") != result["open_decisions"].empty?
      result.freeze
    end

    def validate_v2_2_lifecycle_event!(record, event)
      case event.fetch("action")
      when "feedback"
        fail!("handoff store is invalid") unless event["status"].nil? && event["actor"] == "codex" && event.fetch("data").keys == ["collaboration"]
        validate_feedback!(event.dig("data", "collaboration"), current: record["collaboration"])
      when "block_owner_decision"
        blocked = event.dig("data", "blocked")
        fail!("handoff store is invalid") unless event["status"] == "BLOCKED" && event["actor"] == "codex" && blocked.is_a?(Hash) && blocked["reason"] == "Owner decision required" && blocked.keys.sort == %w[owner_decision reason]
        validate_owner_decision!(blocked["owner_decision"])
      when "block_runner_failure"
        blocked = event.dig("data", "blocked")
        fail!("handoff store is invalid") unless event["status"] == "BLOCKED" && event["actor"] == "codex" && blocked.is_a?(Hash) && blocked["reason"] == "Local Codex launch failed." && blocked.keys.sort == %w[reason runner_failure]
        validate_runner_failure!(blocked["runner_failure"])
      when "complete"
        completion = event.dig("data", "completion")
        fail!("handoff store is invalid") unless event["status"] == "COMPLETED" && completion.is_a?(Hash) && completion["verification_references"].is_a?(Array)
        validate_references!(completion.fetch("verification_references"), "verification references")
        validate_completion_result!(completion["result"]) if completion.key?("result")
      end
    end

    def validate_feedback!(value, current: nil)
      fail!("handoff feedback is invalid") unless value.is_a?(Hash) && value.keys.sort == %w[deviations finding milestone phase progress residual_risks verification_state].sort
      phase = value["phase"]
      verification_state = value["verification_state"]
      fail!("handoff feedback phase is invalid") unless PHASES.include?(phase)
      fail!("handoff feedback verification state is invalid") unless VERIFICATION_STATES.include?(verification_state)
      validate_feedback_pair!(phase, verification_state)
      validate_feedback_transition!(current, phase, verification_state) if current
      result = {"phase" => phase, "milestone" => validate_compact_text!(value["milestone"], "feedback milestone"), "verification_state" => verification_state, "deviations" => validate_compact_list!(value["deviations"], "feedback deviations"), "residual_risks" => validate_compact_list!(value["residual_risks"], "feedback residual risks")}
      %w[progress finding].each { |key| result[key] = validate_compact_text!(value[key], "feedback #{key}") if value[key] }
      result.freeze
    end

    def validate_feedback_pair!(phase, verification_state)
      # V2.2 already persisted each allowlisted phase/state independently. Preserve
      # those historical records while V3.2 adds ordered phase transitions and
      # non-regressing verification state. New VERIFIED feedback is rejected at the
      # mutation boundary because VERIFIED is evidence-owned by completion.
    end

    def validate_feedback_transition!(current, phase, verification_state)
      current_phase = current["phase"]
      current_verification = current["verification_state"]
      fail!("handoff feedback transition is invalid") unless FEEDBACK_PHASE_TRANSITIONS.fetch(current_phase).include?(phase)
      fail!("handoff feedback verification transition is invalid") unless VERIFICATION_STATE_ORDER.fetch(verification_state) >= VERIFICATION_STATE_ORDER.fetch(current_verification)
      fail!("handoff feedback transition is not material") if current_phase == phase && current_verification == verification_state
    end

    def validate_owner_decision!(value)
      fail!("handoff owner decision is invalid") unless value.is_a?(Hash) && value.keys.sort == %w[blocked_work may_continue options question recommendation why].sort
      result = {"question" => validate_compact_text!(value["question"], "owner decision question"), "why" => validate_compact_text!(value["why"], "owner decision why"), "options" => validate_compact_list!(value["options"], "owner decision options"), "blocked_work" => validate_compact_list!(value["blocked_work"], "owner decision blocked work"), "may_continue" => validate_compact_list!(value["may_continue"], "owner decision continuing work")}
      result["recommendation"] = validate_compact_text!(value["recommendation"], "owner decision recommendation") if value["recommendation"]
      result.freeze
    end

    def validate_runner_failure!(value)
      fail!("handoff runner failure is invalid") unless value.is_a?(Hash) && value.keys.sort == %w[code exit_code recovery_hint]
      code = value["code"]
      exit_code = value["exit_code"]
      fail!("handoff runner failure code is invalid") unless RUNNER_FAILURE_CODES.include?(code)
      fail!("handoff runner failure exit code is invalid") unless exit_code.nil? || (exit_code.is_a?(Integer) && exit_code.between?(0, 255))

      {"code" => code, "exit_code" => exit_code, "recovery_hint" => validate_compact_text!(value["recovery_hint"], "runner failure recovery hint")}.freeze
    end

    def validate_completion_result!(value)
      fail!("handoff completion result is invalid") unless value.is_a?(Hash) && value.keys.sort == %w[acceptance_results decisions deviations follow_up residual_risks work_performed].sort
      follow_up = value["follow_up"]
      fail!("handoff completion follow-up is invalid") unless follow_up.is_a?(Hash) && follow_up.keys.sort == %w[needed reason] && [true, false].include?(follow_up["needed"]) && (follow_up["reason"].nil? || follow_up["reason"].is_a?(String))
      fail!("handoff completion follow-up reason is invalid") if follow_up["needed"] != !follow_up["reason"].nil?
      {"work_performed" => validate_compact_list!(value["work_performed"], "completion work performed"), "decisions" => validate_compact_list!(value["decisions"], "completion decisions"), "acceptance_results" => validate_compact_list!(value["acceptance_results"], "completion acceptance results"), "deviations" => validate_compact_list!(value["deviations"], "completion deviations"), "residual_risks" => validate_compact_list!(value["residual_risks"], "completion residual risks"), "follow_up" => {"needed" => follow_up["needed"], "reason" => follow_up["reason"] && validate_compact_text!(follow_up["reason"], "completion follow-up reason")}}.freeze
    end

    def validate_compact_list!(value, label)
      fail!("handoff #{label} is invalid") unless value.is_a?(Array) && value.length <= MAX_LIST
      value.map { |item| validate_compact_text!(item, label) }
    end

    def validate_compact_text!(value, label)
      fail!("handoff #{label} is invalid") unless value.is_a?(String) && !value.strip.empty? && value.length <= MAX_ITEM && !value.include?("\n") && !value.include?("\r")
      value
    end

    def validate_lineage!(records, content)
      %w[supersedes follows_up].each do |key|
        next unless content[key]
        target = records[content[key]]
        fail!("handoff lineage reference is invalid") unless target && target.fetch("project") == content.fetch("project")
      end
    end

    def validate_reference!(value)
      fail!("handoff reference is invalid") unless value.is_a?(String) && !value.empty? && value.length <= 300 && !value.start_with?("/") && !value.split("/").include?("..") && !value.include?("\\")
      value
    end

    def parse_time?(value)
      Time.iso8601(value.to_s)
      true
    rescue ArgumentError
      false
    end

    def fail!(message)
      raise ArgumentError, message
    end
  end
end
