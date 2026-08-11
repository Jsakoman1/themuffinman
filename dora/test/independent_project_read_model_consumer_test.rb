#!/usr/bin/env ruby
# frozen_string_literal: true

# This simulates a separate consumer: it imports the public read-model contract
# and does not invoke Dora's CLI or independently parse project artifacts.
require "fileutils"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_read_model"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/project-read-model-projects.yaml")).fetch("project")

def write_yaml(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def create_project(root, invalid_memory: false, ambiguous: false)
  Dora::ProjectInitializer.initialize!(root, project_id: FIXTURE.fetch("id"), manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief_path = File.join(root, "docs/product-brief.yaml")
  brief = YAML.load_file(brief_path)
  brief["product"] = FIXTURE.fetch("name")
  File.write(brief_path, YAML.dump(brief))
  memory = {"kind" => "dora_project_memory", "version" => 1, "project_intent" => {"product_brief" => "docs/product-brief.yaml", "domain_library" => "docs/domain-library.yaml"}, "canonical_knowledge" => [{"id" => "product", "path" => "docs/product-brief.yaml", "purpose" => "Product intent."}], "open_decisions" => [], "capability_intent" => [], "current_work" => {"plan" => "docs/work/consumer-master.yaml", "task" => "consumer-task", "state" => "active"}}
  memory["current_work"] = {"plan" => "docs/work/consumer-master.yaml", "state" => "verified"} if invalid_memory
  write_yaml(root, "docs/project-memory.yaml", memory)
  master = {"kind" => "master", "version" => 1, "id" => "consumer", "title" => "Consumer delivery", "status" => "verified", "children" => ["docs/work/consumer.yaml"]}
  plan = {
    "kind" => "work", "version" => 1, "id" => "consumer-work", "title" => "Consumer task", "status" => "verified", "baseline" => "pending",
    "tasks" => [{"id" => "consumer-task", "title" => "Produce a verified consumer result", "status" => "done", "observable_outcome" => "Consumer result exists.", "dependencies" => [], "paths" => ["docs/result.md"], "required_paths" => ["docs/result.md"], "validation" => "safe-command", "evidence_boundary" => ["fixture"]}],
    "evidence" => [{"task" => "consumer-task", "result" => "passed", "ranAt" => FIXTURE.fetch("latest_verified_at"), "revision" => "abcdef1", "exitCode" => 0, "output" => FIXTURE.dig("independent_consumer", "raw_output")}]
  }
  inventory = {
    "kind" => "execution_inventory", "version" => 1, "id" => "consumer", "master_plan" => "docs/work/consumer-master.yaml",
    "state" => "verified",
    "items" => [{"id" => "consumer-item", "order" => 1, "plan" => "docs/work/consumer.yaml", "task" => "consumer-task", "status" => "verified", "verified_at" => FIXTURE.fetch("latest_verified_at")}]
  }
  write_yaml(root, "docs/work/consumer-master.yaml", master)
  write_yaml(root, "docs/work/consumer.yaml", plan)
  write_yaml(root, "docs/work/consumer-inventory.yaml", inventory)
  return unless ambiguous

  inventory["id"] = "other-consumer"
  inventory["items"][0]["id"] = "other-item"
  inventory["items"][0]["status"] = "in_progress"
  write_yaml(root, "docs/work/other-consumer-inventory.yaml", inventory)
  write_yaml(root, "docs/work/consumer-inventory.yaml", inventory)
end

Dir.mktmpdir("dora-independent-consumer-invalid") do |root|
  create_project(root, invalid_memory: true)
  summary = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml")).summary
  abort "consumer did not receive a structured summary" unless summary.fetch("kind") == "dora_project_read_model"
  abort "consumer lost verified delivery because memory was invalid" unless summary.dig("delivery", "latest_verified", "id") == "consumer"
  abort "consumer did not receive invalid-memory warning" unless summary.fetch("inconsistencies").any? { |issue| issue["code"] == "project_memory" }
  abort "consumer received raw evidence" if summary.to_s.include?(FIXTURE.dig("independent_consumer", "raw_output"))
  abort "consumer received an absolute root" if summary.to_s.include?(root)
end

Dir.mktmpdir("dora-independent-consumer-ambiguous") do |root|
  create_project(root, ambiguous: true)
  model = Dora::ProjectReadModel.load!(adapter_path: File.join(root, ".dora/project.yaml"))
  summary = model.summary
  abort "consumer guessed ambiguous work" unless summary.dig("delivery", "active", "status") == "ambiguous" && summary["next_task"].nil?
  begin
    model.task("../../.env", "consumer-task")
    abort "consumer escaped declared project root"
  rescue ArgumentError
    nil
  end
end

puts "Independent Dora read-model consumer test passed (structured contract, invalid memory, ambiguity, and containment)."
