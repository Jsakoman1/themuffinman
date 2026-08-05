#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/command_registry"

root = File.expand_path("..", __dir__)
documents = [File.join(root, "README.md"), File.join(root, "docs/agent-first-application-guide.md")].map { |path| [path, File.read(path)] }.to_h
agent_commands = [
  "dora agent-context <adapter-path> <work-plan-path> <task-id>",
  "dora agent-next <adapter-path> <execution-inventory-path>",
  "dora status <adapter-path> <execution-inventory-path>",
  "dora impact <adapter-path> <node-id>...",
  "dora agent-closeout <adapter-path> <work-plan-path> <task-id> <change-impact-path> <changed-path>..."
]
registered = Dora::CommandRegistry.list(schema_path: "unused").fetch("commands").map { |command| command.fetch("usage") }

agent_commands.each do |command|
  abort "agent command is missing from help registry: #{command}" unless registered.include?(command)
  documents.each { |path, content| abort "agent command is missing from #{path}: #{command}" unless content.include?(command) }
end

puts "Dora agent command surface test passed (registry, README, and agent guide expose the same bounded commands)."
