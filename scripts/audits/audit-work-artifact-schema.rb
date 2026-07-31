#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("../..", __dir__)
SCHEMA = YAML.load_file(File.join(ROOT, "docs/work-artifact-schema.yaml"))
CATEGORIES = SCHEMA.fetch("categories")

def missing_fields(artifact, fields)
  fields.reject { |field| artifact.key?(field) && !artifact.fetch(field).nil? && artifact.fetch(field) != "" }
end

def validate_artifact(artifact, label)
  violations = []
  kind = artifact["kind"]
  category_name, category = CATEGORIES.find { |_name, definition| definition.fetch("kinds").include?(kind) }
  return ["#{label}: unsupported kind #{kind.inspect}"] unless category

  exceptions = SCHEMA.fetch("legacy_path_exceptions", {}).fetch(label, {})
  missing = missing_fields(artifact, category.fetch("required_fields")) - Array(exceptions["missing_required_fields"])
  violations << "#{label}: missing required fields #{missing.join(", ")}" unless missing.empty?

  if category_name == "executable"
    status = artifact["status"]
    allowed_statuses = category.fetch("allowed_statuses")
    violations << "#{label}: unsupported executable status #{status.inspect}" unless allowed_statuses.include?(status)
    legacy_task_metadata = Array(SCHEMA.fetch("legacy_task_metadata_path_prefixes", [])).any? { |prefix| label.start_with?(prefix) }
    if kind == "work" && artifact["strict_verification"] == true && !legacy_task_metadata
      tasks = artifact["tasks"]
      violations << "#{label}: work tasks must be an array" unless tasks.is_a?(Array)
      Array(tasks).each_with_index do |task, index|
        required = category.fetch("task_requirements").fetch("work")
        missing_task = task.is_a?(Hash) ? missing_fields(task, required) : required
        violations << "#{label}: task #{index + 1} missing #{missing_task.join(", ")}" unless missing_task.empty?
      end
    end
  elsif category_name == "inventory"
    Array(artifact["items"]).each_with_index do |item, index|
      missing_item = item.is_a?(Hash) ? missing_fields(item, category.fetch("item_required_fields")) : category.fetch("item_required_fields")
      violations << "#{label}: inventory item #{index + 1} missing #{missing_item.join(", ")}" unless missing_item.empty?
    end
  end

  violations
end

def fixture(kind:, artifact:, valid:)
  violations = validate_artifact(artifact, "fixture #{kind}")
  if valid
    abort "Fixture #{kind} unexpectedly failed: #{violations.join("; ")}" unless violations.empty?
  else
    abort "Fixture #{kind} unexpectedly passed" if violations.empty?
  end
end

if ARGV == ["--check-fixtures"]
  fixture(kind: "work", artifact: { "kind" => "work", "version" => 1, "id" => "fixture", "status" => "draft", "tasks" => [{ "id" => "one", "title" => "One", "observable_outcome" => "Visible", "dependencies" => [], "evidence_boundary" => ["leaf"], "paths" => ["docs/a"], "required_paths" => ["docs/a"], "validation" => "ruby -c scripts/a.rb" }] }, valid: true)
  fixture(kind: "historical", artifact: { "kind" => "runtime_scenario_catalog", "version" => 1 }, valid: true)
  fixture(kind: "unknown", artifact: { "kind" => "unclassified", "version" => 1 }, valid: false)
  fixture(kind: "incomplete_work", artifact: { "kind" => "work", "version" => 1, "id" => "fixture", "status" => "draft", "strict_verification" => true }, valid: false)
  fixture(kind: "incomplete_inventory", artifact: { "kind" => "execution-inventory", "version" => 1, "id" => "fixture", "items" => [{}] }, valid: false)
  puts "Work-artifact schema fixtures passed (accepted executable/historical and rejected ambiguous artifacts)."
  exit 0
end

abort "usage: ruby scripts/audits/audit-work-artifact-schema.rb [--check-fixtures]" unless ARGV.empty?
violations = Dir[File.join(ROOT, "docs/work/*.yaml")].sort.flat_map do |path|
  artifact = YAML.load_file(path)
  validate_artifact(artifact, path.delete_prefix("#{ROOT}/"))
rescue Psych::SyntaxError => error
  ["#{path.delete_prefix("#{ROOT}/")}: invalid YAML (#{error.message.lines.first.strip})"]
end

abort "Work-artifact schema audit failed:\n- #{violations.join("\n- ")}" unless violations.empty?

puts "Work-artifact schema audit passed (#{Dir[File.join(ROOT, "docs/work/*.yaml")].length} artifacts; executable, inventory, and historical categories)."
