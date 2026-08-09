#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_memory"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-memory-drift") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "memory-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  memory = {"kind" => "dora_project_memory", "version" => 1, "project_intent" => {"product_brief" => "docs/product-brief.yaml", "domain_library" => "docs/domain-library.yaml"}, "canonical_knowledge" => [{"id" => "product", "path" => "docs/product-brief.yaml", "purpose" => "intent"}], "open_decisions" => [{"id" => "stale", "statement" => "Stale decision.", "source" => "docs/product-brief.yaml"}], "capability_intent" => [], "current_work" => {"plan" => "docs/work/missing.yaml", "task" => "first", "state" => "planned"}}
  path = File.join(root, "docs/project-memory.yaml"); File.write(path, YAML.dump(memory)); before = File.read(path)
  report = Dora::ProjectMemory.drift!(project_root: root)
  abort "memory drift was not reported" unless report.fetch("drifted") && report.fetch("differences").map { |row| row.fetch("kind") }.include?("open_decisions")
  abort "memory drift overwrote project-owned memory" unless File.read(path) == before

  idle_memory = memory.merge("current_work" => {"state" => "none"})
  File.write(path, YAML.dump(idle_memory))
  idle_report = Dora::ProjectMemory.drift!(project_root: root)
  abort "idle memory invented a missing work plan" if idle_report.fetch("differences").any? { |row| row.fetch("kind") == "missing_work_plan" }
end
puts "Dora project memory drift test passed (stale and idle memory are reported without overwrite or invented work)."
