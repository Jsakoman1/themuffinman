#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_knowledge"
require_relative "../lib/dora/project_memory"

ROOT = File.expand_path("..", __dir__)
ANSWERS = File.join(ROOT, "test/fixtures/one-click-project-answers.yaml")

Dir.mktmpdir("dora-one-click-consumer") do |sandbox|
  destination = File.join(sandbox, "garden-journal")
  output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "new", destination, "--answers", ANSWERS)
  abort "one-click consumer creation failed: #{output}" unless status.success?

  knowledge = Dora::ProjectKnowledge.validate!(destination)
  memory = Dora::ProjectMemory.load!(File.join(destination, "docs/project-memory.yaml"))
  plan = YAML.load_file(File.join(destination, "docs/work/first-work.yaml"))
  files = Dir[File.join(destination, "{.dora,docs,AGENTS.md}/**/*")].select { |path| File.file?(path) }
  content = files.map { |path| File.read(path) }.join("\n")

  abort "one-click consumer lost the declared product" unless knowledge.dig("product_brief", "product") == "Garden journal"
  abort "one-click consumer memory does not link first work" unless memory.dig("current_work", "task") == "record-note"
  abort "one-click consumer did not retain its open decision" unless memory.fetch("open_decisions").map { |entry| entry.fetch("statement") } == ["Who may archive a planting note?"]
  abort "one-click consumer did not declare its first task" unless plan.dig("tasks", 0, "id") == "record-note"
  abort "one-click consumer wrote product implementation" if File.exist?(File.join(destination, "docs/planting-note.md"))
  abort "one-click consumer refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent one-click consumer test passed (a fresh explicit project has knowledge, memory, and first work without MuffinMan data)."
