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

def tally_values(values)
  values.each_with_object({}) do |value, counts|
    counts[value] = counts.fetch(value, 0) + 1
  end
end

def mutating_service_candidate?(path)
  return true if File.basename(path).end_with?("UseCase.java")
  return false unless File.basename(path).end_with?("Service.java")

  source = File.read(path)
  return false unless source.include?("@Transactional")

  source.match?(/^\s*(?:public|protected|private)\s+[A-Za-z0-9_<>, ?\[\]]+\s+(?:create|update|delete|approve|decline|confirm|reject|apply|withdraw|start|complete|mark|send|block|unblock|accept)\w*\s*\(/m)
end

def tracked_test_candidate?(path)
  return true if path.include?("/agent/")
  return true if path.include?("/docs/")

  basename = File.basename(path)
  basename.end_with?("ScenarioTest.java") ||
    basename.end_with?("ContractTest.java") ||
    basename.end_with?("ValidationTest.java")
end

def test_domain_id(relative_path, known_domain_ids)
  package_segment = relative_path[%r{apps/themuffinman/src/test/java/com/themuffinman/app/([^/]+)/}, 1]
  return package_segment if package_segment && known_domain_ids.include?(package_segment)

  "platform"
end

source_of_truth = load_section("source_of_truth").fetch("files")
documentation_coverage = load_section("documentation_coverage")
workflow_inventory = load_section("service_workflow_inventory")
backend_audit_coverage = load_section("backend_audit_coverage")
backend_inventory = JSON.parse(File.read(BACKEND_AUDIT_INVENTORY_PATH))
domain_ownership = backend_audit_coverage.fetch("domain_ownership")
strict_backend_rule_ids = (
  Array(backend_audit_coverage.fetch("current_enforcement").fetch("strict_source_of_truth_rule_ids")) +
    Array(backend_audit_coverage.fetch("current_enforcement").fetch("strict_documentation_rule_ids"))
).to_set

path_to_ref = {}
source_of_truth.each do |entry|
  path_to_ref[entry.fetch("path")] = entry.fetch("id")
end

coverage_group_refs = documentation_coverage.fetch("required_source_ref_groups", [])
  .flat_map { |group| Array(group["source_refs"]) }
  .to_set
workflow_entries = workflow_inventory
auto_scans = documentation_coverage.fetch("auto_scans", [])

strict_backend_entries = Array(backend_inventory["entries"])
  .select do |entry|
    entry["tier"] == "executor_critical" ||
      strict_backend_rule_ids.include?(entry.fetch("classificationRuleId"))
  end
strict_backend_paths = strict_backend_entries
  .map { |entry| File.join(REPO_ROOT, entry.fetch("path")) }
  .sort
test_paths = Dir.glob(File.join(TEST_ROOT, "**", "*.java")).select { |path| tracked_test_candidate?(path) }.sort

candidate_paths = (strict_backend_paths + test_paths).uniq
backend_entries_by_path = Array(backend_inventory["entries"]).to_h { |entry| [entry.fetch("path"), entry] }
owner_by_domain = domain_ownership.to_h { |entry| [entry.fetch("id"), entry.fetch("owner_id")] }
known_domain_ids = owner_by_domain.keys.to_set

missing_source_refs = []
missing_documentation_coverage = []
missing_service_workflow_coverage = []
missing_source_ref_details = []
missing_documentation_coverage_details = []
missing_service_workflow_coverage_details = []
candidate_entries = []

candidate_paths.each do |absolute_path|
  relative_path = relative_repo_path(absolute_path)
  source_ref = path_to_ref[relative_path]
  backend_entry = backend_entries_by_path[relative_path]
  domain_id = backend_entry&.fetch("domainId") || test_domain_id(relative_path, known_domain_ids)
  owner_id = backend_entry&.fetch("ownerId") || owner_by_domain.fetch(domain_id)
  candidate_type = absolute_path.start_with?(TEST_ROOT) ? "tracked_test" : "backend_executor_critical"

  if source_ref.nil?
    missing_source_refs << relative_path
    missing_source_ref_details << {
      "path" => relative_path,
      "candidateType" => candidate_type,
      "domainId" => domain_id,
      "ownerId" => owner_id
    }
    next
  end

  covered_by_documentation = coverage_group_refs.include?(source_ref) || matches_auto_scan?(relative_path, auto_scans)
  mutating_service = absolute_path.include?("/service/") && mutating_service_candidate?(absolute_path)
  covered_by_workflow_inventory = mutating_service ? workflow_inventory_covered?(source_ref, relative_path, workflow_entries) : nil

  candidate_entries << {
    "path" => relative_path,
    "candidateType" => candidate_type,
    "domainId" => domain_id,
    "ownerId" => owner_id,
    "sourceRefId" => source_ref,
    "documentationCovered" => covered_by_documentation,
    "mutatingServiceCandidate" => mutating_service,
    "serviceWorkflowCovered" => covered_by_workflow_inventory
  }

  unless covered_by_documentation
    missing_documentation_coverage << relative_path
    missing_documentation_coverage_details << {
      "path" => relative_path,
      "candidateType" => candidate_type,
      "domainId" => domain_id,
      "ownerId" => owner_id,
      "sourceRefId" => source_ref
    }
  end

  next unless mutating_service

  unless covered_by_workflow_inventory
    missing_service_workflow_coverage << relative_path
    missing_service_workflow_coverage_details << {
      "path" => relative_path,
      "candidateType" => candidate_type,
      "domainId" => domain_id,
      "ownerId" => owner_id,
      "sourceRefId" => source_ref
    }
  end
end

report = {
  "generatedAt" => Time.now.utc.iso8601,
  "summary" => {
    "candidateCount" => candidate_entries.length,
    "domainCounts" => tally_values(candidate_entries.map { |entry| entry["domainId"] }),
    "ownerCounts" => tally_values(candidate_entries.map { |entry| entry["ownerId"] }),
    "missingSourceRefCount" => missing_source_refs.length,
    "missingDocumentationCoverageCount" => missing_documentation_coverage.length,
    "missingServiceWorkflowCoverageCount" => missing_service_workflow_coverage.length
  },
  "candidateEntries" => candidate_entries,
  "missingSourceRefs" => missing_source_refs,
  "missingSourceRefDetails" => missing_source_ref_details,
  "missingDocumentationCoverage" => missing_documentation_coverage,
  "missingDocumentationCoverageDetails" => missing_documentation_coverage_details,
  "missingServiceWorkflowCoverage" => missing_service_workflow_coverage,
  "missingServiceWorkflowCoverageDetails" => missing_service_workflow_coverage_details
}

FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
File.write(OUTPUT_PATH, JSON.pretty_generate(report) + "\n")
puts "Generated #{OUTPUT_PATH}"
