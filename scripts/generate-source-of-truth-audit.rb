#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "json"
require "fileutils"
require "set"
require "time"

REPO_ROOT = File.expand_path("..", __dir__)
APP_ROOT = File.join(REPO_ROOT, "apps", "themuffinman")
JAVA_ROOT = File.join(APP_ROOT, "src", "main", "java", "com", "themuffinman", "app")
TEST_ROOT = File.join(APP_ROOT, "src", "test", "java", "com", "themuffinman", "app")
SECTIONS_DIR = File.join(REPO_ROOT, "docs", "agent-operating-model", "sections")
OUTPUT_PATH = File.join(REPO_ROOT, "docs", "generated", "source-of-truth-audit.json")
BACKEND_AUDIT_INVENTORY_PATH = File.join(REPO_ROOT, "docs", "generated", "backend-audit-inventory.json")

def load_section(name)
  YAML.load_file(File.join(SECTIONS_DIR, "#{name}.yaml")).fetch(name)
end

def relative_repo_path(path)
  path.delete_prefix("#{REPO_ROOT}/")
end

def matches_auto_scan?(relative_path, scans)
  scans.any? do |scan|
    relative_path.start_with?(scan.fetch("root_path")) &&
      relative_path.include?(scan.fetch("path_contains")) &&
      relative_path.end_with?(scan.fetch("file_suffix"))
  end
end

def workflow_inventory_covered?(source_ref, relative_path, workflow_entries)
  workflow_entries.any? do |entry|
    Array(entry["source_refs"]).include?(source_ref) ||
      Array(entry["verification_tests"]).include?(relative_path)
  end
end

def mutating_service_candidate?(path)
  return true if File.basename(path).end_with?("UseCase.java")
  return false unless File.basename(path).end_with?("Service.java")

  source = File.read(path)
  return false unless source.include?("@Transactional")

  source.match?(/\b(create|update|delete|approve|decline|confirm|reject|apply|withdraw|start|complete|mark|send|block|unblock|accept)\w*\s*\(/)
end

def tracked_test_candidate?(path)
  return true if path.include?("/agent/")
  return true if path.include?("/docs/")

  basename = File.basename(path)
  basename.end_with?("ScenarioTest.java") ||
    basename.end_with?("ContractTest.java") ||
    basename.end_with?("ValidationTest.java")
end

source_of_truth = load_section("source_of_truth").fetch("files")
documentation_coverage = load_section("documentation_coverage")
workflow_inventory = load_section("service_workflow_inventory")
backend_inventory = JSON.parse(File.read(BACKEND_AUDIT_INVENTORY_PATH))

path_to_ref = {}
source_of_truth.each do |entry|
  path_to_ref[entry.fetch("path")] = entry.fetch("id")
end

coverage_group_refs = documentation_coverage.fetch("required_source_ref_groups", [])
  .flat_map { |group| Array(group["source_refs"]) }
  .to_set
workflow_entries = workflow_inventory
auto_scans = documentation_coverage.fetch("auto_scans", [])

strict_backend_paths = Array(backend_inventory["entries"])
  .select { |entry| entry["tier"] == "executor_critical" }
  .map { |entry| File.join(REPO_ROOT, entry.fetch("path")) }
  .sort
test_paths = Dir.glob(File.join(TEST_ROOT, "**", "*.java")).select { |path| tracked_test_candidate?(path) }.sort

candidate_paths = (strict_backend_paths + test_paths).uniq

missing_source_refs = []
missing_documentation_coverage = []
missing_service_workflow_coverage = []

candidate_paths.each do |absolute_path|
  relative_path = relative_repo_path(absolute_path)
  source_ref = path_to_ref[relative_path]

  if source_ref.nil?
    missing_source_refs << relative_path
    next
  end

  covered_by_documentation = coverage_group_refs.include?(source_ref) || matches_auto_scan?(relative_path, auto_scans)
  missing_documentation_coverage << relative_path unless covered_by_documentation

  next unless absolute_path.include?("/service/")
  next unless mutating_service_candidate?(absolute_path)

  covered_by_workflow_inventory = workflow_inventory_covered?(source_ref, relative_path, workflow_entries)
  missing_service_workflow_coverage << relative_path unless covered_by_workflow_inventory
end

report = {
  "generatedAt" => Time.now.utc.iso8601,
  "summary" => {
    "missingSourceRefCount" => missing_source_refs.length,
    "missingDocumentationCoverageCount" => missing_documentation_coverage.length,
    "missingServiceWorkflowCoverageCount" => missing_service_workflow_coverage.length
  },
  "missingSourceRefs" => missing_source_refs,
  "missingDocumentationCoverage" => missing_documentation_coverage,
  "missingServiceWorkflowCoverage" => missing_service_workflow_coverage
}

FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
File.write(OUTPUT_PATH, JSON.pretty_generate(report) + "\n")
puts "Generated #{OUTPUT_PATH}"
