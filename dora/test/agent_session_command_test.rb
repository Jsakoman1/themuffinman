#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
Dir.mktmpdir("dora-agent-session-command") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "session-command", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  File.write(File.join(root, "docs/work/first.yaml"), YAML.dump({"kind" => "work", "version" => 1, "id" => "first", "status" => "draft", "baseline" => "pending", "tasks" => [{"id" => "note", "title" => "Note", "observable_outcome" => "A note exists.", "dependencies" => [], "required_paths" => ["docs/note.md"], "validation" => "true", "evidence_boundary" => ["fixture"]}]}))
  File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump({"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "note", "plan" => "docs/work/first.yaml", "task" => "note", "status" => "pending"}]}))
  output, status = Open3.capture2e(CLI, "agent-session", ".dora/project.yaml", "docs/work/inventory.yaml", "docs/work/first.yaml", "note", chdir: root)
  session = YAML.safe_load(output)
  abort "agent-session command failed or mutated work" unless status.success? && session.dig("work", "selected", "id") == "note" && session.fetch("read_only") && session.dig("implementation_contract", "start", "invoked") == false && session.fetch("omitted").include?("work mutation")
end
puts "Dora agent session command test passed (one read-only cited Codex session)."
