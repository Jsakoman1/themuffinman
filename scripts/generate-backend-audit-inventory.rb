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
SECTIONS_DIR = File.join(REPO_ROOT, "docs", "agent-operating-model", "sections")
OUTPUT_PATH = File.join(REPO_ROOT, "docs", "generated", "backend-audit-inventory.json")

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

  source.match?(/\b(create|update|delete|approve|decline|confirm|reject|apply|withdraw|start|complete|mark|send|block|unblock|accept)\w*\s*\(/)
end

def rule_matches?(relative_path, rule)
  basename = File.basename(relative_path)

  checks = []
  checks << Array(rule["file_names"]).include?(basename) if rule.key?("file_names")
  checks << Array(rule["file_suffixes"]).any? { |suffix| relative_path.end_with?(suffix) } if rule.key?("file_suffixes")
  checks << Array(rule["path_prefixes"]).any? { |prefix| relative_path.start_with?(prefix) } if rule.key?("path_prefixes")
  checks << Array(rule["path_contains_any"]).any? { |needle| relative_path.include?(needle) } if rule.key?("path_contains_any")

  !checks.empty? && checks.all?
end

def ownership_rule_matches?(relative_path, rule)
  basename = File.basename(relative_path)

  checks = []
  checks << Array(rule["file_names"]).include?(basename) if rule.key?("file_names")
  checks << Array(rule["path_prefixes"]).any? { |prefix| relative_path.start_with?(prefix) } if rule.key?("path_prefixes")
  checks << Array(rule["path_contains_any"]).any? { |needle| relative_path.include?(needle) } if rule.key?("path_contains_any")

  checks.any?
end

backend_audit_coverage = load_section("backend_audit_coverage")
source_of_truth = load_section("source_of_truth").fetch("files")
documentation_coverage = load_section("documentation_coverage")
workflow_inventory = load_section("service_workflow_inventory")

rules = backend_audit_coverage.fetch("classification_rules")
domain_ownership_rules = backend_audit_coverage.fetch("domain_ownership")
tier_ids = backend_audit_coverage.fetch("tiers").map { |tier| tier.fetch("id") }
fail_hard_tiers = backend_audit_coverage.fetch("current_enforcement").fetch("fail_hard_tiers").to_set
strict_source_of_truth_rule_ids = backend_audit_coverage.fetch("current_enforcement").fetch("strict_source_of_truth_rule_ids").to_set
strict_documentation_rule_ids = backend_audit_coverage.fetch("current_enforcement").fetch("strict_documentation_rule_ids").to_set

path_to_ref = {}
source_of_truth.each do |entry|
  path_to_ref[entry.fetch("path")] = entry.fetch("id")
end

coverage_group_refs = documentation_coverage.fetch("required_source_ref_groups", [])
  .flat_map { |group| Array(group["source_refs"]) }
  .to_set
auto_scans = documentation_coverage.fetch("auto_scans", [])

entries = []
unclassified_paths = []
unowned_domain_paths = []
executor_critical_without_source_ref = []
automation_relevant_without_source_ref = []
strict_source_of_truth_missing = []
strict_documentation_missing = []

Dir.glob(File.join(JAVA_ROOT, "**", "*.java")).sort.each do |absolute_path|
  relative_path = relative_repo_path(absolute_path)
  matched_rule = rules.find { |rule| rule_matches?(relative_path, rule) }

  if matched_rule.nil?
    unclassified_paths << relative_path
    next
  end

  tier = matched_rule.fetch("tier")
  domain_ownership = domain_ownership_rules.find { |rule| ownership_rule_matches?(relative_path, rule) }
  if domain_ownership.nil?
    unowned_domain_paths << relative_path
    next
  end

  source_ref = path_to_ref[relative_path]
  registered_in_source_of_truth = !source_ref.nil?
  documentation_covered = registered_in_source_of_truth &&
    (coverage_group_refs.include?(source_ref) || matches_auto_scan?(relative_path, auto_scans))
  workflow_inventory_covered = registered_in_source_of_truth &&
    workflow_inventory_covered?(source_ref, relative_path, workflow_inventory)
  mutating_service = absolute_path.include?("/service/") && mutating_service_candidate?(absolute_path)

  entries << {
    "path" => relative_path,
    "tier" => tier,
    "classificationRuleId" => matched_rule.fetch("id"),
    "domainId" => domain_ownership.fetch("id"),
    "ownerId" => domain_ownership.fetch("owner_id"),
    "registeredInSourceOfTruth" => registered_in_source_of_truth,
    "sourceRefId" => source_ref,
    "documentationCovered" => documentation_covered,
    "mutatingServiceCandidate" => mutating_service,
    "serviceWorkflowCovered" => mutating_service ? workflow_inventory_covered : nil,
    "currentGate" => fail_hard_tiers.include?(tier) ? "fail_hard" : "report_only"
  }

  executor_critical_without_source_ref << relative_path if tier == "executor_critical" && !registered_in_source_of_truth
  automation_relevant_without_source_ref << relative_path if tier == "automation_relevant" && !registered_in_source_of_truth
  strict_source_of_truth_missing << relative_path if strict_source_of_truth_rule_ids.include?(matched_rule.fetch("id")) && !registered_in_source_of_truth
  strict_documentation_missing << relative_path if strict_documentation_rule_ids.include?(matched_rule.fetch("id")) && !documentation_covered
end

report = {
  "generatedAt" => Time.now.utc.iso8601,
  "summary" => {
    "totalFiles" => entries.length + unclassified_paths.length + unowned_domain_paths.length,
    "classifiedFiles" => entries.length,
    "unclassifiedFileCount" => unclassified_paths.length,
    "unownedDomainFileCount" => unowned_domain_paths.length,
    "tierCounts" => tier_ids.to_h { |tier_id| [tier_id, entries.count { |entry| entry["tier"] == tier_id }] },
    "domainCounts" => tally_values(entries.map { |entry| entry["domainId"] }),
    "ownerCounts" => tally_values(entries.map { |entry| entry["ownerId"] }),
    "executorCriticalWithoutSourceRefCount" => executor_critical_without_source_ref.length,
    "automationRelevantWithoutSourceRefCount" => automation_relevant_without_source_ref.length,
    "strictSourceOfTruthMissingCount" => strict_source_of_truth_missing.length,
    "strictDocumentationMissingCount" => strict_documentation_missing.length
  },
  "entries" => entries,
  "unclassifiedPaths" => unclassified_paths,
  "unownedDomainPaths" => unowned_domain_paths,
  "executorCriticalWithoutSourceRef" => executor_critical_without_source_ref,
  "automationRelevantWithoutSourceRef" => automation_relevant_without_source_ref,
  "strictSourceOfTruthMissing" => strict_source_of_truth_missing,
  "strictDocumentationMissing" => strict_documentation_missing
}

FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
File.write(OUTPUT_PATH, JSON.pretty_generate(report) + "\n")
puts "Generated #{OUTPUT_PATH}"
