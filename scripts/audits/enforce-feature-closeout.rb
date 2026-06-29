#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require "time"
require "yaml"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT
OUT_ROOT = "docs/generated/local-tooling/closeout-enforcement"
REQUIRED_TOP_LEVEL = %w[featureId title status riskTier changeMode changeImpact changeProfiles planFile backlog checklist artifacts validationEvidence generatedArtifacts planCompletion closeoutDecision].freeze
ALLOWED_TOP_LEVEL = (REQUIRED_TOP_LEVEL + %w[summary docDelta]).freeze
REQUIRED_CHECKLIST = %w[tempPlanCreated codeImplemented backendTestsPassed frontendValidationPassed docsSynced agentModelSynced destructivePolicyChecked multilingualCoverageChecked].freeze
ARTIFACT_BUCKETS = %w[codePaths docPaths testPaths generatorCommands auditCommands].freeze
REQUIRED_PROFILES = {
  "backend-logic" => [%r{(\./mvnw test|mvnw .* test|make audit-agent-safety)}],
  "agent-contract" => [/make audit-agent-safety/, /make generate-agent-(operating-model|artifacts)/],
  "frontend-contract" => [/npm run validate:contracts/, /npm run type-check/, /npm run build/],
  "workflow-expansion" => [/(ScenarioTest|UseCaseContractTest)/]
}.freeze

def options(argv)
  argv.each_with_object({}) do |arg, parsed|
    key, value = arg.split("=", 2)
    parsed[key] = value if value
  end
end

def abs(path)
  File.join(REPO_ROOT, path.to_s)
end

def list(value)
  value.is_a?(Array) ? value.compact : []
end

def feature_id(manifest, manifest_path)
  raw = manifest["featureId"].to_s
  raw = File.basename(manifest_path.to_s, ".yaml").sub(/-manifest\z/, "") if raw.empty?
  raw.gsub(/[^A-Za-z0-9_-]+/, "-")
end

def validation_commands(manifest)
  list(manifest.dig("validationEvidence", "commands"))
end

def passed_command?(manifest, pattern)
  validation_commands(manifest).any? do |entry|
    entry.is_a?(Hash) && entry["result"].to_s == "passed" && entry["command"].to_s.match?(pattern)
  end
end

def not_applicable_evidence?(manifest, key)
  validation_commands(manifest).any? do |entry|
    next false unless entry.is_a? Hash
    next false unless entry["result"].to_s == "not_applicable"

    "#{entry["command"]} #{entry["summary"]} #{entry["skippedReason"]}".downcase.include?(key.downcase)
  end
end

def completion_section(content)
  match = content.match(/^## Completion Evidence\s*$([\s\S]*?)(?=^## |\z)/)
  match && match[1]
end

def weak_status?(status)
  status.to_s.empty? || status.to_s.match?(/\A(TBD|draft|not started|unknown|implementation complete, validation pending)\z/i)
end

def open_task_lines(content)
  content.lines.each_with_index.each_with_object([]) do |(line, index), rows|
    rows << {line: index + 1, text: line.strip} if line.start_with?("- [ ]")
  end
end

def backlog_open_ids
  paths = %w[docs/implementation-backlog.md docs/agent-improvement-backlog.md docs/codex-local-tooling-todo.md]
  paths.each_with_object(Set.new) do |path, ids|
    next unless File.exist?(abs(path))

    File.readlines(abs(path)).each do |line|
      ids << Regexp.last_match(1) if line.match?(/^\s*-\s*\[\s\]\s+([A-Z0-9_-]+):/)
    end
  end
end

def add_schema_issues(manifest, issues)
  (REQUIRED_TOP_LEVEL - manifest.keys).each { |key| issues << "manifest is missing required key: #{key}" }
  (manifest.keys - ALLOWED_TOP_LEVEL).each { |key| issues << "manifest has unsupported key: #{key}" }
  issues << "changeProfiles must contain at least one profile" if list(manifest["changeProfiles"]).empty?
  issues << "validationEvidence.commands must contain at least one command" if validation_commands(manifest).empty?
  REQUIRED_CHECKLIST.each do |key|
    value = manifest.dig("checklist", key)
    issues << "checklist.#{key} is missing" unless value == true || value == false
  end
  ARTIFACT_BUCKETS.each do |key|
    issues << "artifacts.#{key} must be an array" unless manifest.dig("artifacts", key).is_a?(Array)
  end
end

def add_validation_evidence_issues(manifest, issues)
  validation_commands(manifest).each_with_index do |entry, index|
    unless entry.is_a?(Hash)
      issues << "validationEvidence.commands[#{index}] must be an object"
      next
    end
    command = entry["command"].to_s
    result = entry["result"].to_s
    summary = entry["summary"].to_s
    issues << "validationEvidence.commands[#{index}].command is required" if command.empty?
    issues << "validationEvidence.commands[#{index}].summary is required" if summary.empty?
    issues << "validationEvidence.commands[#{index}] records failed evidence: #{command}" if result == "failed"
    if %w[skipped not_applicable].include?(result) && entry["skippedReason"].to_s.empty?
      issues << "validationEvidence.commands[#{index}].skippedReason is required when result is #{result}"
    end
  end
end

def add_plan_issues(manifest, issues)
  plan_file = manifest["planFile"].to_s
  plan_path = abs(plan_file)
  issues << "manifest is missing planFile" if plan_file.empty?
  issues << "referenced plan file is missing: #{plan_file}" unless !plan_file.empty? && File.exist?(plan_path)
  return unless File.exist?(plan_path)

  content = File.read(plan_path)
  open_tasks = open_task_lines(content)
  section = completion_section(content)
  status = section && section[/^\s*-\s*Status:\s*(.+?)\s*$/i, 1]&.strip
  issues << "referenced plan still has #{open_tasks.size} open task(s)" unless open_tasks.empty?
  issues << "referenced plan is missing ## Completion Evidence" unless section
  issues << "referenced plan completion evidence status is missing or not final" if weak_status?(status)
  issues << "planCompletion.reviewed must be true for completed closeout" unless manifest.dig("planCompletion", "reviewed") == true
  issues << "planCompletion.openTasks must be 0 for completed closeout" unless manifest.dig("planCompletion", "openTasks").to_i.zero?
end

def add_checklist_issues(manifest, issues)
  REQUIRED_CHECKLIST.each do |key|
    value = manifest.dig("checklist", key)
    next if value == true
    next if value == false && not_applicable_evidence?(manifest, key)

    issues << "checklist.#{key} must be true or have not_applicable validation evidence for completed closeout"
  end
end

def add_profile_issues(manifest, issues)
  list(manifest["changeProfiles"]).each do |profile|
    REQUIRED_PROFILES.fetch(profile, []).each do |pattern|
      issues << "missing passed validation evidence for #{profile}: #{pattern.inspect}" unless passed_command?(manifest, pattern)
    end
  end
  if %w[high executor-critical].include?(manifest["riskTier"].to_s)
    issues << "missing passed validation evidence for high-risk agent safety audit" unless passed_command?(manifest, /make audit-agent-safety/)
  end
  issues << "missing passed validation evidence for make audit-todo" unless passed_command?(manifest, /make audit-todo/)
end

def add_artifact_issues(manifest, manifest_path, issues)
  artifact_groups = manifest["artifacts"] || {}
  all_paths = []
  ARTIFACT_BUCKETS.each do |group|
    list(artifact_groups[group]).each { |path| all_paths << [group, path] }
  end
  all_paths.group_by { |_group, path| path }.select { |_path, rows| rows.size > 1 }.each do |path, rows|
    issues << "artifact path appears in multiple buckets: #{path} (#{rows.map(&:first).join(', ')})"
  end

  manifest_mtime = File.exist?(abs(manifest_path)) ? File.mtime(abs(manifest_path)) : Time.at(0)
  refreshed = list(manifest.dig("generatedArtifacts", "refreshedPaths"))
  refreshed.each do |path|
    full_path = abs(path)
    if !File.exist?(full_path)
      issues << "generated artifact path is missing: #{path}"
    elsif File.mtime(full_path) < manifest_mtime
      issues << "generated artifact path is older than manifest: #{path}"
    end
  end
  if refreshed.empty? && manifest.dig("generatedArtifacts", "notApplicableReason").to_s.empty?
    issues << "generatedArtifacts.notApplicableReason is required when no refreshedPaths are listed"
  end
end

def add_backlog_issues(manifest, issues)
  open_ids = backlog_open_ids
  created = list(manifest.dig("backlog", "createdIds"))
  resolved = list(manifest.dig("backlog", "resolvedIds"))
  issues << "backlog.reviewed must be true for completed closeout" unless manifest.dig("backlog", "reviewed") == true
  created.each { |id| issues << "backlog.createdIds includes #{id}, but that ID is not open in persistent backlog" unless open_ids.include?(id) }
  resolved.each { |id| issues << "backlog.resolvedIds includes #{id}, but that ID is still open in persistent backlog" if open_ids.include?(id) }
end

def write_report(feature_id, payload)
  json_path = "#{OUT_ROOT}/#{feature_id}.json"
  summary_path = "#{OUT_ROOT}/#{feature_id}-summary.md"
  LocalToolingCommon.write_json(json_path, payload)
  lines = ["# Closeout Enforcement #{feature_id}", ""]
  lines << "- Status: `#{payload[:status]}`"
  lines << "- Manifest: `#{payload[:manifest]}`"
  lines << "- Issues: `#{payload[:issues].size}`"
  lines << "- Warnings: `#{payload[:warnings].size}`"
  lines << ""
  if payload[:issues].any?
    lines << "## Issues"
    lines << ""
    payload[:issues].each { |issue| lines << "- #{issue}" }
    lines << ""
  end
  LocalToolingCommon.write_text(summary_path, lines.join("\n"))
  [json_path, summary_path]
end

parsed = options(ARGV)
manifest_path = parsed["manifest"]
if manifest_path.to_s.empty?
  warn "usage: ruby scripts/audits/enforce-feature-closeout.rb manifest=<manifest-file>"
  exit 1
end

manifest = File.exist?(abs(manifest_path)) ? YAML.load_file(abs(manifest_path)) : nil
manifest = {} unless manifest.is_a?(Hash)
issues = []
warnings = []
issues << "manifest not found: #{manifest_path}" unless File.exist?(abs(manifest_path))
id = feature_id(manifest, manifest_path)

add_schema_issues(manifest, issues)
if manifest["status"].to_s != "complete"
  issues << "manifest status must be complete for final closeout"
else
  issues << "closeoutDecision.status must be ready for completed closeout" unless manifest.dig("closeoutDecision", "status").to_s == "ready"
  add_validation_evidence_issues(manifest, issues)
  add_plan_issues(manifest, issues)
  add_checklist_issues(manifest, issues)
  add_profile_issues(manifest, issues)
  add_artifact_issues(manifest, manifest_path, issues)
  add_backlog_issues(manifest, issues)
end

payload = {
  generated_at: Time.now.utc.iso8601,
  feature_id: id,
  manifest: manifest_path,
  status: issues.empty? ? "passed" : "failed",
  issues: issues.uniq,
  warnings: warnings,
  checks: {
    schema: true,
    complete_status: manifest["status"].to_s == "complete",
    validation_evidence_count: validation_commands(manifest).size,
    generated_artifact_count: list(manifest.dig("generatedArtifacts", "refreshedPaths")).size,
    profiles: list(manifest["changeProfiles"])
  }
}
json_path, summary_path = write_report(id, payload)

puts "Feature closeout enforcement"
puts "  feature: #{id}"
puts "  status: #{payload[:status]}"
puts "  issues: #{payload[:issues].size}"
puts "  json: #{json_path}"
puts "  summary: #{summary_path}"

if payload[:issues].any?
  warn "Feature closeout enforcement failed:"
  payload[:issues].each { |issue| warn "  - #{issue}" }
  exit 1
end
