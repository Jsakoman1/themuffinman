#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require "open3"
require_relative "../lib/dora/agent_closeout"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)

def run!(*command, chdir:)
  _output, error, status = Open3.capture3(*command, chdir: chdir)
  abort "command failed: #{command.join(" ")}: #{error}" unless status.success?
end
Dir.mktmpdir("dora-agent-closeout") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "closeout-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => "closeout-task", "title" => "Record closeout", "observable_outcome" => "Closeout guidance is reported.", "dependencies" => [], "required_paths" => ["src/item.rb", "docs/item.md"], "validation" => "ruby -e 'exit 0'", "evidence_boundary" => ["closeout fixture"]}]}
  File.write(File.join(root, "docs/work/closeout.yaml"), YAML.dump(plan))
  impact = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "validations" => ["test"], "documentation" => ["docs/item.md"], "runtime_evidence" => ["runtime/item"], "decisions" => ["DEC-1"], "companions" => [{"id" => "source-test", "paths" => ["test/item_test.rb"], "reason" => "Source changes may require test review."}]}]}
  File.write(File.join(root, ".dora/change-impact.yaml"), YAML.dump(impact))

  result = Dora::AgentCloseout.review!(project_root: root, work_plan: "docs/work/closeout.yaml", task_id: "closeout-task", impact_path: ".dora/change-impact.yaml", changed_paths: ["src/item.rb"])
  abort "closeout did not report the missing declared path" unless result.dig("required_paths", "missing_from_change_set") == ["docs/item.md"]
  abort "closeout did not retain declared obligations" unless result.dig("impact_obligations", "documentation") == ["docs/item.md"]
  abort "closeout omitted advisory companion finding" unless result.dig("impact_obligations", "companion_findings", 0, "classification_required") && result.fetch("next_steps").include?("Classify declared companion findings before expanding scope.")
  abort "closeout claimed a verification result" if result.dig("completion", "verified") || result.dig("completion", "status_mutation")

  run!("git", "init", "-q", chdir: root)
  run!("git", "config", "user.email", "dora@example.test", chdir: root)
  run!("git", "config", "user.name", "Dora Test", chdir: root)
  run!("git", "add", ".", chdir: root)
  run!("git", "commit", "-qm", "baseline", chdir: root)
  baseline, = Open3.capture2("git", "rev-parse", "--short", "HEAD", chdir: root)
  started_plan = YAML.load_file(File.join(root, "docs/work/closeout.yaml"))
  started_plan["baseline"] = baseline.strip
  started_plan.fetch("tasks").first["started_at"] = "2026-08-10T12:00:00Z"
  started_plan.fetch("tasks").first["start_workspace_paths"] = []
  File.write(File.join(root, "docs/work/closeout.yaml"), YAML.dump(started_plan))
  run!("git", "add", ".", chdir: root)
  run!("git", "commit", "-qm", "task-start", chdir: root)
  Dir.mkdir(File.join(root, "src"))
  File.write(File.join(root, "src/item.rb"), "changed after task start\n")
  started = Dora::AgentCloseout.review_started_task!(project_root: root, work_plan: "docs/work/closeout.yaml", task_id: "closeout-task", impact_path: ".dora/change-impact.yaml")
  abort "started closeout did not derive the task change set" unless started.dig("change_set", "changed_since_start") == ["src/item.rb"]
  abort "started closeout lost the declared missing path" unless started.dig("required_paths", "missing_from_change_set") == ["docs/item.md"]
  abort "started closeout claimed mutation" unless started.fetch("read_only") && !started.dig("completion", "status_mutation")
end

puts "Dora agent closeout test passed (read-only gaps and declared follow-up only)."
