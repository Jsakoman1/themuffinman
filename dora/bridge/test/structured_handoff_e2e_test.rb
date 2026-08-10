#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "tmpdir"
require "yaml"
require_relative "../../lib/dora/handoff"
require_relative "../../lib/dora/handoff_cli"
require_relative "../../lib/dora/handoff_runner"
require_relative "../../lib/dora/project_initializer"
require_relative "../lib/dora_bridge/server"

ROOT = File.expand_path("../..", __dir__)

def write_yaml(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def request(server, id, method, params = nil)
  payload = {"jsonrpc" => "2.0", "id" => id, "method" => method}
  payload["params"] = params if params
  server.handle(payload)
end

Dir.mktmpdir("dora-structured-handoff-e2e") do |root|
  project_root = File.join(root, "registered-project")
  Dora::ProjectInitializer.initialize!(project_root, project_id: "handoff-fixture", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  master = {"kind" => "master", "version" => 1, "id" => "handoff-delivery", "title" => "Verified delivery", "status" => "verified", "children" => ["docs/work/handoff-delivery.yaml"]}
  work = {"kind" => "work", "version" => 1, "id" => "handoff-delivery-work", "title" => "Delivery work", "status" => "verified", "baseline" => "pending", "tasks" => [{"id" => "verify-handoff", "title" => "Verify handoff", "status" => "done", "observable_outcome" => "Handoff evidence is available.", "dependencies" => [], "paths" => ["docs/handoff.md"], "required_paths" => ["docs/handoff.md"], "validation" => "true", "evidence_boundary" => ["fixture"]}], "evidence" => [{"task" => "verify-handoff", "result" => "passed", "ranAt" => "2026-08-10T12:00:00Z", "revision" => "abcdef1", "exitCode" => 0}]}
  write_yaml(project_root, "docs/work/handoff-delivery-master.yaml", master)
  write_yaml(project_root, "docs/work/handoff-delivery.yaml", work)
  registry_path = File.join(root, "projects.yaml")
  write_yaml(root, "projects.yaml", {"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "handoff-fixture", "adapter_path" => File.join(project_root, ".dora/project.yaml"), "capabilities" => {"handoff_write" => true}}]})
  registry = DoraBridge::ProjectRegistry.load!(registry_path)

  v1_server = DoraBridge::Server.new(registry)
  v1_tools = request(v1_server, 1, "tools/list").dig("result", "tools").map { |tool| tool.fetch("name") }
  abort "V1 tools changed when V2 is disabled" unless v1_tools.sort == DoraBridge::Server::TOOL_DEFINITIONS.map(&:first).sort

  state_root = File.join(root, "private-handoff-state")
  handoff_store = Dora::Handoff.new(state_root: state_root)
  server = DoraBridge::Server.new(registry, handoff_store: handoff_store)
  brief = {"request_mode" => "implementation", "expected_result" => "Evidence-backed completion.", "locked_decisions" => ["Owner gates Codex."], "non_goals" => ["No remote execution."], "context" => ["Synthetic fixture."], "stop_conditions" => ["Stop for missing owner decisions."], "verification" => ["Read completed evidence."], "open_decisions" => []}
  create_arguments = {"project" => "handoff-fixture", "title" => "Synthetic ChatGPT handoff", "objective" => "Prove the owner-gated structured handoff flow.", "acceptance_criteria" => ["The handoff is completed with evidence."], "constraints" => ["No remote execution."], "references" => ["docs/work/handoff-delivery.yaml"], "brief" => brief, "client_request_id" => "synthetic-chatgpt-request"}
  created = request(server, 2, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments}).dig("result", "structuredContent")
  abort "synthetic MCP create failed" unless created.fetch("status") == "READY"
  abort "handoff was not persisted outside the project" unless File.file?(File.join(state_root, "handoff-events.jsonl")) && !File.exist?(File.join(project_root, "handoff-events.jsonl"))
  listed = request(server, 3, "tools/call", {"name" => "list_handoffs", "arguments" => {"project" => "handoff-fixture"}}).dig("result", "structuredContent")
  abort "read side did not show one READY handoff" unless listed.fetch("handoffs").length == 1 && listed.fetch("handoffs").first.fetch("status") == "READY"

  next_handoff = Dora::HandoffCLI.run!(command: "next", registry_path: registry_path, state_root: state_root, project: "handoff-fixture")
  abort "Codex-equivalent consumer did not retrieve the handoff" unless next_handoff.fetch("id") == created.fetch("id")
  claimed = Dora::HandoffCLI.run!(command: "claim", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"))
  abort "handoff claim failed" unless claimed.fetch("status") == "CLAIMED"
  begin
    Dora::HandoffCLI.run!(command: "claim", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"))
    abort "double claim passed"
  rescue ArgumentError
    nil
  end
  Dora::HandoffCLI.run!(command: "feedback", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"), feedback: {"phase" => "DISCOVERY", "milestone" => "Verified V2.1 baseline", "progress" => "Authoritative lifecycle identified", "finding" => "Reuse the existing append-only event stream", "verification_state" => "NOT_STARTED", "deviations" => [], "residual_risks" => []})
  Dora::HandoffCLI.run!(command: "feedback", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"), feedback: {"phase" => "IMPLEMENTING", "milestone" => "Implemented lifecycle projection", "progress" => "Bounded feedback is available", "finding" => "No parallel status store is needed", "verification_state" => "IMPLEMENTED_UNVERIFIED", "deviations" => [], "residual_risks" => []})
  Dora::HandoffCLI.run!(command: "feedback", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"), feedback: {"phase" => "VERIFYING", "milestone" => "Ran synthetic proof", "progress" => "Verification is in progress", "finding" => "Completion still requires Dora evidence", "verification_state" => "VERIFICATION_IN_PROGRESS", "deviations" => [], "residual_risks" => []})
  Dora::HandoffCLI.run!(command: "link", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"), master_plan: "docs/work/handoff-delivery-master.yaml", work_plan: "docs/work/handoff-delivery.yaml")
  completion_result = {"work_performed" => ["Created, claimed, progressed, and linked the synthetic handoff"], "decisions" => ["Reuse normal Dora verification evidence"], "acceptance_results" => ["Synthetic safe flow completed"], "deviations" => [], "residual_risks" => ["Real owner runtime remains separately gated"], "follow_up" => {"needed" => false, "reason" => nil}}
  completed = Dora::HandoffCLI.run!(command: "complete", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: created.fetch("id"), verification_plan: "docs/work/handoff-delivery.yaml", verification_task: "verify-handoff", completion_result: completion_result)
  abort "handoff completion lost evidence" unless completed.fetch("status") == "COMPLETED" && completed.dig("completion", "verification_references") == ["docs/work/handoff-delivery.yaml#verify-handoff"] && completed.dig("completion", "result", "work_performed") == completion_result.fetch("work_performed")
  final = request(server, 4, "tools/call", {"name" => "get_handoff_status", "arguments" => {"project" => "handoff-fixture", "handoff_id" => created.fetch("id")}}).dig("result", "structuredContent")
  abort "MCP final readback is incomplete" unless final.fetch("status") == "COMPLETED" && final.dig("collaboration", "verification_state") == "VERIFIED" && final.fetch("progress") == {"phase" => "COMPLETED", "label" => "Completed", "summary" => "Handoff completed with Dora verification evidence.", "material_change_token" => "COMPLETED:COMPLETED:VERIFIED"} && final.dig("completion", "verification_references") == ["docs/work/handoff-delivery.yaml#verify-handoff"] && final.dig("completion", "result", "follow_up", "needed") == false
  lifecycle_final = request(server, 41, "tools/call", {"name" => "get_handoff_lifecycle_readback", "arguments" => {"project" => "handoff-fixture", "handoff_id" => created.fetch("id")}}).dig("result", "structuredContent")
  abort "lifecycle-only completion readback is incomplete" unless lifecycle_final == {"handoff_id" => created.fetch("id"), "status" => "COMPLETED", "outcome_category" => "COMPLETED", "verification" => {"state" => "VERIFIED", "evidence_references" => ["docs/work/handoff-delivery.yaml#verify-handoff"]}, "progress" => {"phase" => "COMPLETED", "label" => "Completed", "summary" => "Handoff completed with Dora verification evidence.", "material_change_token" => "COMPLETED:COMPLETED:VERIFIED"}, "summary" => "Handoff completed with Dora verification evidence."}
  abort "lifecycle-only completion readback leaked immutable content" if lifecycle_final.to_s.include?(completion_result.fetch("work_performed").first) || lifecycle_final.to_s.include?(state_root)
  decision_brief = brief.merge("request_mode" => "owner_decision_required", "expected_result" => "A documented owner decision.", "verification" => [], "open_decisions" => ["Which product wording is approved?"])
  decision_arguments = create_arguments.merge("title" => "Synthetic owner decision", "objective" => "Prove an explicit owner-decision stop.", "brief" => decision_brief, "client_request_id" => "synthetic-owner-decision")
  decision_handoff = request(server, 7, "tools/call", {"name" => "create_handoff", "arguments" => decision_arguments}).dig("result", "structuredContent")
  Dora::HandoffCLI.run!(command: "claim", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: decision_handoff.fetch("id"))
  owner_decision = {"question" => "Which product wording is approved?", "why" => "Codex cannot infer owner-facing product language.", "options" => ["Concise wording", "Detailed wording"], "recommendation" => "Concise wording", "blocked_work" => ["Owner-facing copy"], "may_continue" => ["Regression tests"]}
  Dora::HandoffCLI.run!(command: "block_owner_decision", registry_path: registry_path, state_root: state_root, project: "handoff-fixture", id: decision_handoff.fetch("id"), owner_decision: owner_decision)
  decision_final = request(server, 8, "tools/call", {"name" => "get_handoff_status", "arguments" => {"project" => "handoff-fixture", "handoff_id" => decision_handoff.fetch("id")}}).dig("result", "structuredContent")
  abort "owner-decision readback is incomplete" unless decision_final.fetch("status") == "BLOCKED" && decision_final.dig("blocked", "owner_decision") == owner_decision
  abort "final readback leaked local state" if final.to_s.include?(state_root) || final.to_s.include?(root)
  v2_names = request(server, 5, "tools/list").dig("result", "tools").map { |tool| tool.fetch("name") }
  abort "generic remote execution capability exists" if v2_names.any? { |name| name.match?(/shell|exec|file|patch|git|process|codex/) }
  list_definition = request(server, 6, "tools/list").dig("result", "tools").find { |tool| tool.fetch("name") == "list_handoffs" }
  abort "V2 handoff structured output schema is missing" unless list_definition.dig("outputSchema", "type") == "object"

  runner_handoff = request(server, 9, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments.merge("title" => "Synthetic runner handoff", "objective" => "Prove local runner completion.", "client_request_id" => "synthetic-local-runner")}).dig("result", "structuredContent")
  launcher = File.join(root, "fixed-local-codex-both")
  completion_source = <<~RUBY
    require #{File.join(ROOT, "lib/dora/handoff_cli").inspect}
    handoff_id = ARGV.fetch(1)
    registry = #{registry_path.inspect}
    state_root = #{state_root.inspect}
    project = Dora::Handoff.new(state_root: state_root).get!(id: handoff_id).fetch("project")
    Dora::HandoffCLI.run!(command: "link", registry_path: registry, state_root: state_root, project: project, id: handoff_id, master_plan: "docs/work/handoff-delivery-master.yaml", work_plan: "docs/work/handoff-delivery.yaml")
    result = {"work_performed" => ["Completed through the fixed local runner contract"], "decisions" => ["Readback remains V2.2"], "acceptance_results" => ["Synthetic runner completion is evidence-gated"], "deviations" => [], "residual_risks" => [], "follow_up" => {"needed" => false, "reason" => nil}}
    Dora::HandoffCLI.run!(command: "complete", registry_path: registry, state_root: state_root, project: project, id: handoff_id, verification_plan: "docs/work/handoff-delivery.yaml", verification_task: "verify-handoff", completion_result: result)
  RUBY
  File.write(launcher, "#!/usr/bin/env ruby\n#{completion_source}")
  FileUtils.chmod("u+x", launcher)
  runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["handoff-fixture"], launcher: launcher)
  runner_result = runner.run_once
  runner_final = request(server, 10, "tools/call", {"name" => "get_handoff_status", "arguments" => {"project" => "handoff-fixture", "handoff_id" => runner_handoff.fetch("id")}}).dig("result", "structuredContent")
  abort "runner completion/readback proof failed" unless runner_result.fetch("status") == "COMPLETED" && runner_final.fetch("status") == "COMPLETED" && runner_final.dig("completion", "verification_references") == ["docs/work/handoff-delivery.yaml#verify-handoff"]
end

puts "Dora structured handoff end-to-end test passed (synthetic MCP create, claim, fixed local runner completion, milestone feedback, owner-decision readback, V1 regression, and no execution tool)."
