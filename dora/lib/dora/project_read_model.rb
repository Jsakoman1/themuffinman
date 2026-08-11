# frozen_string_literal: true

require "digest"
require "time"
require "yaml"

require_relative "adapter"
require_relative "agent_next"
require_relative "decision_log"
require_relative "project_doctor"
require_relative "project_knowledge"
require_relative "project_memory"
require_relative "intent_plan_alignment"

module Dora
  # Read-only, sanitized aggregation of declared Dora project artifacts.
  #
  # This is intentionally independent from transport and client identity. A bridge
  # selects an allowed adapter before constructing this object; this model then
  # reads only files under that adapter's declared project root and work-plan root.
  class ProjectReadModel
    SCHEMA_PATH = File.expand_path("../../project-adapter.schema.yaml", __dir__)
    CONTROL_SCHEMA_PATH = File.expand_path("../../project-control.schema.yaml", __dir__)
    STATUS = %w[HEALTHY WARNING INVALID].freeze

    def self.load!(adapter_path:, adapter_schema_path: SCHEMA_PATH, control_schema_path: CONTROL_SCHEMA_PATH)
      new(adapter_path: adapter_path, adapter_schema_path: adapter_schema_path, control_schema_path: control_schema_path)
    end

    def initialize(adapter_path:, adapter_schema_path:, control_schema_path:)
      @adapter_path = File.expand_path(adapter_path)
      @adapter = Adapter.validate!(@adapter_path, adapter_schema_path)
      @root = File.realpath(@adapter.fetch("root"))
      @control_schema_path = control_schema_path
      @work_root = resolve_declared_root!("work_plans")
      @docs_root = resolve_declared_root!("docs")
      @runtime_evidence_root = @adapter.fetch("paths").fetch("runtime_evidence")
    end

    def summary
      inconsistencies = []
      health = ProjectDoctor.run(@adapter_path, schema_path: SCHEMA_PATH, control_schema_path: @control_schema_path)
      append_doctor_advisories(health, inconsistencies)
      knowledge = load_knowledge(inconsistencies)
      memory = load_memory(inconsistencies)
      inventories = load_inventories(inconsistencies)
      validate_memory_navigation(memory, inventories, inconsistencies)
      deliveries = resolve_deliveries(inventories, inconsistencies)
      next_task = resolve_next_task(inventories, inconsistencies)
      decisions = resolve_open_decisions(knowledge, memory, inconsistencies)
      integrity = integrity_summary(health, inconsistencies)

      {
        "kind" => "dora_project_read_model",
        "version" => 1,
        "project" => {"id" => @adapter.fetch("project"), "name" => knowledge.dig("product_brief", "product") || @adapter.fetch("project"), "adapter" => ".dora/project.yaml"},
        "state" => integrity.fetch("status"),
        "health" => safe_health(health, integrity),
        "integrity" => integrity,
        "inconsistencies" => inconsistencies,
        "delivery" => deliveries,
        "current_goal" => current_goal(deliveries, next_task),
        "next_task" => next_task,
        "open_decisions" => decisions,
        "references" => summary_references(knowledge, memory, deliveries)
      }.freeze
    end

    # A compact, regenerable navigation view over the same sanitized facts used by
    # summary. It deliberately accepts a supplied summary for testability and does
    # not write or reinterpret canonical work/decision state.
    def self.current_work_index(summary:)
      fail!("current work index summary is invalid") unless summary.is_a?(Hash) && summary["kind"] == "dora_project_read_model" && summary["version"].to_i == 1

      delivery = summary["delivery"].is_a?(Hash) ? summary.fetch("delivery") : {}
      current = summary["current_goal"].is_a?(Hash) ? safe_index_goal(summary.fetch("current_goal")) : legacy_current_work(summary, delivery)
      latest = delivery["latest_verified"]
      references = Array(summary["references"]).select { |reference| safe_index_reference?(reference) }
      references.concat(Array(current["references"]))
      references << latest["master_plan"] if latest.is_a?(Hash) && safe_index_reference?(latest["master_plan"])

      {
        "kind" => "dora_current_work_index",
        "version" => 1,
        "observed_at" => Time.now.utc.iso8601,
        "source_references" => references.uniq.sort,
        "read_only" => true,
        "disposition" => "advisory",
        "current_work" => current,
        "latest_verified_delivery" => latest.is_a?(Hash) ? safe_index_delivery(latest) : nil,
        "open_decisions" => Array(summary["open_decisions"]).select { |entry| entry.is_a?(Hash) }.map { |entry| entry.slice("id", "statement", "source", "reference") },
        "next_action" => summary["next_task"].is_a?(Hash) ? safe_index_task(summary.fetch("next_task")) : nil,
        "completion_boundary" => "This index projects an existing ProjectReadModel summary only; it cannot persist or change work status, create or amend a decision, invoke GitHub, mutate a consumer project, run a repair, or start a runner or remote agent."
      }.compact.freeze
    end

    def current_work_index
      self.class.current_work_index(summary: summary)
    end

    def plan(plan_path)
      plan = load_plan!(plan_path)
      safe_plan(plan, plan_path)
    end

    def task(plan_path, task_id)
      plan = load_plan!(plan_path)
      task = Array(plan["tasks"]).find { |candidate| candidate.is_a?(Hash) && candidate["id"] == task_id }
      fail!("task is not declared: #{task_id}") unless task

      safe_task(task, plan_path)
    end

    def task_evidence(plan_path, task_id)
      plan = load_plan!(plan_path)
      task(plan_path, task_id)
      evidence = Array(plan["evidence"]).find { |candidate| candidate.is_a?(Hash) && candidate["task"] == task_id }
      return {"task" => task_id, "status" => "not_recorded", "reference" => plan_path}.freeze unless evidence

      safe_evidence(evidence, task_id: task_id, plan_path: plan_path)
    end

    # Owner-readable, regenerable closeout for one verifier-recorded work plan.
    # It deliberately reads plan evidence and decision records without assigning a
    # delivery status or writing a new summary artifact.
    def owner_delivery_closeout(plan_path)
      plan = load_plan!(plan_path)
      fail!("owner delivery closeout requires a verified work plan") unless plan["kind"] == "work" && plan["status"] == "verified"
      tasks = Array(plan["tasks"])
      fail!("verified work plan has no tasks") if tasks.empty?
      completed = tasks.map do |task|
        safe = safe_task(task, plan_path)
        evidence = task_evidence(plan_path, safe.fetch("id"))
        fail!("verified work task lacks passing evidence") unless safe.fetch("status") == "done" && evidence.fetch("status") == "passed"

        safe.slice("id", "title", "observable_outcome", "reference").merge("evidence" => evidence)
      end
      decisions = decision_log_entries([]).select { |entry| Array(decision_entry_references(entry)).include?(plan_path) }.map { |entry| entry.slice("id", "decision", "status", "reference") }
      {
        "kind" => "dora_owner_delivery_closeout",
        "version" => 1,
        "observed_at" => Time.now.utc.iso8601,
        "read_only" => true,
        "work" => {"id" => plan["id"], "title" => plan["title"], "reference" => plan_path},
        "completed_tasks" => completed,
        "decisions" => decisions,
        "follow_up" => decisions.any? { |decision| decision["status"] == "proposed" } ? "owner_decision_required" : "none_declared",
        "verification_boundary" => "Completion is reported only from passing task evidence recorded by the project work verifier.",
        "references" => ([plan_path] + decisions.map { |decision| decision["reference"] }).compact.uniq
      }.freeze
    end

    # A small, terminal-safe view of one declared Master Plan. It is intentionally
    # read-only and fails closed when the plan, inventory, task, or verification
    # evidence cannot be reconciled from canonical Dora artifacts.
    def master_plan_progress(master_plan_path)
      master = load_plan!(master_plan_path)
      fail!("master plan is invalid: #{master_plan_path}") unless master["kind"] == "master"
      master_id = safe_terminal_identifier!(master["id"], "master plan identifier")
      master_title = safe_terminal_text!(master["title"], "master plan title")
      inventory_path = master["execution_inventory"]
      fail!("master plan execution inventory is invalid") unless safe_relative_path?(inventory_path)

      inventory = load_inventory!(inventory_path)
      fail!("execution inventory belongs to a different master") unless inventory.fetch("master_plan") == master_plan_path
      fail!("execution inventory is not active") unless %w[active verified].include?(inventory["state"])
      children = Array(master["children"])
      fail!("master plan children are invalid") unless children.all? { |path| safe_relative_path?(path) }
      items = inventory.fetch("items").sort_by { |item| item.fetch("order") }
      fail!("execution inventory items are not uniquely ordered") unless items.map { |item| item.fetch("order") }.uniq.length == items.length
      fail!("execution inventory item identifiers are not unique") unless items.map { |item| item.fetch("id") }.uniq.length == items.length

      projected_items = items.map { |item| master_plan_progress_item(item, children) }
      projection = {
        "master_plan" => {"id" => master_id, "title" => master_title, "reference" => master_plan_path},
        "items" => projected_items
      }
      projection["material_change_token"] = Digest::SHA256.hexdigest(Marshal.dump(projection))
      projection.freeze
    end

    # This is deliberately derived at request time. An Intent Plan proposal is
    # neither persisted nor treated as a Dora work or decision artifact.
    def align_intent_plan(proposal)
      project = summary
      IntentPlanAlignment.evaluate(
        proposal: proposal,
        canonical_state: {
          "state" => project.fetch("state"),
          "active_delivery" => project.dig("delivery", "active"),
          "latest_verified_delivery" => project.dig("delivery", "latest_verified"),
          "open_decisions" => project.fetch("open_decisions"),
          "accepted_decisions" => accepted_decisions
        }
      )
    end

    private

    def self.safe_index_reference?(value)
      value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_index_reference?

    def self.safe_index_delivery(delivery)
      delivery.slice("id", "title", "status", "master_plan", "inventory", "task")
    end
    private_class_method :safe_index_delivery

    def self.safe_index_task(task)
      task.slice("id", "title", "plan", "task", "status", "observable_outcome", "reference")
    end
    private_class_method :safe_index_task

    def self.safe_index_goal(goal)
      state = goal["state"]
      return {"state" => "none"} if state == "none"
      return {"state" => "ambiguous", "references" => Array(goal["references"]).select { |reference| safe_index_reference?(reference) }.sort} if state == "ambiguous"

      result = {"state" => state}.compact
      result["delivery"] = safe_index_delivery(goal.fetch("delivery")) if goal["delivery"].is_a?(Hash)
      result["next_task"] = safe_index_task(goal.fetch("next_task")) if goal["next_task"].is_a?(Hash)
      result
    end
    private_class_method :safe_index_goal

    def self.legacy_current_work(summary, delivery)
      active = delivery["active"]
      if active.is_a?(Hash) && active["status"] == "ambiguous"
        {"state" => "ambiguous", "references" => Array(active["references"]).select { |reference| safe_index_reference?(reference) }.sort}
      elsif active.is_a?(Hash)
        {"state" => "active", "delivery" => safe_index_delivery(active)}
      elsif summary["next_task"].is_a?(Hash)
        {"state" => "planned", "next_task" => safe_index_task(summary.fetch("next_task"))}
      else
        {"state" => "none"}
      end
    end
    private_class_method :legacy_current_work

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!

    def resolve_declared_root!(key)
      relative = @adapter.fetch("paths").fetch(key)
      resolve_under_root!(relative, "declared #{key} root")
    end

    def load_knowledge(inconsistencies)
      ProjectKnowledge::ARTIFACTS.each_value { |path| resolve_under_root!(path, "project knowledge artifact") }
      ProjectKnowledge.validate!(@root)
    rescue ArgumentError => error
      inconsistencies << issue("INVALID", "project_knowledge", "project knowledge is invalid", ["docs/product-brief.yaml", "docs/domain-library.yaml", ".dora/agent-project-profile.yaml"])
      {}
    end

    def append_doctor_advisories(health, inconsistencies)
      Array(health["checks"]).each do |check|
        next unless check.is_a?(Hash) && check["status"] == "advisory" && statement?(check["id"])

        code = check.fetch("id").start_with?("work-artifact:verified-work-active-inventory:") ? "control_state_conflict" : "doctor_advisory"
        inconsistencies << issue("WARNING", code, check.fetch("detail").to_s.empty? ? "local Dora advisory requires review" : check.fetch("detail"), Array(check["source_references"]), classification: code == "control_state_conflict" ? "conflict" : "warning")
      end
    end

    def load_memory(inconsistencies)
      path = File.join(@docs_root, "project-memory.yaml")
      return nil unless File.file?(path)

      relative = File.join(@adapter.fetch("paths").fetch("docs"), "project-memory.yaml")
      ProjectMemory.load!(resolve_under_root!(relative, "project memory"))
    rescue ArgumentError => error
      inconsistencies << issue("INVALID", "project_memory", "project memory is invalid", [relative_to_root(path)])
      nil
    end

    def load_inventories(inconsistencies)
      inventory_paths = Dir[File.join(@work_root, "**", "*inventory*.yaml")].sort
      inventory_paths.each_with_object([]) do |absolute, inventories|
        contained = resolve_under_root!(relative_to_root(absolute), "execution inventory")
        fail!("execution inventory is outside declared work-plans root") unless contained.start_with?("#{@work_root}/")
        document = YAML.load_file(contained)
        next unless document.is_a?(Hash) && document["kind"] == "execution_inventory" && document["version"].to_i == 1

        inventories << safe_inventory(document, relative_to_root(absolute))
      rescue Psych::Exception, ArgumentError => error
        inconsistencies << issue("INVALID", "execution_inventory", "execution inventory is invalid", [relative_to_root(absolute)])
      end
    end

    def validate_memory_navigation(memory, inventories, inconsistencies)
      return unless memory

      ProjectMemory.validate_work_navigation!(memory: memory, inventories: inventories)
    rescue ArgumentError => error
      classification = if error.message.include?("stale")
                         "stale"
                       elsif error.message.include?("ambiguous")
                         "ambiguous"
                       elsif error.message.include?("contradictory")
                         "conflict"
                       else
                         "invalid"
                       end
      message = case classification
                when "stale" then "project memory current-work navigation is stale"
                when "ambiguous" then "project memory current-work navigation is ambiguous"
                when "conflict" then "project memory current-work navigation conflicts with declared execution state"
                else "project memory current-work navigation is invalid"
                end
      inconsistencies << issue("INVALID", "project_memory", message, ["docs/project-memory.yaml"], classification: classification)
    end

    def safe_inventory(document, path)
      items = document["items"]
      fail!("execution inventory items are invalid: #{path}") unless items.is_a?(Array)
      normalized = items.map do |item|
        fail!("execution inventory item is invalid: #{path}") unless item.is_a?(Hash) && identifier?(item["id"]) && safe_relative_path?(item["plan"]) && identifier?(item["task"]) && statement?(item["status"])
        item.slice("id", "order", "plan", "task", "status", "started_at", "verified_at", "evidence")
      end
      state = document["state"]
      fail!("execution inventory state is invalid: #{path}") unless state.nil? || statement?(state)

      {"id" => document["id"], "path" => path, "master_plan" => safe_relative_path?(document["master_plan"]) ? document["master_plan"] : nil, "state" => state, "items" => normalized}.compact.freeze
    end

    def resolve_deliveries(inventories, inconsistencies)
      eligible_inventories = inventories.reject { |inventory| non_delivery_inventory?(inventory) }
      active = eligible_inventories.select { |inventory| inventory.fetch("items").any? { |item| item["status"] == "in_progress" } }
      active_delivery = if active.length == 1
                          delivery_summary(active.first, "active", inconsistencies)
                        elsif active.length > 1
                          inconsistencies << issue("WARNING", "active_delivery", "multiple execution inventories contain in-progress work", active.map { |item| item.fetch("path") })
                          {"status" => "ambiguous", "references" => active.map { |item| item.fetch("path") }}
                        end

      verified = verified_delivery_candidates(eligible_inventories, inconsistencies)
      latest_at = verified.map { |entry| entry.fetch("verified_at") }.max
      latest = latest_at ? verified.select { |entry| entry.fetch("verified_at") == latest_at } : []
      latest_delivery = if latest.length == 1
                          delivery_summary(latest.first.fetch("inventory"), "latest_verified", inconsistencies, latest.first.fetch("item"), verified_at: latest.first.fetch("verified_at").iso8601)
                        elsif latest.length > 1
                          references = latest.flat_map { |entry| [entry.fetch("inventory").fetch("path"), entry.fetch("item").fetch("plan")] }.uniq.sort
                          inconsistencies << issue("WARNING", "latest_verified_delivery", "multiple evidence-backed verified delivery candidates share the latest timestamp", references, classification: "ambiguous")
                          {"role" => "latest_verified", "status" => "ambiguous", "references" => references}.freeze
                        end
      {"active" => active_delivery, "latest_verified" => latest_delivery}.compact.freeze
    end

    def verified_delivery_candidates(inventories, inconsistencies)
      inventories.each_with_object([]) do |inventory, candidates|
        next unless inventory["state"] == "verified"

        inventory.fetch("items").each do |item|
          next unless item["status"] == "verified" && parse_time(item["verified_at"])

          plan = load_plan(item.fetch("plan"))
          evidence = plan && task_evidence(item.fetch("plan"), item.fetch("task"))
          next unless plan && plan["status"] == "verified" && evidence && evidence["status"] == "passed"

          candidates << {"inventory" => inventory, "item" => item, "verified_at" => parse_time(item.fetch("verified_at"))}
        rescue ArgumentError
          inconsistencies << issue("WARNING", "latest_verified_delivery", "verified delivery inventory item cannot be reconciled with passing Dora evidence", [inventory.fetch("path"), item.fetch("plan")], classification: "conflict")
        end
      end
    end

    def delivery_summary(inventory, role, inconsistencies, terminal_item = nil, verified_at: nil)
      master_path = inventory["master_plan"]
      master = master_path ? load_plan(master_path) : nil
      if master_path && !master
        inconsistencies << issue("WARNING", "delivery_master", "execution inventory references a missing or invalid master plan", [inventory.fetch("path"), master_path])
      end
      selected = terminal_item || inventory.fetch("items").find { |item| item["status"] == "in_progress" }
      {
        "role" => role,
        "id" => master && master["id"],
        "title" => master && master["title"],
        "status" => master && master["status"],
        "master_plan" => master_path,
        "inventory" => inventory.fetch("path"),
        "task" => selected && selected.slice("id", "plan", "task", "status", "verified_at")
      }.compact.merge(verified_at ? {"verified_at" => verified_at} : {}).freeze
    end

    def resolve_next_task(inventories, inconsistencies)
      eligible_inventories = inventories.reject { |inventory| non_delivery_inventory?(inventory) }
      active = eligible_inventories.select { |inventory| inventory.fetch("items").any? { |item| item["status"] == "in_progress" } }
      return nil if active.length > 1
      candidate = active.first || eligible_inventories.find { |inventory| inventory.fetch("items").any? { |item| item["status"] == "pending" } }
      return nil unless candidate

      AgentNext.next!(project_root: @root, inventory_path: candidate.fetch("path"))
    rescue ArgumentError => error
      inconsistencies << issue("WARNING", "next_task", "next eligible task cannot be resolved", candidate ? [candidate.fetch("path")] : [])
      nil
    end

    def current_goal(deliveries, next_task)
      active = deliveries["active"]
      return {"state" => "ambiguous", "references" => Array(active["references"]).sort}.freeze if active.is_a?(Hash) && active["status"] == "ambiguous"
      return {"state" => "active", "source" => "execution_inventory", "delivery" => active}.freeze if active.is_a?(Hash)
      return {"state" => "none"}.freeze unless next_task.is_a?(Hash)

      state = {"start" => "planned", "continue" => "active", "blocked" => "blocked"}[next_task["action"]]
      return {"state" => "none"}.freeze unless state

      {"state" => state, "source" => "execution_inventory", "next_task" => next_task}.freeze
    end

    def resolve_open_decisions(knowledge, memory, inconsistencies)
      records = Array(knowledge.dig("product_brief", "unanswered_decisions")).map do |statement|
        {"source" => "product_brief", "statement" => statement, "reference" => "docs/product-brief.yaml"}
      end
      memory_records = Array(memory && memory["open_decisions"])
      canonical = records.map { |record| record.fetch("statement") }.sort
      remembered = memory_records.map { |record| record.fetch("statement") }.sort
      if memory && canonical != remembered
        inconsistencies << issue("WARNING", "open_decisions", "project memory and product brief declare different open decisions", ["docs/product-brief.yaml", "docs/project-memory.yaml"])
      end
      records.concat(memory_records.reject { |record| canonical.include?(record.fetch("statement")) }.map { |record| record.slice("id", "statement", "source").merge("source" => "project_memory", "reference" => record.fetch("source")) })
      decision_log_entries(inconsistencies).each do |entry|
        next unless entry["status"] == "proposed"

        records << {"id" => entry.fetch("id"), "source" => "decision_log", "statement" => entry.fetch("decision"), "reference" => entry.fetch("reference")}
      end
      records.uniq { |record| record["statement"] }.freeze
    end

    def decision_log_entries(inconsistencies)
      decision_log_paths.each_with_object([]) do |path, entries|
        next unless File.file?(File.join(@root, path))

        log = DecisionLog.load!(resolve_under_root!(path, "decision log"))
        entries.concat(log.fetch("entries").map { |entry| entry.slice("id", "decision", "status", "plan_references").merge("reference" => path) })
      rescue ArgumentError => error
        inconsistencies << issue("INVALID", "decision_log", "decision log is invalid", [path])
      end
    end

    def decision_entry_references(entry)
      Array(entry["plan_references"])
    end

    def accepted_decisions
      decision_log_paths.each_with_object([]) do |path, decisions|
        next unless File.file?(File.join(@root, path))

        log = DecisionLog.load!(resolve_under_root!(path, "decision log"))
        decisions.concat(log.fetch("entries").select { |entry| entry["status"] == "accepted" }.map { |entry| {"id" => entry.fetch("id"), "statement" => entry.fetch("decision")} })
      rescue ArgumentError
        []
      end
    end

    def decision_log_paths
      [File.join(@adapter.fetch("paths").fetch("docs"), "decision-log.yaml"), ".dora/decision-log.yaml"].select { |path| safe_relative_path?(path) }.uniq
    end

    def summary_references(knowledge, memory, deliveries)
      references = [".dora/project.yaml", "docs/product-brief.yaml", "docs/domain-library.yaml"]
      references << "docs/project-memory.yaml" if memory
      references << deliveries.dig("active", "master_plan") if deliveries.dig("active", "master_plan")
      references << deliveries.dig("latest_verified", "master_plan") if deliveries.dig("latest_verified", "master_plan")
      references.compact.uniq.select { |path| safe_relative_path?(path) }.freeze
    end

    def load_plan(path)
      load_plan!(path)
    rescue ArgumentError
      nil
    end

    def load_plan!(path)
      absolute = resolve_under_root!(path, "work plan")
      fail!("work plan is outside the declared work-plans root: #{path}") unless absolute.start_with?("#{@work_root}/")
      fail!("work plan is missing: #{path}") unless File.file?(absolute)
      document = YAML.load_file(absolute)
      fail!("work plan is invalid: #{path}") unless document.is_a?(Hash) && %w[work master].include?(document["kind"]) && document["version"].to_i == 1
      document
    rescue Psych::Exception => error
      fail!("work plan YAML is invalid: #{error.message}")
    end

    def load_inventory!(path)
      absolute = resolve_under_root!(path, "execution inventory")
      fail!("execution inventory is outside the declared work-plans root: #{path}") unless absolute.start_with?("#{@work_root}/")
      fail!("execution inventory is missing: #{path}") unless File.file?(absolute)
      document = YAML.load_file(absolute)
      fail!("execution inventory is invalid: #{path}") unless document.is_a?(Hash) && document["kind"] == "execution_inventory" && document["version"].to_i == 1

      normalized = safe_inventory(document, path)
      fail!("execution inventory master plan is invalid: #{path}") unless normalized["master_plan"]
      items = normalized.fetch("items")
      fail!("execution inventory items are invalid: #{path}") unless items.all? { |item| item["order"].is_a?(Integer) }

      normalized
    rescue Psych::Exception => error
      fail!("execution inventory YAML is invalid: #{error.message}")
    end

    def master_plan_progress_item(item, children)
      status = item.fetch("status")
      fail!("execution inventory item status is invalid") unless %w[verified in_progress pending blocked].include?(status)
      plan_path = item.fetch("plan")
      fail!("execution inventory item is not a Master Plan child") unless children.include?(plan_path)
      task = task(plan_path, item.fetch("task"))
      task_id = safe_terminal_identifier!(task.fetch("id"), "task identifier")
      task_title = safe_terminal_text!(task.fetch("title"), "task title")
      evidence = task_evidence(plan_path, task_id)
      fail!("verified inventory item lacks passing Dora evidence") if status == "verified" && evidence.fetch("status") != "passed"

      {"id" => safe_terminal_identifier!(item.fetch("id"), "inventory item identifier"), "status" => status, "task" => {"id" => task_id, "title" => task_title, "reference" => plan_path}}.freeze
    end

    def safe_plan(plan, path)
      tasks = Array(plan["tasks"]).each_with_object([]) do |task, projected|
        projected << safe_task(task, path) if task.is_a?(Hash)
      end
      {"id" => plan["id"], "title" => plan["title"], "kind" => plan["kind"], "status" => plan["status"], "reference" => path, "tasks" => tasks}.freeze
    end

    def safe_task(task, plan_path)
      fail!("work task is invalid: #{plan_path}") unless identifier?(task["id"]) && statement?(task["title"]) && statement?(task["status"])
      {"id" => task.fetch("id"), "title" => task.fetch("title"), "status" => task.fetch("status"), "observable_outcome" => task["observable_outcome"], "dependencies" => Array(task["dependencies"]).select { |item| statement?(item) }, "reference" => plan_path}.compact.freeze
    end

    def safe_evidence(evidence, task_id:, plan_path:)
      result = evidence["result"] == "passed" ? "passed" : evidence["result"] == "failed" ? "failed" : "recorded"
      {"task" => task_id, "status" => result, "verified_at" => evidence["ranAt"], "revision" => sha?(evidence["revision"]) ? evidence["revision"] : nil, "exit_code" => evidence["exitCode"].is_a?(Integer) ? evidence["exitCode"] : nil, "validation" => "declared_leaf_validation", "runtime_evidence" => safe_evidence_paths(evidence["runtimeEvidencePaths"]), "visual_evidence" => safe_evidence_paths(evidence["visualEvidencePaths"]), "reference" => plan_path}.compact.freeze
    end

    def safe_evidence_paths(paths)
      Array(paths).select do |path|
        next false unless safe_relative_path?(path)

        path == @runtime_evidence_root || path.start_with?("#{@runtime_evidence_root}/")
      end
    end

    def non_delivery_inventory?(inventory)
      %w[superseded control_reconciled].include?(inventory["state"])
    end

    def resolve_under_root!(path, label)
      fail!("#{label} must be a safe relative path") unless safe_relative_path?(path)
      absolute = File.expand_path(path, @root)
      fail!("#{label} resolves outside project root") unless absolute.start_with?("#{@root}/")
      return absolute unless File.exist?(absolute)

      resolved = File.realpath(absolute)
      fail!("#{label} resolves outside project root") unless resolved.start_with?("#{@root}/")
      resolved
    end

    def relative_to_root(path)
      File.expand_path(path).delete_prefix("#{@root}/")
    end

    def integrity_summary(health, inconsistencies)
      status = state_for(health, inconsistencies)
      signals = inconsistencies.map do |item|
        item.slice("severity", "code", "message", "references").merge("classification" => item["classification"] || integrity_classification(item))
      end
      {"status" => status, "doctor_healthy" => health.fetch("healthy"), "signals" => signals}.freeze
    end

    def integrity_classification(item)
      return "stale" if item["code"] == "project_memory" && item["message"].to_s.include?("stale")
      return "ambiguous" if %w[active_delivery latest_verified_delivery].include?(item["code"])
      return "conflict" if item["code"] == "control_state_conflict"

      item["severity"] == "INVALID" ? "invalid" : "warning"
    end

    def state_for(health, inconsistencies)
      return "INVALID" if inconsistencies.any? { |item| item["severity"] == "INVALID" }
      return "WARNING" unless health.fetch("healthy")
      return "WARNING" if inconsistencies.any?

      "HEALTHY"
    end

    def safe_health(health, integrity)
      {
        "healthy" => integrity.fetch("status") == "HEALTHY",
        "doctor_healthy" => health.fetch("healthy"),
        "status" => integrity.fetch("status"),
        "checks" => Array(health["checks"]).each_with_object([]) do |check, projected|
          projected << check.slice("id", "status") if check.is_a?(Hash) && statement?(check["id"]) && statement?(check["status"])
        end
      }.freeze
    end


    def issue(severity, code, message, references, classification: nil)
      fail!("inconsistency severity is invalid") unless STATUS.include?(severity)
      result = {"severity" => severity, "code" => code, "message" => message, "references" => references.select { |path| safe_relative_path?(path) }.uniq}
      result["classification"] = classification if classification
      result.freeze
    end

    def parse_time(value)
      Time.iso8601(value.to_s).utc
    rescue ArgumentError
      nil
    end

    def identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end

    def statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end

    def safe_relative_path?(path)
      statement?(path) && !path.start_with?("/") && !path.split("/").include?("..")
    end

    def sha?(value)
      value.is_a?(String) && value.match?(/\A[0-9a-f]{7,40}\z/i)
    end

    def safe_terminal_identifier!(value, label)
      fail!("#{label} is invalid") unless identifier?(value)

      value
    end

    def safe_terminal_text!(value, label)
      fail!("#{label} is invalid") unless value.is_a?(String) && value.length.between?(1, 200) && value.match?(/\A[^\r\n\x00-\x1f\x7f]+\z/)

      value
    end

    def fail!(message)
      raise ArgumentError, message
    end
  end
end
