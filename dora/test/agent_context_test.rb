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
  abort "context is not bounded and cited" unless context.fetch("citations").map { |citation| citation.fetch("path") } == ["docs/product-brief.yaml", "docs/domain-library.yaml", ".dora/agent-project-profile.yaml", "docs/work/first.yaml"]
  abort "context made a completion claim" if context.to_s.downcase.include?("completed")
end

puts "Dora agent context test passed (bounded selected task and declared cited knowledge only)."
