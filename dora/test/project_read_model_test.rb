#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_read_model"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/project-read-model-projects.yaml")).fetch("project")

def write_yaml(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def write_project(root, invalid_memory: false, stale_memory: false, ambiguous: false)
  Dora::ProjectInitializer.initialize!(root, project_id: FIXTURE.fetch("id"), manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief_path = File.join(root, "docs/product-brief.yaml")
  brief = YAML.load_file(brief_path)
  brief["product"] = FIXTURE.fetch("name")
  brief["unanswered_decisions"] = [FIXTURE.fetch("open_decision")]
  File.write(brief_path, YAML.dump(brief))
  memory = {"kind" => "dora_project_memory", "version" => 1, "project_intent" => {"product_brief" => "docs/product-brief.yaml", "domain_library" => "docs/domain-library.yaml"}, "canonical_knowledge" => [{"id" => "product", "path" => "docs/product-brief.yaml", "purpose" => "Product intent."}], "open_decisions" => [{"id" => "retention", "statement" => FIXTURE.fetch("open_decision"), "source" => "docs/product-brief.yaml"}], "capability_intent" => [], "current_work" => {"state" => "none"}}
  memory["current_work"] = {"plan" => "docs/work/delivery-master.yaml", "task" => nil, "state" => "verified"} if invalid_memory
  memory["current_work"] = {"plan" => "docs/work/delivery-master.yaml", "task" => "verify-delivery", "state" => "active"} if stale_memory
  write_yaml(root, "docs/project-memory.yaml", memory)
  write_yaml(root, "docs/decision-log.yaml", {"kind" => "dora_decision_log", "version" => 1, "entries" => [{"id" => "retention-choice", "decision" => "Choose a retention policy.", "status" => "proposed", "domain_references" => ["docs/domain-library.yaml"], "plan_references" => [FIXTURE.fetch("latest_master"), "docs/work/delivery.yaml"], "evidence_references" => ["docs/product-brief.yaml"]}]})
  master = {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Verified delivery", "status" => "verified", "children" => ["docs/work/delivery.yaml"]}
  plan = {"kind" => "work", "version" => 1, "id" => "delivery-work", "title" => "Delivery work", "status" => "verified", "baseline" => "pending", "tasks" => [{"id" => FIXTURE.fetch("latest_task"), "title" => "Verify delivery", "status" => "done", "observable_outcome" => "A safe delivery is verified.", "dependencies" => [], "paths" => ["docs/delivery.md"], "required_paths" => ["docs/delivery.md"], "validation" => "secret-command --token hidden", "evidence_boundary" => ["fixture"]}], "evidence" => [{"task" => FIXTURE.fetch("latest_task"), "result" => "passed", "ranAt" => FIXTURE.fetch("latest_verified_at"), "revision" => "abcdef1", "exitCode" => 0, "output" => FIXTURE.fetch("raw_output"), "runtimeEvidencePaths" => ["docs/runtime-evidence/delivery.json", "../../.env"], "visualEvidencePaths" => ["docs/runtime-evidence/delivery.png"]}]}
  inventory = {
    "kind" => "execution_inventory", "version" => 1, "id" => "delivery", "master_plan" => FIXTURE.fetch("latest_master"), "state" => "verified",
    "items" => [{"id" => "delivery-proof", "order" => 1, "plan" => "docs/work/delivery.yaml", "task" => FIXTURE.fetch("latest_task"), "status" => "verified", "verified_at" => FIXTURE.fetch("latest_verified_at")}]
  }
  write_yaml(root, FIXTURE.fetch("latest_master"), master); write_yaml(root, "docs/work/delivery.yaml", plan); write_yaml(root, FIXTURE.fetch("latest_inventory"), inventory)
  if ambiguous
    duplicate = Marshal.load(Marshal.dump(inventory)); duplicate["id"] = "other"; duplicate["items"][0]["status"] = "in_progress"; duplicate["items"][0]["id"] = "other-work"; write_yaml(root, "docs/work/other-inventory.yaml", duplicate)
    inventory["items"][0]["status"] = "in_progress"; write_yaml(root, FIXTURE.fetch("latest_inventory"), inventory)
  end
end

def write_master_progress(root, invalid_verified_evidence: false)
  master_path = "docs/work/progress-master.yaml"
  plans = %w[verified current pending blocked].map { |state| "docs/work/progress-#{state}.yaml" }
  master = {"kind" => "master", "version" => 1, "id" => "progress-master", "title" => "Canonical progress", "status" => "active", "execution_inventory" => "docs/work/progress-inventory.yaml", "children" => plans}
  write_yaml(root, master_path, master)
  items = %w[verified in_progress pending blocked].each_with_index.map do |status, index|
    state = {"verified" => "verified", "in_progress" => "current", "pending" => "pending", "blocked" => "blocked"}.fetch(status)
    plan_path = "docs/work/progress-#{state}.yaml"
    task_id = "#{state}-task"
    plan = {"kind" => "work", "version" => 1, "id" => "progress-#{state}", "title" => "#{state.capitalize} work", "status" => status == "verified" ? "verified" : "active", "tasks" => [{"id" => task_id, "title" => "#{state.capitalize} task", "status" => status == "verified" ? "done" : "in_progress"}]}
    plan["evidence"] = [{"task" => task_id, "result" => invalid_verified_evidence ? "failed" : "passed", "ranAt" => "2026-08-09T12:00:00Z", "revision" => "abcdef1", "exitCode" => invalid_verified_evidence ? 1 : 0}] if status == "verified"
    write_yaml(root, plan_path, plan)
    {"id" => "progress-#{state}", "order" => index + 1, "plan" => plan_path, "task" => task_id, "status" => status, "verified_at" => status == "verified" ? "2026-08-09T12:00:00Z" : nil}.compact
  end
  write_yaml(root, "docs/work/progress-inventory.yaml", {"kind" => "execution_inventory", "version" => 1, "id" => "progress", "master_plan" => master_path, "state" => "active", "items" => items})
end

def complete_controls(root)
  controls = File.join(root, ".dora/controls")
  File.write(File.join(root, "docs/backlog.md"), "# Backlog\n")
  File.write(File.join(controls, "change-routing.yaml"), YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "commands" => ["test"]}]}))
  File.write(File.join(controls, "workspace-inventory.yaml"), YAML.dump({"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => "source", "path_prefixes" => ["src/"]}]}))
  File.write(File.join(controls, "documentation-evidence.yaml"), YAML.dump({"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "docs", "match" => "Backlog", "evidence" => ["docs/backlog.md"]}]}))
  File.write(File.join(controls, "system-map.yaml"), YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "source"}], "edges" => []}))
  File.write(File.join(controls, "backlog.yaml"), YAML.dump({"kind" => "dora_backlog", "version" => 1, "sources" => ["docs/backlog.md"]}))
end

def enable_work_artifact_audit(root)
  policy_path = File.join(root, ".dora/controls/artifact-policy.yaml")
  policy = YAML.load_file(policy_path)
  policy["work_artifact_audit"] = {"paths" => ["docs/work"]}
  File.write(policy_path, YAML.dump(policy))
end

def exclude_non_executable_record(root, path)
  policy_path = File.join(root, ".dora/controls/artifact-policy.yaml")
  policy = YAML.load_file(policy_path)
  policy.fetch("work_artifact_audit")["non_executable_records"] = [{"path" => path, "reason" => "Historical narrative record."}]
  File.write(policy_path, YAML.dump(policy))
end

Dir.mktmpdir("dora-project-read-model-clean") do |root|
  write_project(root)
  complete_controls(root)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "clean summary was not healthy" unless summary.fetch("state") == "HEALTHY" && summary.dig("health", "healthy") && summary.dig("integrity", "signals").empty?
  abort "clean current goal was not explicit" unless summary.fetch("current_goal") == {"state" => "none"}
  abort "latest delivery did not retain evidence timestamp" unless summary.dig("delivery", "latest_verified", "verified_at") == FIXTURE.fetch("latest_verified_at")
end

Dir.mktmpdir("dora-project-read-model") do |root|
  write_project(root)
  write_master_progress(root)
  model = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml"))
  summary = model.summary
  abort "summary lost project" unless summary.dig("project", "id") == FIXTURE.fetch("id") && summary.dig("project", "name") == FIXTURE.fetch("name")
  abort "summary lost verified delivery" unless summary.dig("delivery", "latest_verified", "id") == "delivery"
  abort "summary lost proposed decision" unless summary.fetch("open_decisions").any? { |item| item["source"] == "decision_log" }
  abort "summary duplicated a canonical open decision through project memory" unless summary.fetch("open_decisions").count { |item| item["statement"] == FIXTURE.fetch("open_decision") } == 1
  abort "summary leaked raw output" if summary.to_s.include?(FIXTURE.fetch("raw_output"))
  evidence = model.task_evidence("docs/work/delivery.yaml", FIXTURE.fetch("latest_task"))
  abort "evidence leaked output or unsafe path" if evidence.to_s.include?(FIXTURE.fetch("raw_output")) || evidence.to_s.include?(".env")
  abort "evidence lost safe runtime reference" unless evidence.fetch("runtime_evidence") == ["docs/runtime-evidence/delivery.json"]
  closeout = model.owner_delivery_closeout("docs/work/delivery.yaml")
  abort "owner closeout lost verified work evidence" unless closeout.dig("work", "id") == "delivery-work" && closeout.dig("completed_tasks", 0, "evidence", "status") == "passed" && closeout.fetch("read_only")
  abort "owner closeout lost relevant proposed decision" unless closeout.fetch("follow_up") == "owner_decision_required" && closeout.fetch("decisions").any? { |decision| decision["id"] == "retention-choice" }
  abort "owner closeout leaked raw output" if closeout.to_s.include?(FIXTURE.fetch("raw_output")) || closeout.to_s.include?("secret-command")
  progress = model.master_plan_progress("docs/work/progress-master.yaml")
  abort "canonical Master Plan progress lost its safe plan identity" unless progress.dig("master_plan", "id") == "progress-master" && progress.dig("master_plan", "title") == "Canonical progress"
  abort "canonical Master Plan progress did not preserve ordered canonical states" unless progress.fetch("items").map { |item| item.fetch("status") } == %w[verified in_progress pending blocked]
  abort "canonical Master Plan progress exposed unsafe plan data" if progress.to_s.include?(FIXTURE.fetch("raw_output")) || progress.to_s.include?("secret-command")
  begin
    model.plan("../../.env")
    abort "escaped plan path passed"
  rescue ArgumentError
    nil
  end
  File.symlink("/etc/hosts", File.join(root, "docs/work/escaped-plan.yaml"))
  begin
    model.plan("docs/work/escaped-plan.yaml")
    abort "symlinked plan escaped declared project root"
  rescue ArgumentError
    nil
  end
end

Dir.mktmpdir("dora-project-read-model-invalid-progress") do |root|
  write_project(root)
  write_master_progress(root, invalid_verified_evidence: true)
  model = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml"))
  begin
    model.master_plan_progress("docs/work/progress-master.yaml")
    abort "invalid Master Plan verification evidence produced progress"
  rescue ArgumentError
    nil
  end
end

Dir.mktmpdir("dora-project-read-model-invalid") do |root|
  write_project(root, invalid_memory: true)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "invalid memory was hidden" unless summary.fetch("inconsistencies").any? { |item| item["code"] == "project_memory" && item["severity"] == "INVALID" }
  abort "invalid memory hid verified delivery" unless summary.dig("delivery", "latest_verified", "id") == "delivery"
end

Dir.mktmpdir("dora-project-read-model-stale-memory") do |root|
  write_project(root, stale_memory: true)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "stale valid-shaped memory was hidden" unless summary.fetch("inconsistencies").any? { |item| item["code"] == "project_memory" && item["severity"] == "INVALID" }
  abort "stale memory hid verified delivery" unless summary.dig("delivery", "latest_verified", "id") == "delivery"
  abort "stale memory was not classified for Bridge readers" unless summary.dig("integrity", "signals").any? { |item| item["classification"] == "stale" }
end

Dir.mktmpdir("dora-project-read-model-ambiguous") do |root|
  write_project(root, ambiguous: true)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "ambiguous active work was guessed" unless summary.dig("delivery", "active", "status") == "ambiguous"
end

Dir.mktmpdir("dora-project-read-model-superseded") do |root|
  write_project(root)
  inventory = YAML.load_file(File.join(root, FIXTURE.fetch("latest_inventory")))
  inventory["id"] = "superseded-delivery"
  inventory["state"] = "superseded"
  inventory["items"] = [
    inventory.fetch("items").first.merge("id" => "stale-active", "status" => "in_progress"),
    inventory.fetch("items").first.merge("id" => "stale-verified", "status" => "verified", "verified_at" => "2099-01-01T00:00:00Z")
  ]
  write_yaml(root, "docs/work/superseded-inventory.yaml", inventory)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "superseded inventory resolved as active work" if summary.dig("delivery", "active")
  abort "superseded inventory replaced latest verified delivery" unless summary.dig("delivery", "latest_verified", "id") == "delivery"
end

Dir.mktmpdir("dora-project-read-model-control-reconciled") do |root|
  write_project(root)
  inventory = YAML.load_file(File.join(root, FIXTURE.fetch("latest_inventory")))
  inventory["id"] = "control-reconciliation"
  inventory["state"] = "control_reconciled"
  inventory["items"] = [inventory.fetch("items").first.merge("id" => "reconciliation", "verified_at" => "2099-01-01T00:00:00Z")]
  write_yaml(root, "docs/work/control-reconciliation-inventory.yaml", inventory)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "control reconciliation replaced latest verified delivery" unless summary.dig("delivery", "latest_verified", "id") == "delivery"
end

Dir.mktmpdir("dora-project-read-model-doctor-warning") do |root|
  write_project(root)
  complete_controls(root)
  enable_work_artifact_audit(root)
  work = YAML.load_file(File.join(root, "docs/work/delivery.yaml"))
  work["execution_inventory"] = "docs/work/warning-inventory.yaml"
  work["tasks"] = work.fetch("tasks").map { |task| task.merge("paths" => task.fetch("required_paths")) }
  write_yaml(root, "docs/work/warning-work.yaml", work.merge("id" => "warning-work"))
  write_yaml(root, "docs/work/warning-master.yaml", {"kind" => "master", "version" => 1, "id" => "warning-master", "title" => "Warning master", "status" => "verified", "children" => ["docs/work/warning-work.yaml"]})
  write_yaml(root, "docs/work/warning-inventory.yaml", {"kind" => "execution_inventory", "version" => 1, "id" => "warning", "master_plan" => "docs/work/warning-master.yaml", "state" => "active", "items" => [{"id" => "warning-task", "order" => 1, "plan" => "docs/work/warning-work.yaml", "task" => FIXTURE.fetch("latest_task"), "status" => "verified"}]})
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "doctor warning remained hidden behind Bridge health" unless summary.fetch("state") == "WARNING" && !summary.dig("health", "healthy") && summary.dig("health", "doctor_healthy")
  abort "doctor conflict did not reach Bridge integrity" unless summary.dig("integrity", "signals").any? { |item| item["code"] == "control_state_conflict" && item["classification"] == "conflict" }
end

Dir.mktmpdir("dora-project-read-model-non-executable-record") do |root|
  write_project(root)
  complete_controls(root)
  enable_work_artifact_audit(root)
  historical_path = File.join(root, "docs/work/historical-review.yaml")
  File.write(historical_path, "kind: [\n")
  exclude_non_executable_record(root, "docs/work/historical-review.yaml")
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "explicit non-executable historical record degraded safe context" unless summary.fetch("state") == "HEALTHY" && summary.dig("integrity", "signals").empty?
  abort "read model classification changed retained history" unless File.binread(historical_path) == "kind: [\n"
end

Dir.mktmpdir("dora-project-read-model-ambiguous-latest") do |root|
  write_project(root)
  original = YAML.load_file(File.join(root, "docs/work/delivery.yaml"))
  alternate_task = original.fetch("tasks").first.merge("id" => "verify-alternate")
  alternate_evidence = original.fetch("evidence").first.merge("task" => "verify-alternate")
  write_yaml(root, "docs/work/alternate.yaml", original.merge("id" => "alternate", "tasks" => [alternate_task], "evidence" => [alternate_evidence]))
  write_yaml(root, "docs/work/alternate-inventory.yaml", {"kind" => "execution_inventory", "version" => 1, "id" => "alternate", "master_plan" => FIXTURE.fetch("latest_master"), "state" => "verified", "items" => [{"id" => "alternate-proof", "order" => 1, "plan" => "docs/work/alternate.yaml", "task" => "verify-alternate", "status" => "verified", "verified_at" => FIXTURE.fetch("latest_verified_at")}]} )
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "ambiguous latest delivery was guessed" unless summary.dig("delivery", "latest_verified", "status") == "ambiguous"
  abort "ambiguous latest delivery was not signaled" unless summary.dig("integrity", "signals").any? { |item| item["code"] == "latest_verified_delivery" && item["classification"] == "ambiguous" }
end

puts "Dora project read model test passed (safe summary, evidence-backed delivery, integrity signals, invalid memory, supersession, ambiguity, and path containment)."
