#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/handoff"

def create(store, request_id: "request-1", project: "dora", title: "Add a handoff queue", **options)
  store.create!(**{project: project, title: title, objective: "Implement the declared V2 handoff boundary.", acceptance_criteria: ["A focused test passes."], constraints: ["Do not add shell access."], references: ["docs/work/v2.yaml"], created_by: "chatgpt", client_request_id: request_id}.merge(options))
end

Dir.mktmpdir("dora-handoff") do |root|
  store = Dora::Handoff.new(state_root: root, now: -> { Time.utc(2026, 8, 10, 12, 0, 0) })
  created = create(store)
  abort "handoff did not start READY" unless created.fetch("status") == "READY" && created.fetch("schema_version") == 1
  retry_result = create(store)
  abort "idempotent creation duplicated a handoff" unless retry_result.fetch("id") == created.fetch("id") && store.list(project: "dora").length == 1
  begin
    create(store, title: "Conflicting handoff")
    abort "conflicting idempotency key passed"
  rescue ArgumentError => error
    abort "wrong idempotency error" unless error.message.include?("idempotency")
  end
  brief = {"request_mode" => "owner_decision_required", "expected_result" => "An owner decision.", "locked_decisions" => ["No remote execution."], "non_goals" => ["Do not guess product behavior."], "context" => ["V2.1 fixture."], "stop_conditions" => ["Stop until the owner answers."], "verification" => [], "open_decisions" => ["Which lifecycle outcome should change?"]}
  follow_up = create(store, request_id: "brief-follow-up", title: "Resolve a decision", follows_up: created.fetch("id"), brief: brief)
  abort "brief or immutable lineage was not retained" unless follow_up.dig("brief", "request_mode") == "owner_decision_required" && follow_up.fetch("follows_up") == created.fetch("id")
  begin
    create(store, request_id: "bad-brief", brief: brief.merge("request_mode" => "implementation"))
    abort "ambiguous implementation brief passed"
  rescue ArgumentError
    nil
  end
  begin
    create(store, request_id: "cross-project-lineage", project: "other", follows_up: created.fetch("id"), brief: brief)
    abort "cross-project lineage passed"
  rescue ArgumentError
    nil
  end
  begin
    create(store, request_id: "bad-path", project: "dora", title: "Invalid reference").tap { |_record| }
    store.create!(project: "dora", title: "Invalid reference", objective: "No filesystem write.", acceptance_criteria: [], constraints: [], references: ["/tmp/pwn"], created_by: "chatgpt", client_request_id: "path-1")
    abort "absolute reference passed"
  rescue ArgumentError
    nil
  end
  claimed = store.claim!(id: created.fetch("id"), project: "dora", claimed_by: "codex")
  abort "claim failed" unless claimed.fetch("status") == "CLAIMED"
  feedback = {"phase" => "IMPLEMENTING", "milestone" => "Lifecycle contract implemented", "progress" => "Validated local mutation surface is ready", "finding" => "Existing delivery evidence can remain authoritative", "verification_state" => "IMPLEMENTED_UNVERIFIED", "deviations" => [], "residual_risks" => ["Bridge read projection remains pending"]}
  reported = store.feedback!(id: created.fetch("id"), project: "dora", feedback: feedback)
  abort "feedback did not retain current milestone" unless reported.dig("collaboration", "phase") == "IMPLEMENTING" && reported.fetch("feedback_history").length == 1
  active_lifecycle = store.lifecycle_readback!(id: created.fetch("id"), project: "dora")
  expected_active_progress = {"phase" => "IMPLEMENTATION", "label" => "Implementation", "summary" => "Handoff implementation is in progress.", "material_change_token" => "CLAIMED:IMPLEMENTATION:IMPLEMENTED_UNVERIFIED"}
  abort "safe active progress projection is incomplete or leaked feedback prose" unless active_lifecycle.fetch("progress") == expected_active_progress && !active_lifecycle.to_s.include?(feedback.fetch("milestone")) && !active_lifecycle.to_s.include?(feedback.fetch("finding"))
  status_readback = store.status_readback!(id: created.fetch("id"), project: "dora")
  abort "status readback did not include the safe progress projection" unless status_readback.fetch("progress") == expected_active_progress
  begin
    store.feedback!(id: created.fetch("id"), project: "dora", feedback: feedback)
    abort "duplicate feedback transition passed"
  rescue ArgumentError
    nil
  end
  begin
    store.feedback!(id: created.fetch("id"), project: "dora", feedback: feedback.merge("phase" => "START", "verification_state" => "NOT_STARTED"))
    abort "regressive feedback transition passed"
  rescue ArgumentError
    nil
  end
  begin
    store.feedback!(id: created.fetch("id"), project: "dora", feedback: feedback.merge("milestone" => "x" * 1_001))
    abort "oversized feedback passed"
  rescue ArgumentError
    nil
  end
  begin
    store.feedback!(id: created.fetch("id"), project: "dora", feedback: feedback.merge("phase" => "VERIFYING", "verification_state" => "VERIFIED"))
    abort "feedback-owned verified state passed"
  rescue ArgumentError
    nil
  end
  abort "invalid feedback corrupted the current projection" unless store.lifecycle_readback!(id: created.fetch("id"), project: "dora").fetch("progress") == expected_active_progress
  begin
    store.feedback!(id: created.fetch("id"), project: "dora", feedback: feedback.merge("milestone" => "terminal\noutput"))
    abort "terminal-style feedback passed"
  rescue ArgumentError
    nil
  end
  begin
    store.claim!(id: created.fetch("id"), project: "dora", claimed_by: "codex")
    abort "double claim passed"
  rescue ArgumentError
    nil
  end
  begin
    store.complete!(id: created.fetch("id"), project: "dora", verification_references: ["docs/work/v2.yaml"])
    abort "completion without delivery passed"
  rescue ArgumentError => error
    abort "wrong completion guard" unless error.message.include?("delivery")
  end
  linked = store.link_delivery!(id: created.fetch("id"), project: "dora", master_plan: "docs/work/v2-master.yaml", work_plan: "docs/work/v2.yaml")
  abort "delivery link missing" unless linked.dig("delivery", "master_plan") == "docs/work/v2-master.yaml"
  completion_result = {"work_performed" => ["Implemented the bounded lifecycle contract"], "decisions" => ["Reuse Dora delivery evidence"], "acceptance_results" => ["Lifecycle validation passed"], "deviations" => [], "residual_risks" => ["External runtime remains owner-gated"], "follow_up" => {"needed" => false, "reason" => nil}}
  completed = store.complete!(id: created.fetch("id"), project: "dora", verification_references: ["docs/work/v2.yaml#verify"], completion_result: completion_result)
  abort "completion did not retain evidence and compact result" unless completed.fetch("status") == "COMPLETED" && completed.dig("completion", "verification_references") == ["docs/work/v2.yaml#verify"] && completed.dig("completion", "result", "work_performed") == completion_result.fetch("work_performed")
  completed_readback = store.lifecycle_readback!(id: created.fetch("id"), project: "dora")
  abort "completed lifecycle readback is incomplete" unless completed_readback == {"handoff_id" => created.fetch("id"), "status" => "COMPLETED", "outcome_category" => "COMPLETED", "verification" => {"state" => "VERIFIED", "evidence_references" => ["docs/work/v2.yaml#verify"]}, "progress" => {"phase" => "COMPLETED", "label" => "Completed", "summary" => "Handoff completed with Dora verification evidence.", "material_change_token" => "COMPLETED:COMPLETED:VERIFIED"}, "summary" => "Handoff completed with Dora verification evidence."}
  abort "lifecycle readback exposed immutable content" if completed_readback.to_s.include?(completion_result.fetch("work_performed").first) || completed_readback.to_s.include?(created.fetch("objective"))
  sensitive = create(store, request_id: "sensitive-readback", title: "Private value", objective: "Do not expose SIMULATED_SECRET_VALUE through lifecycle readback.")
  sensitive_readback = store.lifecycle_readback!(id: sensitive.fetch("id"), project: "dora")
  abort "lifecycle readback leaked a secret-like immutable value" if sensitive_readback.to_s.include?("SIMULATED_SECRET_VALUE")
  blocked = create(store, request_id: "owner-decision-block", title: "Need an owner decision")
  store.claim!(id: blocked.fetch("id"), project: "dora", claimed_by: "codex")
  owner_decision = {"question" => "Which approved read surface should show this summary?", "why" => "The owner must choose the public contract.", "options" => ["Extend get_handoff_status", "Add a new tool"], "recommendation" => "Extend get_handoff_status", "blocked_work" => ["Public read projection"], "may_continue" => ["Local lifecycle tests"]}
  blocked = store.block_owner_decision!(id: blocked.fetch("id"), project: "dora", owner_decision: owner_decision)
  abort "structured owner decision was not retained" unless blocked.fetch("status") == "BLOCKED" && blocked.dig("blocked", "owner_decision", "question") == owner_decision.fetch("question")
  blocked_readback = store.lifecycle_readback!(id: blocked.fetch("id"), project: "dora")
  abort "blocked lifecycle readback did not remain owner-safe" unless blocked_readback.fetch("status") == "BLOCKED" && blocked_readback.fetch("outcome_category") == "BLOCKED" && blocked_readback.dig("progress", "phase") == "BLOCKED" && !blocked_readback.to_s.include?(owner_decision.fetch("question"))
  verification_failed = create(store, request_id: "verification-failed", title: "Record a verification failure")
  store.claim!(id: verification_failed.fetch("id"), project: "dora", claimed_by: "codex")
  store.feedback!(id: verification_failed.fetch("id"), project: "dora", feedback: feedback.merge("phase" => "VERIFYING", "milestone" => "Verification failed", "verification_state" => "VERIFICATION_FAILED"))
  failed_readback = store.lifecycle_readback!(id: verification_failed.fetch("id"), project: "dora")
  abort "verification failure lifecycle readback is ambiguous" unless failed_readback.fetch("status") == "CLAIMED" && failed_readback.fetch("outcome_category") == "VERIFICATION_FAILED" && failed_readback.dig("verification", "state") == "VERIFICATION_FAILED" && failed_readback.fetch("progress") == {"phase" => "VERIFICATION", "label" => "Verification", "summary" => "Verification did not pass; the handoff remains claimed for owner recovery.", "material_change_token" => "CLAIMED:VERIFICATION:VERIFICATION_FAILED"}
  runner_block = create(store, request_id: "runner-failure", title: "Record a runner failure")
  store.claim!(id: runner_block.fetch("id"), project: "dora", claimed_by: "codex")
  runner_failure = {"code" => "keychain_unavailable", "exit_code" => 1, "recovery_hint" => "Run local setup, then create a follow-up handoff."}
  runner_block = store.block_runner_failure!(id: runner_block.fetch("id"), project: "dora", failure: runner_failure)
  abort "structured runner failure was not retained" unless runner_block.dig("blocked", "runner_failure") == runner_failure
  execution_block = create(store, request_id: "codex-execution-failure", title: "Record a Codex execution failure")
  store.claim!(id: execution_block.fetch("id"), project: "dora", claimed_by: "codex")
  execution_failure = {"code" => "codex_execution_failed", "exit_code" => 71, "recovery_hint" => "Review the claimed task outcome, then create a follow-up handoff."}
  execution_block = store.block_runner_failure!(id: execution_block.fetch("id"), project: "dora", failure: execution_failure)
  abort "Codex execution failure code was not retained" unless execution_block.dig("blocked", "runner_failure") == execution_failure
  begin
    store.block_runner_failure!(id: follow_up.fetch("id"), project: "dora", failure: runner_failure.merge("code" => "raw terminal output"))
    abort "invalid runner failure code passed"
  rescue ArgumentError
    nil
  end
  abort "private store leaked its root" if completed.to_s.include?(root)
  events = File.read(File.join(root, "handoff-events.jsonl"))
  abort "store persisted a tunnel secret" if events.include?("CONTROL_PLANE_API_KEY") || events.include?("tunnel_")
  abort "private modes are wrong" unless (File.stat(root).mode & 0o777) == 0o700 && (File.stat(File.join(root, "handoff-events.jsonl")).mode & 0o777) == 0o600
end

Dir.mktmpdir("dora-handoff-concurrency") do |root|
  ids = 2.times.map do
    fork do
      store = Dora::Handoff.new(state_root: root)
      puts create(store, request_id: "same-request").fetch("id")
    end
  end
  ids.each { |pid| Process.wait(pid) }
  store = Dora::Handoff.new(state_root: root)
  record = store.next_ready!(project: "dora")
  abort "concurrent creation did not converge" unless store.list(project: "dora").length == 1 && record
  outcomes = 2.times.map do
    fork do
      begin
        Dora::Handoff.new(state_root: root).claim!(id: record.fetch("id"), project: "dora", claimed_by: "codex")
        exit 0
      rescue ArgumentError
        exit 1
      end
    end
  end.map { |pid| Process.wait2(pid).last.exitstatus }
  abort "concurrent claim was not exclusive" unless outcomes.sort == [0, 1]
end

Dir.mktmpdir("dora-handoff-symlink") do |root|
  target = File.join(root, "target"); FileUtils.mkdir_p(target)
  link = File.join(root, "state-link"); File.symlink(target, link)
  begin
    Dora::Handoff.new(state_root: link)
    abort "symlink state root passed"
  rescue ArgumentError
    nil
  end
end

puts "Dora handoff test passed (immutable records, idempotency, lifecycle feedback, owner decisions, structured runner failures, completion evidence, containment, permissions, and concurrency)."
