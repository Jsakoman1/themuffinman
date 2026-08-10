#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_context"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-agent-context") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "context-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => "first-task", "title" => "Record a note", "observable_outcome" => "A note is recorded.", "dependencies" => [], "required_paths" => ["docs/note.md"], "validation" => "ruby -e 'exit 0'", "evidence_boundary" => ["note fixture"]}]}
  File.write(File.join(root, "docs/work/first.yaml"), YAML.dump(plan))
  context = Dora::AgentContext.build!(project_root: root, work_plan: "docs/work/first.yaml", task_id: "first-task")
  abort "context lost selected task" unless context.dig("task", "id") == "first-task"
  abort "context invented an implementation contract" if context.dig("task", "implementation_contract")
  contract_plan = Marshal.load(Marshal.dump(plan))
  contract_plan.fetch("tasks").first["implementation_contract"] = {"read_paths" => ["src/read.rb"], "write_paths" => ["src/write.rb"], "permission_owner" => "service", "schema_paths" => [], "api_paths" => ["docs/api.yaml"], "ui_paths" => [], "test_paths" => ["test/api_test.rb"], "documentation_paths" => ["docs/api.md"], "runtime_evidence" => {"required" => false, "paths" => []}}
  File.write(File.join(root, "docs/work/contract.yaml"), YAML.dump(contract_plan))
  contract_context = Dora::AgentContext.build!(project_root: root, work_plan: "docs/work/contract.yaml", task_id: "first-task")
  abort "context omitted the task-owned implementation contract" unless contract_context.dig("task", "implementation_contract", "permission_owner") == "service" && contract_context.dig("task", "implementation_contract", "api_paths") == ["docs/api.yaml"]
  abort "context is not bounded and cited" unless context.fetch("citations").map { |citation| citation.fetch("path") } == ["docs/product-brief.yaml", "docs/domain-library.yaml", ".dora/agent-project-profile.yaml", "docs/work/first.yaml"]
  abort "context made a completion claim" if context.to_s.downcase.include?("completed")
end

puts "Dora agent context test passed (bounded selected task and declared cited knowledge only)."
