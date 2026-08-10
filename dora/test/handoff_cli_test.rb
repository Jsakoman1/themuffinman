#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/handoff"
require_relative "../lib/dora/handoff_cli"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)

def write_yaml(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def run_cli(*arguments)
  output, status = Open3.capture2e(RbConfig.ruby, File.join(ROOT, "bin/dora"), *arguments, chdir: ROOT)
  [output, status]
end

Dir.mktmpdir("dora-handoff-cli") do |root|
  project_root = File.join(root, "project")
  Dora::ProjectInitializer.initialize!(project_root, project_id: "dora", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  master = {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Verified delivery", "status" => "verified", "children" => ["docs/work/delivery.yaml"]}
  plan = {"kind" => "work", "version" => 1, "id" => "delivery-work", "title" => "Delivery work", "status" => "verified", "baseline" => "pending", "tasks" => [{"id" => "verify", "title" => "Verify", "status" => "done", "observable_outcome" => "Verified.", "dependencies" => [], "paths" => ["docs/verified.md"], "required_paths" => ["docs/verified.md"], "validation" => "true", "evidence_boundary" => ["fixture"]}], "evidence" => [{"task" => "verify", "result" => "passed", "ranAt" => "2026-08-10T12:00:00Z", "revision" => "abcdef1", "exitCode" => 0}]}
  write_yaml(project_root, "docs/work/delivery-master.yaml", master)
  write_yaml(project_root, "docs/work/delivery.yaml", plan)
  registry_path = File.join(root, "projects.yaml")
  write_yaml(root, "projects.yaml", {"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "dora", "adapter_path" => File.join(project_root, ".dora/project.yaml"), "capabilities" => {"handoff_write" => true}}]})
  state_root = File.join(root, "private-state")
  handoff = Dora::Handoff.new(state_root: state_root).create!(project: "dora", title: "CLI handoff", objective: "Use the local Dora command boundary.", acceptance_criteria: ["CLI completes it."], constraints: ["No direct file edits."], references: ["docs/work/delivery.yaml"], created_by: "chatgpt", client_request_id: "cli-handoff")
  common = [registry_path, "--state-root", state_root, "--project", "dora"]
  output, status = run_cli("handoff-next", *common)
  abort "next handoff CLI failed: #{output}" unless status.success? && output.include?(handoff.fetch("id")) && !output.include?(state_root)
  output, status = run_cli("handoff-claim", *common, "--id", handoff.fetch("id"))
  abort "claim handoff CLI failed: #{output}" unless status.success? && output.include?("CLAIMED")
  feedback = {"phase" => "START", "milestone" => "Started safe handoff work", "progress" => "The owner-local workflow started", "finding" => "Existing lifecycle feedback remains the write boundary", "verification_state" => "NOT_STARTED", "deviations" => [], "residual_risks" => []}
  output, status = run_cli("handoff-feedback", *common, "--id", handoff.fetch("id"), "--feedback-json", JSON.generate(feedback))
  abort "feedback CLI failed: #{output}" unless status.success? && output.include?("START")
  _output, malformed_feedback = run_cli("handoff-feedback", *common, "--id", handoff.fetch("id"), "--feedback-json", "not-json")
  abort "malformed feedback CLI passed" if malformed_feedback.success?
  _output, second_claim = run_cli("handoff-claim", *common, "--id", handoff.fetch("id"))
  abort "double claim CLI passed" if second_claim.success?
  output, status = run_cli("handoff-link", *common, "--id", handoff.fetch("id"), "--master-plan", "docs/work/delivery-master.yaml", "--work-plan", "docs/work/delivery.yaml")
  abort "link handoff CLI failed: #{output}" unless status.success? && output.include?("delivery-master.yaml")
  completion = {"work_performed" => ["Proved CLI feedback"], "decisions" => ["Keep the local boundary"], "acceptance_results" => ["CLI proof passed"], "deviations" => [], "residual_risks" => [], "follow_up" => {"needed" => false, "reason" => nil}}
  output, status = run_cli("handoff-complete", *common, "--id", handoff.fetch("id"), "--verification-plan", "docs/work/delivery.yaml", "--verification-task", "verify", "--completion-json", JSON.generate(completion))
  abort "complete handoff CLI failed: #{output}" unless status.success? && output.include?("COMPLETED") && output.include?("delivery.yaml#verify") && output.include?("Proved CLI feedback")
  second = Dora::Handoff.new(state_root: state_root).create!(project: "dora", title: "Blocked CLI handoff", objective: "Record a structured block.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "cli-blocked")
  output, status = run_cli("handoff-block", *common, "--id", second.fetch("id"), "--reason", "Owner decision required.")
  abort "block handoff CLI failed: #{output}" unless status.success? && output.include?("BLOCKED")
  owner_block = Dora::Handoff.new(state_root: state_root).create!(project: "dora", title: "Owner decision CLI handoff", objective: "Record a structured owner decision.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "cli-owner-decision")
  run_cli("handoff-claim", *common, "--id", owner_block.fetch("id"))
  owner_decision = {"question" => "Which result is intended?", "why" => "The owner owns this product choice.", "options" => ["Result A", "Result B"], "recommendation" => "Result A", "blocked_work" => ["Implementation choice"], "may_continue" => ["Documentation"]}
  output, status = run_cli("handoff-block-owner-decision", *common, "--id", owner_block.fetch("id"), "--owner-decision-json", JSON.generate(owner_decision))
  abort "structured owner decision CLI failed: #{output}" unless status.success? && output.include?("Owner decision required") && output.include?("Which result is intended?")
  runner_block = Dora::Handoff.new(state_root: state_root).create!(project: "dora", title: "Runner failure CLI handoff", objective: "Record a sanitized local runner failure.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "cli-runner-failure")
  Dora::HandoffCLI.run!(command: "claim", registry_path: registry_path, state_root: state_root, project: "dora", id: runner_block.fetch("id"))
  runner_failure = {"code" => "launcher_exited_nonzero", "exit_code" => 1, "recovery_hint" => "Review the owner-local launcher."}
  runner_block = Dora::HandoffCLI.run!(command: "block_runner_failure", registry_path: registry_path, state_root: state_root, project: "dora", id: runner_block.fetch("id"), runner_failure: runner_failure)
  abort "runner failure CLI service boundary did not retain structured readback" unless runner_block.dig("blocked", "runner_failure") == runner_failure && !runner_block.to_s.include?(state_root)
end

puts "Dora handoff CLI test passed (next, exclusive claim, bounded feedback, structured runner failure, evidence-gated completion, structured owner decision block, and sanitized output)."
