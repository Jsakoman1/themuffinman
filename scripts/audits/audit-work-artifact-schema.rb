#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../work_artifact_retention"

ROOT = File.expand_path("../..", __dir__)
SCHEMA = YAML.load_file(File.join(ROOT, "docs/work-artifact-schema.yaml"))
CATEGORIES = SCHEMA.fetch("categories")
RETENTION_POLICY = YAML.load_file(File.join(ROOT, SCHEMA.fetch("classification_source")))
VALIDATION_PROJECTION = RETENTION_POLICY.fetch("validation_projection")

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

def validate_historical_artifact(artifact, label)
  missing = missing_fields(artifact, VALIDATION_PROJECTION.fetch("historical_minimum_fields"))
  missing.empty? ? [] : ["#{label}: historical artifact missing #{missing.join(", ")}"]
end

def validation_class(artifact, label, externally_referenced: false)
  category_name, = CATEGORIES.find { |_name, definition| definition.fetch("kinds").include?(artifact["kind"]) }
  return ["unknown", "unknown", ["#{label}: unsupported kind #{artifact["kind"].inspect}"]] unless category_name
  if category_name == "historical"
    return ["historical_compatibility", "historical_contract", validate_historical_artifact(artifact, label)]
  end

  retention_class = WorkArtifactRetention.classification(artifact, externally_referenced)
  if VALIDATION_PROJECTION.fetch("current_strict_classes").include?(retention_class)
    ["current_strict", retention_class, validate_artifact(artifact, label)]
  elsif VALIDATION_PROJECTION.fetch("historical_compatibility_classes").include?(retention_class)
    ["historical_compatibility", retention_class, validate_historical_artifact(artifact, label)]
  else
    ["unknown", retention_class, ["#{label}: retention class #{retention_class.inspect} has no validation boundary"]]
  end
end

def fixture(kind:, artifact:, valid:)
  _validation_class, _retention_class, violations = validation_class(artifact, "fixture #{kind}")
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
  fixture(kind: "incomplete_current_work", artifact: { "kind" => "work", "version" => 1, "id" => "fixture", "status" => "draft", "strict_verification" => true }, valid: false)
  fixture(kind: "incomplete_current_inventory", artifact: { "kind" => "execution-inventory", "version" => 1, "id" => "fixture", "status" => "draft", "items" => [{}] }, valid: false)
  fixture(kind: "retained_verified_legacy_work", artifact: { "kind" => "work", "version" => 1, "id" => "legacy", "status" => "verified", "strict_verification" => true, "tasks" => [{}] }, valid: true)
  fixture(kind: "malformed_history", artifact: { "kind" => "scope" }, valid: false)
  puts "Work-artifact schema fixtures passed (strict current artifacts, explicit retained-history compatibility, and unknown-kind rejection)."
  exit 0
end

abort "usage: ruby scripts/audits/audit-work-artifact-schema.rb [--check-fixtures]" unless ARGV.empty?
references = WorkArtifactRetention.referenced_paths(ROOT)
counts = Hash.new(0)
retention_counts = Hash.new(0)
violations = Dir[File.join(ROOT, "docs/work/*.yaml")].sort.flat_map do |path|
  artifact = YAML.load_file(path)
  label = path.delete_prefix("#{ROOT}/")
  selected_class, retention_class, artifact_violations = validation_class(artifact, label, externally_referenced: references.fetch(label))
  counts[selected_class] += 1
  retention_counts[retention_class] += 1
  artifact_violations
rescue Psych::SyntaxError => error
  counts["invalid_yaml"] += 1
  ["#{path.delete_prefix("#{ROOT}/")}: invalid YAML (#{error.message.lines.first.strip})"]
end

abort "Work-artifact schema audit failed:\n- #{violations.join("\n- ")}" unless violations.empty?

history_summary = VALIDATION_PROJECTION.fetch("historical_compatibility_classes").map { |name| "#{name}=#{retention_counts.fetch(name, 0)}" }.join(", ")
puts "Work-artifact schema audit passed (#{Dir[File.join(ROOT, "docs/work/*.yaml")].length} artifacts; #{counts.fetch("current_strict", 0)} current strict, #{counts.fetch("historical_compatibility", 0)} retained historical [#{history_summary}], #{counts.fetch("unknown", 0)} unknown)."
