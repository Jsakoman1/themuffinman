#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/codex-experience-consumers.yaml"))
abort "Codex experience fixture is invalid" unless fixture["kind"] == "dora_codex_experience_consumers"
Dir.mktmpdir("dora-codex-experience") do |sandbox|
  fixture.fetch("consumers").each do |consumer|
    project = File.join(sandbox, consumer.fetch("id"))
    _output, status = Open3.capture2e(CLI, "init", project, "--project", consumer.fetch("id"))
    abort "cannot initialize #{consumer.fetch("id")}" unless status.success?
    instructions = "#{consumer.fetch("instruction")}\n"
    File.write(File.join(project, "AGENTS.md"), instructions)
    plan = {"kind" => "work", "version" => 1, "id" => "first", "tasks" => [{"id" => consumer.fetch("task")}]} 
    inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "first", "plan" => "docs/work/first.yaml", "task" => consumer.fetch("task"), "status" => "pending"}]}
    File.write(File.join(project, "docs/work/first.yaml"), YAML.dump(plan))
    File.write(File.join(project, "docs/work/inventory.yaml"), YAML.dump(inventory))
    _output, status = Open3.capture2e(CLI, "codex-integrate", project)
    abort "cannot integrate #{consumer.fetch("id")}" unless status.success?
    output, status = Open3.capture2e(CLI, "next", File.join(project, ".dora/project.yaml"), "docs/work/inventory.yaml", "--format", "json")
    abort "cannot obtain next action for #{consumer.fetch("id")}" unless status.success?
    result = JSON.parse(output)
    abort "consumer lost its own instructions" unless File.read(File.join(project, "AGENTS.md")) == instructions
    abort "consumer received another task" unless result.dig("payload", "task") == consumer.fetch("task")
    abort "consumer lacks bounded Dora guidance" unless File.file?(File.join(project, ".dora/codex-integration/session-discovery.md"))
  end
end
puts "Dora independent Codex experience consumer test passed (two projects preserve distinct instructions and receive only their own cited next task)."
