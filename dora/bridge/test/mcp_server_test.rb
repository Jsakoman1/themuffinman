#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "stringio"
require "tmpdir"
require "yaml"

require_relative "../../lib/dora/project_initializer"
require_relative "../lib/dora_bridge/server"

ROOT = File.expand_path("../..", __dir__)

def write_yaml(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def create_project(root)
  Dora::ProjectInitializer.initialize!(root, project_id: "doomsday-storage", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief_path = File.join(root, "docs/product-brief.yaml")
  brief = YAML.load_file(brief_path)
  brief["unanswered_decisions"] = []
  File.write(brief_path, YAML.dump(brief))
  master = {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Verified delivery", "status" => "verified", "children" => ["docs/work/delivery.yaml"]}
  plan = {"kind" => "work", "version" => 1, "id" => "delivery-work", "title" => "Delivery work", "status" => "verified", "baseline" => "pending", "tasks" => [{"id" => "verify-delivery", "title" => "Verify delivery", "status" => "done", "observable_outcome" => "A delivery is verified.", "dependencies" => [], "paths" => ["docs/delivery.md"], "required_paths" => ["docs/delivery.md"], "validation" => "safe-command", "evidence_boundary" => ["fixture"]}], "evidence" => [{"task" => "verify-delivery", "result" => "passed", "ranAt" => "2026-08-09T15:00:00Z", "revision" => "abcdef1", "exitCode" => 0, "output" => "/private/secret/raw-command-output"}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "id" => "delivery", "master_plan" => "docs/work/delivery-master.yaml", "state" => "verified", "items" => [{"id" => "delivery-proof", "order" => 1, "plan" => "docs/work/delivery.yaml", "task" => "verify-delivery", "status" => "verified", "verified_at" => "2026-08-09T15:00:00Z"}]}
  write_yaml(root, "docs/work/delivery-master.yaml", master)
  write_yaml(root, "docs/work/delivery.yaml", plan)
  write_yaml(root, "docs/work/delivery-inventory.yaml", inventory)
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
  path = File.join(root, ".dora/controls/artifact-policy.yaml")
  policy = YAML.load_file(path)
  policy["work_artifact_audit"] = {"paths" => ["docs/work"]}
  File.write(path, YAML.dump(policy))
end

def exclude_non_executable_record(root, path)
  policy_path = File.join(root, ".dora/controls/artifact-policy.yaml")
  policy = YAML.load_file(policy_path)
  policy.fetch("work_artifact_audit")["non_executable_records"] = [{"path" => path, "reason" => "Historical narrative record."}]
  File.write(policy_path, YAML.dump(policy))
end

def request(server, id, method, params = nil)
  payload = {"jsonrpc" => "2.0", "id" => id, "method" => method}
  payload["params"] = params if params
  server.handle(payload)
end

Dir.mktmpdir("dora-bridge-mcp") do |root|
  project_root = File.join(root, "project")
  create_project(project_root)
  complete_controls(project_root)
  registry_path = File.join(root, "bridge-projects.yaml")
  write_yaml(root, "bridge-projects.yaml", {"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "doomsday-storage", "name" => "DoomsDayStorage", "adapter_path" => "project/.dora/project.yaml", "capabilities" => {"handoff_write" => true}}, {"id" => "unreachable-project", "adapter_path" => "missing/.dora/project.yaml"}]})
  registry = DoraBridge::ProjectRegistry.load!(registry_path)
  server = DoraBridge::Server.new(registry)

  initialize = request(server, 1, "initialize", {"protocolVersion" => "2025-06-18"})
  abort "initialize failed" unless initialize.dig("result", "capabilities", "tools")
  tool_names = request(server, 2, "tools/list").dig("result", "tools").map { |tool| tool.fetch("name") }
  abort "write or shell tool is exposed" unless tool_names.sort == DoraBridge::Server::TOOL_DEFINITIONS.map(&:first).sort && tool_names.none? { |name| name.match?(/work|verify|shell|file|commit|push/) }
  listed = request(server, 3, "tools/call", {"name" => "list_projects", "arguments" => {}})
  abort "allow-list leaked a filesystem root" if listed.to_s.include?(root)
  abort "allow-list was incomplete" unless listed.dig("result", "structuredContent", "projects").map { |entry| entry.fetch("id") } == %w[doomsday-storage unreachable-project]
  unknown = request(server, 4, "tools/call", {"name" => "get_project_summary", "arguments" => {"project" => "unknown-project"}})
  abort "unknown project was not rejected before an unreachable entry could matter" unless unknown.dig("error", "code") == -32001 && !unknown.to_s.include?("missing")
  abort "explicit handoff capability was not available" unless registry.handoff_authorized!("doomsday-storage").fetch("id") == "doomsday-storage"
  begin
    registry.handoff_authorized!("unreachable-project")
    abort "readable project unexpectedly received handoff write"
  rescue ArgumentError => error
    abort "wrong disabled capability error" unless error.message.include?("disabled")
  end
  begin
    registry.handoff_authorized!("unknown-project")
    abort "unknown handoff project was not rejected"
  rescue ArgumentError => error
    abort "unknown project leaked a path" if error.message.include?("missing")
  end
  summary = request(server, 5, "tools/call", {"name" => "get_project_summary", "arguments" => {"project" => "doomsday-storage"}})
  output = summary.dig("result", "structuredContent")
  abort "summary did not delegate to read model" unless output.fetch("kind") == "dora_project_read_model" && output.dig("delivery", "latest_verified", "id") == "delivery"
  abort "summary lost the explicit no-current-goal result" unless output.fetch("current_goal") == {"state" => "none"} && output.dig("integrity", "status") == "HEALTHY"
  abort "summary leaked a root or raw evidence" if output.to_s.include?(root) || output.to_s.include?("raw-command-output")
  idc_envelope = request(server, 55, "tools/call", {"name" => "get_idc_envelope", "arguments" => {"project" => "doomsday-storage"}}).dig("result", "structuredContent")
  abort "IDC Bridge profile did not return the fixed read-only envelope" unless idc_envelope.fetch("kind") == "dora_idc_read_envelope" && idc_envelope.fetch("read_only") == true && idc_envelope.fetch("disposition") == "advisory"
  abort "IDC Bridge profile accepted caller-selected context" unless idc_envelope.fetch("selection") == {"project_fields" => DoraBridge::Server::IDC_ENVELOPE_SELECTION.fetch("project_fields").sort, "decision_ids" => [], "artifact_references" => []}
  abort "IDC Bridge profile leaked a root or raw evidence" if idc_envelope.to_s.include?(root) || idc_envelope.to_s.include?("raw-command-output")
  rejected_idc_path = request(server, 56, "tools/call", {"name" => "get_idc_envelope", "arguments" => {"project" => "doomsday-storage", "path" => "/tmp/pwn"}})
  abort "IDC Bridge profile accepted a caller path" unless rejected_idc_path.dig("error", "code") == -32602
  triage_request = {"kind" => "dora_idc_triage_request", "version" => 1, "id" => "bridge-idc", "request_shape" => "wide_research", "profile" => "research_dossier", "source_scope" => "explicit_owner_selected_only", "authorization_scope" => "current_request_only", "owner_authorization" => "not_granted"}
  triage = request(server, 57, "tools/call", {"name" => "evaluate_idc_triage", "arguments" => {"project" => "doomsday-storage", "triage_request" => triage_request}}).dig("result", "structuredContent")
  abort "IDC Bridge triage did not retain the owner confirmation gate" unless triage.values_at("kind", "read_only", "disposition", "outcome", "profile", "owner_confirmation_required") == ["dora_idc_triage_readback", true, "advisory", "IDC_OWNER_CONFIRMATION_REQUIRED", "research_dossier", true]
  abort "IDC Bridge triage leaked a root or request prose" if triage.to_s.include?(root) || triage.to_s.include?(triage_request.fetch("id"))
  authorized_triage = request(server, 58, "tools/call", {"name" => "evaluate_idc_triage", "arguments" => {"project" => "doomsday-storage", "triage_request" => triage_request.merge("owner_authorization" => "authorize_local_idc_render")}}).dig("result", "structuredContent")
  abort "IDC Bridge triage did not return the local-only advisory outcome" unless authorized_triage.fetch("outcome") == "IDC_OWNER_AUTHORIZED_LOCAL_RENDER" && authorized_triage.fetch("next_action").include?("local owner/Codex")
  rejected_triage_path = request(server, 59, "tools/call", {"name" => "evaluate_idc_triage", "arguments" => {"project" => "doomsday-storage", "triage_request" => triage_request, "path" => "/tmp/pwn"}})
  abort "IDC Bridge triage accepted a caller path" unless rejected_triage_path.dig("error", "code") == -32602
  malformed_triage = request(server, 60, "tools/call", {"name" => "evaluate_idc_triage", "arguments" => {"project" => "doomsday-storage", "triage_request" => triage_request.merge("source_scope" => "automatic_selection")}})
  abort "IDC Bridge triage accepted automatic source selection" unless malformed_triage.dig("error", "code") == -32001
  enable_work_artifact_audit(project_root)
  File.write(File.join(project_root, "docs/work", "narrative-review.yaml"), "kind: [\n")
  warning_summary = request(server, 50, "tools/call", {"name" => "get_project_summary", "arguments" => {"project" => "doomsday-storage"}}).dig("result", "structuredContent")
  abort "Bridge hid a local Dora advisory" unless warning_summary.fetch("state") == "WARNING" && !warning_summary.dig("health", "healthy") && warning_summary.dig("integrity", "signals").any? { |signal| signal["code"] == "doctor_advisory" }
  exclude_non_executable_record(project_root, "docs/work/narrative-review.yaml")
  classified_summary = request(server, 501, "tools/call", {"name" => "get_project_summary", "arguments" => {"project" => "doomsday-storage"}}).dig("result", "structuredContent")
  abort "Bridge treated an explicitly non-executable historical record as work" unless classified_summary.fetch("state") == "HEALTHY" && classified_summary.dig("integrity", "signals").empty?
  abort "Bridge classification changed retained history" unless File.binread(File.join(project_root, "docs/work/narrative-review.yaml")) == "kind: [\n"
  intent_proposal = {"intent_plan_id" => "bridge-intent", "intended_outcome" => "Safely align one proposal.", "in_scope_work" => ["Evaluate one proposal."], "non_goals" => ["Do not persist an Intent Plan."], "fixed_owner_decisions" => [], "candidate_slices" => [{"id" => "first-slice", "outcome" => "Prepare Dora work.", "depends_on" => [], "gates" => ["no_owner_decision_pending"]}, {"id" => "later-slice", "outcome" => "Wait for verification.", "depends_on" => ["first-slice"], "gates" => ["no_owner_decision_pending", "prior_slice_verification"]}], "required_owner_readback" => %w[phase alignment_result first_safe_next_action actionable_blocker_or_decision]}
  intent_alignment = request(server, 51, "tools/call", {"name" => "align_intent_plan", "arguments" => {"project" => "doomsday-storage", "proposal" => intent_proposal}}).dig("result", "structuredContent")
  abort "intent alignment did not reconcile against verified Dora state" unless intent_alignment.fetch("alignment_result") == "RECONCILED" && intent_alignment.fetch("first_eligible_slice") == {"id" => "first-slice", "status" => "ELIGIBLE"} && intent_alignment.fetch("later_slices") == [{"id" => "later-slice", "status" => "BLOCKED_PENDING_DORA_VERIFICATION"}]
  secret_intent = "/private/intent-source"
  intent_redaction = request(server, 52, "tools/call", {"name" => "align_intent_plan", "arguments" => {"project" => "doomsday-storage", "proposal" => intent_proposal.merge("intended_outcome" => secret_intent)}}).dig("result", "structuredContent")
  abort "intent alignment leaked proposal content" if intent_redaction.to_s.include?(secret_intent) || intent_redaction.to_s.include?(root)
  invalid_intent = request(server, 53, "tools/call", {"name" => "align_intent_plan", "arguments" => {"project" => "doomsday-storage", "proposal" => intent_proposal.reject { |key, _value| key == "non_goals" }}}).dig("result", "structuredContent")
  abort "invalid Intent Plan was not held for owner decision" unless invalid_intent.fetch("alignment_result") == "OWNER_DECISION_NEEDED" && invalid_intent.fetch("first_eligible_slice").nil?
  rejected_intent_path = request(server, 54, "tools/call", {"name" => "align_intent_plan", "arguments" => {"project" => "doomsday-storage", "proposal" => intent_proposal, "path" => "/tmp/pwn"}})
  abort "Intent Plan tool accepted a caller path" unless rejected_intent_path.dig("error", "code") == -32602
  evidence = request(server, 6, "tools/call", {"name" => "get_task_evidence", "arguments" => {"project" => "doomsday-storage", "plan" => "docs/work/delivery.yaml", "task" => "verify-delivery"}})
  abort "sanitized evidence was unavailable" unless evidence.dig("result", "structuredContent", "status") == "passed" && !evidence.to_s.include?("raw-command-output")
  escaped = request(server, 7, "tools/call", {"name" => "get_plan", "arguments" => {"project" => "doomsday-storage", "plan" => "../../.env"}})
  abort "path escape was not rejected" unless escaped.dig("error", "code") == -32001
  File.symlink("/etc/hosts", File.join(project_root, "docs/work/escaped-plan.yaml"))
  symlink_escape = request(server, 71, "tools/call", {"name" => "get_plan", "arguments" => {"project" => "doomsday-storage", "plan" => "docs/work/escaped-plan.yaml"}})
  abort "symlink path escape was not rejected" unless symlink_escape.dig("error", "code") == -32001 && !symlink_escape.to_s.include?("/etc/hosts")
  write_attempt = request(server, 8, "tools/call", {"name" => "work-start", "arguments" => {"project" => "doomsday-storage"}})
  abort "write attempt was accepted" unless write_attempt.dig("error", "code") == -32601
  wire = StringIO.new
  server.run(input: StringIO.new(JSON.generate({"jsonrpc" => "2.0", "id" => 9, "method" => "tools/list"}) + "\n"), output: wire)
  abort "stdio JSON-RPC framing failed" unless JSON.parse(wire.string).dig("result", "tools")

  state_root = File.join(root, "private-handoff-state")
  handoff_store = Dora::Handoff.new(state_root: state_root)
  handoff_server = DoraBridge::Server.new(registry, handoff_store: handoff_store)
  handoff_tools = request(handoff_server, 10, "tools/list").dig("result", "tools")
  handoff_names = handoff_tools.map { |tool| tool.fetch("name") }
  abort "V2 tools were not exposed" unless %w[create_handoff list_handoffs get_handoff get_next_handoff get_handoff_status get_handoff_lifecycle_readback].all? { |name| handoff_names.include?(name) }
  abort "V2 exposed a generic execution surface" if handoff_names.any? { |name| name.match?(/shell|exec|file|patch|git|work-start|work-verify|codex/) }
  brief = {"request_mode" => "implementation", "expected_result" => "A ready handoff.", "locked_decisions" => ["Use the local queue."], "non_goals" => ["No shell access."], "context" => ["Synthetic fixture."], "stop_conditions" => ["Stop for missing owner decisions."], "verification" => ["Inspect one ready handoff."], "open_decisions" => []}
  create_arguments = {"project" => "doomsday-storage", "title" => "Synthetic structured handoff", "objective" => "Prove the local handoff queue without source access.", "acceptance_criteria" => ["One handoff is ready."], "constraints" => ["No shell access."], "references" => ["docs/work/delivery.yaml"], "brief" => brief, "client_request_id" => "mcp-request-1"}
  created = request(handoff_server, 11, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments}).dig("result", "structuredContent")
  abort "V2 creation did not yield READY" unless created && created.fetch("status") == "READY"
  abort "V2 output leaked a private state path" if created.to_s.include?(state_root) || created.to_s.include?(root)
  repeated = request(handoff_server, 12, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments}).dig("result", "structuredContent")
  abort "MCP retry duplicated a handoff" unless repeated.fetch("id") == created.fetch("id")
  conflict = request(handoff_server, 13, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments.merge("title" => "Conflicting retry")})
  abort "conflicting idempotency key passed" unless conflict.dig("error", "code") == -32001 && !conflict.to_s.include?(state_root)
  malformed = request(handoff_server, 14, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments.merge("path" => "/tmp/pwn")})
  abort "caller-selected path was accepted" unless malformed.dig("error", "code") == -32602
  escaped_create = request(handoff_server, 15, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments.merge("client_request_id" => "mcp-request-escape", "references" => ["../../.env"])})
  abort "handoff path traversal was accepted" unless escaped_create.dig("error", "code") == -32001
  oversized = request(handoff_server, 16, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments.merge("client_request_id" => "mcp-request-large", "objective" => "x" * 8_001)})
  abort "oversized handoff payload was accepted" unless oversized.dig("error", "code") == -32001
  listed_handoffs = request(handoff_server, 17, "tools/call", {"name" => "list_handoffs", "arguments" => {"project" => "doomsday-storage"}}).dig("result", "structuredContent")
  abort "handoff list was incorrect" unless listed_handoffs.fetch("handoffs").length == 1 && listed_handoffs.fetch("handoffs").first.fetch("id") == created.fetch("id")
  next_handoff = request(handoff_server, 18, "tools/call", {"name" => "get_next_handoff", "arguments" => {"project" => "doomsday-storage"}}).dig("result", "structuredContent")
  abort "next handoff did not resolve deterministically" unless next_handoff.fetch("handoff").fetch("id") == created.fetch("id")
  handoff_store.claim!(id: created.fetch("id"), project: "doomsday-storage", claimed_by: "codex")
  feedback = {"phase" => "VERIFYING", "milestone" => "MCP projection regression", "progress" => "Existing status read is extended", "finding" => "No extra tool is needed", "verification_state" => "VERIFICATION_IN_PROGRESS", "deviations" => [], "residual_risks" => ["V1 compatibility must remain green"]}
  handoff_store.feedback!(id: created.fetch("id"), project: "doomsday-storage", feedback: feedback)
  status_readback = request(handoff_server, 21, "tools/call", {"name" => "get_handoff_status", "arguments" => {"project" => "doomsday-storage", "handoff_id" => created.fetch("id")}}).dig("result", "structuredContent")
  expected_verifying_progress = {"phase" => "VERIFICATION", "label" => "Verification", "summary" => "Handoff verification is in progress.", "material_change_token" => "CLAIMED:VERIFICATION:VERIFICATION_IN_PROGRESS"}
  abort "current feedback was not projected" unless status_readback.dig("collaboration", "phase") == "VERIFYING" && status_readback.dig("collaboration", "finding") == "No extra tool is needed" && status_readback.fetch("progress") == expected_verifying_progress
  abort "feedback projection exposed a terminal stream" if status_readback.to_s.include?("raw-command-output") || status_readback.to_s.include?(state_root)
  verification_failed = handoff_store.create!(project: "doomsday-storage", title: "Verification failed", objective: "Prove a safe lifecycle failure readback.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "mcp-verification-failed")
  handoff_store.claim!(id: verification_failed.fetch("id"), project: "doomsday-storage", claimed_by: "codex")
  handoff_store.feedback!(id: verification_failed.fetch("id"), project: "doomsday-storage", feedback: feedback.merge("milestone" => "Verification failed", "verification_state" => "VERIFICATION_FAILED"))
  failed_readback = request(handoff_server, 23, "tools/call", {"name" => "get_handoff_lifecycle_readback", "arguments" => {"project" => "doomsday-storage", "handoff_id" => verification_failed.fetch("id")}}).dig("result", "structuredContent")
  abort "verification failure lifecycle readback is incomplete" unless failed_readback.fetch("handoff_id") == verification_failed.fetch("id") && failed_readback.fetch("status") == "CLAIMED" && failed_readback.fetch("outcome_category") == "VERIFICATION_FAILED" && failed_readback.dig("verification", "state") == "VERIFICATION_FAILED" && failed_readback.dig("progress", "material_change_token") == "CLAIMED:VERIFICATION:VERIFICATION_FAILED"
  decision_handoff = handoff_store.create!(project: "doomsday-storage", title: "Decision readback", objective: "Prove structured decision readback.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "mcp-owner-decision")
  handoff_store.claim!(id: decision_handoff.fetch("id"), project: "doomsday-storage", claimed_by: "codex")
  owner_decision = {"question" => "Which safe read contract is preferred?", "why" => "The owner must choose the public product contract.", "options" => ["Extend status", "Create a new tool"], "recommendation" => "Extend status", "blocked_work" => ["Public projection"], "may_continue" => ["Local tests"]}
  handoff_store.block_owner_decision!(id: decision_handoff.fetch("id"), project: "doomsday-storage", owner_decision: owner_decision)
  decision_readback = request(handoff_server, 22, "tools/call", {"name" => "get_handoff_status", "arguments" => {"project" => "doomsday-storage", "handoff_id" => decision_handoff.fetch("id")}}).dig("result", "structuredContent")
  abort "owner decision was not safely projected" unless decision_readback.fetch("status") == "BLOCKED" && decision_readback.dig("blocked", "owner_decision", "may_continue") == ["Local tests"]
  blocked_lifecycle = request(handoff_server, 24, "tools/call", {"name" => "get_handoff_lifecycle_readback", "arguments" => {"project" => "doomsday-storage", "handoff_id" => decision_handoff.fetch("id")}}).dig("result", "structuredContent")
  abort "blocked lifecycle readback was incomplete or leaked owner decision content" unless blocked_lifecycle.fetch("status") == "BLOCKED" && blocked_lifecycle.fetch("outcome_category") == "BLOCKED" && blocked_lifecycle.dig("progress", "phase") == "BLOCKED" && !blocked_lifecycle.to_s.include?(owner_decision.fetch("question"))
  unavailable_lifecycle = request(handoff_server, 25, "tools/call", {"name" => "get_handoff_lifecycle_readback", "arguments" => {"project" => "unreachable-project", "handoff_id" => "handoff-not-a-valid-id"}}).dig("result", "structuredContent")
  abort "invalid or unauthorized lifecycle readback did not fail closed" unless unavailable_lifecycle == {"status" => "UNAVAILABLE", "outcome_category" => "UNAVAILABLE", "verification" => {"state" => "UNAVAILABLE", "evidence_references" => []}, "summary" => "Handoff lifecycle readback is unavailable."}
  unknown_lifecycle = request(handoff_server, 251, "tools/call", {"name" => "get_handoff_lifecycle_readback", "arguments" => {"project" => "doomsday-storage", "handoff_id" => "handoff-00000000-0000-4000-8000-000000000000"}}).dig("result", "structuredContent")
  abort "unknown lifecycle readback did not use the safe unavailable envelope" unless unknown_lifecycle == unavailable_lifecycle
  malformed_lifecycle = request(handoff_server, 26, "tools/call", {"name" => "get_handoff_lifecycle_readback", "arguments" => {"project" => "doomsday-storage", "handoff_id" => created.fetch("id"), "path" => state_root}}).dig("result", "structuredContent")
  abort "malformed lifecycle readback did not use the safe unavailable envelope" unless malformed_lifecycle == unavailable_lifecycle && !malformed_lifecycle.to_s.include?(state_root)
  disabled_write = request(handoff_server, 19, "tools/call", {"name" => "create_handoff", "arguments" => create_arguments.merge("project" => "unreachable-project", "client_request_id" => "disabled-project")})
  abort "disabled project write was accepted" unless disabled_write.dig("error", "code") == -32001 && !disabled_write.to_s.include?("missing")
  cross_project = request(handoff_server, 20, "tools/call", {"name" => "get_handoff", "arguments" => {"project" => "unreachable-project", "handoff_id" => created.fetch("id")}})
  abort "cross-project handoff access was accepted" unless cross_project.dig("error", "code") == -32001
end

puts "Dora Bridge MCP test passed (V1 read compatibility, V2 gating, lifecycle-only readback, containment, idempotency, and sanitized output)."
