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

puts "Dora project memory test passed (declared intent and work remain navigable without completion claims)."
