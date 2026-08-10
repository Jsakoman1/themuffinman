#!/usr/bin/env ruby
# frozen_string_literal: true

require "tempfile"
require "yaml"
require_relative "../lib/dora/project_memory"

memory = {
  "kind" => "dora_project_memory",
  "version" => 1,
  "project_intent" => {"product_brief" => "docs/product-brief.yaml", "domain_library" => "docs/domain-library.yaml"},
  "canonical_knowledge" => [
    {"id" => "product", "path" => "docs/product-brief.yaml", "purpose" => "Declared user problem and product boundaries."},
    {"id" => "domain", "path" => "docs/domain-library.yaml", "purpose" => "Declared domain rules and workflows."}
  ],
  "open_decisions" => [{"id" => "archive-rule", "statement" => "Who may archive a planting note?", "source" => "docs/product-brief.yaml"}],
  "capability_intent" => [{"id" => "record-note", "intended_outcome" => "A member can find a recorded planting decision.", "knowledge_references" => ["product", "domain"]}],
  "current_work" => {"plan" => "docs/work/first-work.yaml", "task" => "record-note", "state" => "planned"}
}

Tempfile.create(["dora-project-memory", ".yaml"]) do |file|
  file.write(YAML.dump(memory))
  file.flush
  result = Dora::ProjectMemory.load!(file.path)
  abort "project memory lost current task" unless result.dig("current_work", "task") == "record-note"
  abort "project memory lost an open decision" unless result.fetch("open_decisions").map { |entry| entry.fetch("id") } == ["archive-rule"]
  abort "project memory made a completion claim" unless result.fetch("completion_boundary").include?("does not prove")
end

begin
  Dora::ProjectMemory.validate!(memory.merge("current_work" => memory.fetch("current_work").merge("state" => "complete")))
  abort "project memory accepted a completion state"
rescue ArgumentError
end

idle = Dora::ProjectMemory.validate!(memory.merge("current_work" => {"state" => "none"}))
abort "project memory lost an explicit idle state" unless idle.fetch("current_work") == {"state" => "none"}

active_navigation = Dora::ProjectMemory.validate_work_navigation!(memory: memory.merge("current_work" => {"plan" => "docs/work/first-work.yaml", "task" => "record-note", "state" => "active"}), inventories: [{"items" => [{"plan" => "docs/work/first-work.yaml", "task" => "record-note", "status" => "in_progress"}]}])
abort "project memory did not preserve active authoritative navigation" unless active_navigation == {"plan" => "docs/work/first-work.yaml", "task" => "record-note", "state" => "active"}

begin
  Dora::ProjectMemory.validate_work_navigation!(memory: idle, inventories: [{"items" => [{"plan" => "docs/work/first-work.yaml", "task" => "record-note", "status" => "in_progress"}]}])
  abort "project memory accepted missing active navigation"
rescue ArgumentError => error
  abort "missing navigation failure was unclear" unless error.message.include?("missing")
end

begin
  Dora::ProjectMemory.validate_work_navigation!(memory: memory.merge("current_work" => {"plan" => "docs/work/first-work.yaml", "task" => "record-note", "state" => "active"}), inventories: [{"items" => [{"plan" => "docs/work/first-work.yaml", "task" => "record-note", "status" => "verified"}]}])
  abort "project memory accepted stale active navigation"
rescue ArgumentError => error
  abort "stale navigation failure was unclear" unless error.message.include?("stale")
end

begin
  Dora::ProjectMemory.validate!(memory.merge("current_work" => {"state" => "none", "plan" => "docs/work/first-work.yaml"}))
  abort "project memory accepted an idle work declaration with a plan"
rescue ArgumentError
end

puts "Dora project memory test passed (declared intent, active work, and idle state remain navigable without completion claims)."
