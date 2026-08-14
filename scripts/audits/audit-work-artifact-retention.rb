#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "time"

ROOT = File.expand_path("../..", __dir__)
POLICY_PATH = File.join(ROOT, "docs/work-artifact-retention-policy.yaml")

def executable_kind?(kind)
  %w[work master execution-inventory execution_inventory].include?(kind)
end

def classification(artifact, externally_referenced)
  kind = artifact["kind"]
  status = artifact["status"]
  return "historical_contract" unless executable_kind?(kind)
  return "active_or_draft" if %w[active draft].include?(status)
  return "externally_referenced_verified" if status == "verified" && externally_referenced
  return "unreferenced_verified" if status == "verified"

  "other_status"
end

def reference_sources
  paths = [File.join(ROOT, "AGENTS.md"), File.join(ROOT, "Makefile")]
  paths.concat(Dir[File.join(ROOT, "scripts/**/*")])
  paths.concat(Dir[File.join(ROOT, "docs/**/*")].reject do |path|
    path.start_with?(File.join(ROOT, "docs/work/"), File.join(ROOT, "docs/audit-output/"), File.join(ROOT, "docs/runtime-evidence/"))
  end)
  paths.select { |path| File.file?(path) }
end

def referenced_paths
  corpus = reference_sources.map { |path| File.read(path, mode: "rb").force_encoding("UTF-8") }.join("\n")
  Dir[File.join(ROOT, "docs/work/*.yaml")].each_with_object({}) do |path, result|
    relative = path.delete_prefix("#{ROOT}/")
    result[relative] = corpus.include?(relative)
  end
end

def report
  references = referenced_paths
  entries = Dir[File.join(ROOT, "docs/work/*.yaml")].sort.map do |path|
    artifact = YAML.load_file(path)
    relative = path.delete_prefix("#{ROOT}/")
    category = classification(artifact, references.fetch(relative))
    {
      "path" => relative,
      "id" => artifact["id"],
      "kind" => artifact["kind"],
      "status" => artifact["status"],
      "externally_referenced" => references.fetch(relative),
      "classification" => category,
      "action" => category == "unreferenced_verified" ? "review_candidate" : "retain"
    }
  end
  {
    "kind" => "work_artifact_retention_review",
    "version" => 2,
    "generated_at" => Time.now.utc.iso8601,
    "policy" => "docs/work-artifact-retention-policy.yaml",
    "deletion_performed" => false,
    "summary" => entries.group_by { |entry| entry.fetch("classification") }.transform_values(&:length).sort.to_h,
    "artifacts_reviewed" => entries.length,
    "detail_audit" => "Git history and an exact deletion manifest; this compact review intentionally retains no per-path history."
  }
end

def verify_report(path)
  policy = YAML.load_file(POLICY_PATH)
  document = YAML.load_file(path)
  failures = []
  failures << "retention policy kind is invalid" unless policy["kind"] == "work_artifact_retention_policy"
  failures << "retention policy must prohibit inline deletion" unless policy.dig("candidate_contract", "deletion_authority") == "separate_user_authorized_cleanup_task"
  failures << "report kind is invalid" unless document["kind"] == "work_artifact_retention_review"
  failures << "report points to the wrong policy" unless document["policy"] == "docs/work-artifact-retention-policy.yaml"
  failures << "report must not record deletion" unless document["deletion_performed"] == false
  if document["version"] == 2
    failures << "compact report must declare an artifact count" unless document["artifacts_reviewed"].is_a?(Integer)
    failures << "compact report must not retain per-path entries" if document.key?("entries")
    abort "Work-artifact retention audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
    puts "Work-artifact retention audit passed (compact review, #{document.fetch("artifacts_reviewed")} artifacts, no deletion)."
    return
  end
  entries = Array(document["entries"])
  failures << "report paths are duplicated" unless entries.map { |entry| entry["path"] }.uniq.length == entries.length
  failures << "report entries are not sorted" unless entries.map { |entry| entry["path"] } == entries.map { |entry| entry["path"] }.sort
  failures << "report summary disagrees" unless document["summary"] == entries.group_by { |entry| entry.fetch("classification") }.transform_values(&:length).sort.to_h
  entries.each do |entry|
    expected = classification({ "kind" => entry["kind"], "status" => entry["status"] }, entry["externally_referenced"])
    failures << "invalid classification for #{entry["path"]}" unless entry["classification"] == expected
    expected_action = expected == "unreferenced_verified" ? "review_candidate" : "retain"
    failures << "invalid action for #{entry["path"]}" unless entry["action"] == expected_action
  end
  abort "Work-artifact retention audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?

  puts "Work-artifact retention audit passed (#{entries.length} artifacts, #{document.fetch("summary").fetch("unreferenced_verified", 0)} review candidates, no deletion)."
end

if ARGV == ["--check-fixtures"]
  abort "active fixture failed" unless classification({ "kind" => "work", "status" => "active" }, false) == "active_or_draft"
  abort "historical fixture failed" unless classification({ "kind" => "runtime_scenario_catalog", "status" => "reviewed" }, false) == "historical_contract"
  abort "referenced fixture failed" unless classification({ "kind" => "work", "status" => "verified" }, true) == "externally_referenced_verified"
  abort "candidate fixture failed" unless classification({ "kind" => "work", "status" => "verified" }, false) == "unreferenced_verified"
  puts "Work-artifact retention fixtures passed (active, historical, referenced, and candidate classes)."
  exit 0
end

if ARGV.first == "--write"
  destination = ARGV[1]
  abort "usage: ruby scripts/audits/audit-work-artifact-retention.rb --write <report-path>" if destination.to_s.empty? || ARGV.length != 2
  File.write(File.join(ROOT, destination), YAML.dump(report).sub(/\A---\n/, ""))
  puts "Work-artifact retention review written (no deletion)."
  exit 0
end

abort "usage: ruby scripts/audits/audit-work-artifact-retention.rb [--check-fixtures|--write <report-path>|--check <report-path>]" unless ARGV.first == "--check" && ARGV.length == 2
verify_report(File.join(ROOT, ARGV.last))
