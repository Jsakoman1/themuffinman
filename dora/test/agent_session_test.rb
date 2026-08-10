#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_session"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-agent-session") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "session-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  plan = {"kind" => "work", "version" => 1, "id" => "first", "status" => "draft", "baseline" => "pending", "tasks" => [{"id" => "write-note", "title" => "Write note", "observable_outcome" => "A note exists.", "dependencies" => [], "required_paths" => ["docs/note.md"], "validation" => "true", "evidence_boundary" => ["fixture"]}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "note", "plan" => "docs/work/first.yaml", "task" => "write-note", "status" => "pending"}]}
  File.write(File.join(root, "docs/work/first.yaml"), YAML.dump(plan)); File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump(inventory))
  session = Dora::AgentSession.build!(adapter_path: File.join(root, ".dora/project.yaml"), inventory_path: "docs/work/inventory.yaml", work_plan: "docs/work/first.yaml", task_id: "write-note", adapter_schema_path: File.join(ROOT, "project-adapter.schema.yaml"), control_schema_path: File.join(ROOT, "project-control.schema.yaml"))
  abort "session lost selected work" unless session.dig("work", "selected", "id") == "write-note" && session.dig("work", "next", "action") == "start"
  abort "session omitted declared implementation contract" unless session.fetch("read_only") && !session.fetch("observed_at").empty? && session.dig("implementation_contract", "start", "invoked") == false && session.dig("implementation_contract", "start", "plan") == "docs/work/first.yaml" && session.dig("implementation_contract", "validation") == "true" && session.dig("implementation_contract", "closeout", "status_mutation") == false
  abort "session omitted declared tools or approval boundary" if session.fetch("tools").empty? || session.fetch("approval_boundary").empty?
  abort "session is not bounded and cited" unless session.fetch("citations").map { |row| row.fetch("path") }.include?("docs/work/inventory.yaml") && session.fetch("omitted").include?("work mutation")
end
puts "Dora agent session test passed (bounded work, health, tools, gaps, decisions, and approval boundary)."
