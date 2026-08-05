#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_session"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/agent-cockpit-projects.yaml"))

def session_for(root, definition)
  Dora::ProjectInitializer.initialize!(root, project_id: definition.fetch("id"), manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  profile_path = File.join(root, ".dora/agent-project-profile.yaml")
  profile = YAML.load_file(profile_path)
  profile["stack_commands"] = [{"id" => definition.fetch("tool"), "command" => "true", "purpose" => "Run this consumer fixture tool."}]
  File.write(profile_path, YAML.dump(profile))
  plan_path = "docs/work/first.yaml"
  plan = {"kind" => "work", "version" => 1, "id" => "first", "status" => "draft", "baseline" => "pending", "tasks" => [{"id" => definition.fetch("task"), "title" => definition.fetch("task"), "observable_outcome" => "One declared consumer record exists.", "dependencies" => [], "required_paths" => ["docs/#{definition.fetch("task")}.md"], "validation" => "true", "evidence_boundary" => ["consumer fixture"]}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => definition.fetch("task"), "plan" => plan_path, "task" => definition.fetch("task"), "status" => "pending"}]}
  File.write(File.join(root, plan_path), YAML.dump(plan)); File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump(inventory))
  Dora::AgentSession.build!(adapter_path: File.join(root, ".dora/project.yaml"), inventory_path: "docs/work/inventory.yaml", work_plan: plan_path, task_id: definition.fetch("task"), adapter_schema_path: File.join(ROOT, "project-adapter.schema.yaml"), control_schema_path: File.join(ROOT, "project-control.schema.yaml"))
end

Dir.mktmpdir("dora-independent-agent-cockpit") do |sandbox|
  sessions = FIXTURE.fetch("projects").to_h { |definition| [definition.fetch("id"), session_for(File.join(sandbox, definition.fetch("id")), definition)] }
  FIXTURE.fetch("projects").each do |definition|
    own = sessions.fetch(definition.fetch("id")).to_s
    other = FIXTURE.fetch("projects").find { |candidate| candidate != definition }
    abort "consumer session lost its declared task or tool" unless own.include?(definition.fetch("task")) && own.include?(definition.fetch("tool"))
    abort "consumer session leaked another project's task or tool" if own.include?(other.fetch("task")) || own.include?(other.fetch("tool"))
  end
end

puts "Dora independent agent cockpit consumer test passed (two projects receive isolated bounded sessions)."
