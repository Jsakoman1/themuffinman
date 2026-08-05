#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_closeout"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-agent-closeout") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "closeout-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => "closeout-task", "title" => "Record closeout", "observable_outcome" => "Closeout guidance is reported.", "dependencies" => [], "required_paths" => ["src/item.rb", "docs/item.md"], "validation" => "ruby -e 'exit 0'", "evidence_boundary" => ["closeout fixture"]}]}
  File.write(File.join(root, "docs/work/closeout.yaml"), YAML.dump(plan))
  impact = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "validations" => ["test"], "documentation" => ["docs/item.md"], "runtime_evidence" => ["runtime/item"], "decisions" => ["DEC-1"]}]}
  File.write(File.join(root, ".dora/change-impact.yaml"), YAML.dump(impact))

  result = Dora::AgentCloseout.review!(project_root: root, work_plan: "docs/work/closeout.yaml", task_id: "closeout-task", impact_path: ".dora/change-impact.yaml", changed_paths: ["src/item.rb"])
  abort "closeout did not report the missing declared path" unless result.dig("required_paths", "missing_from_change_set") == ["docs/item.md"]
  abort "closeout did not retain declared obligations" unless result.dig("impact_obligations", "documentation") == ["docs/item.md"]
  abort "closeout claimed a verification result" if result.dig("completion", "verified") || result.dig("completion", "status_mutation")
end

puts "Dora agent closeout test passed (read-only gaps and declared follow-up only)."
