#!/bin/zsh
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: scripts/feature-closeout-audit.sh <manifest-file>" >&2
  exit 1
fi

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
manifest_arg="$1"
manifest_path="$repo_root/$manifest_arg"

if [[ ! -f "$manifest_path" ]]; then
  echo "manifest not found: $manifest_path" >&2
  exit 1
fi

ruby -ryaml - "$repo_root" "$manifest_arg" <<'RUBY'
repo_root = ARGV.fetch(0)
manifest_arg = ARGV.fetch(1)
manifest_path = File.join(repo_root, manifest_arg)
manifest = YAML.load_file(manifest_path)
errors = []

def list(value)
  value.is_a?(Array) ? value.compact : []
end

def commands(manifest)
  list(manifest.dig("validationEvidence", "commands"))
end

def passed_command?(manifest, pattern)
  commands(manifest).any? do |entry|
    entry.is_a?(Hash) &&
      entry["result"] == "passed" &&
      entry["command"].to_s.match?(pattern)
  end
end

def require_passed_command(manifest, errors, pattern, description)
  errors << "missing passed validation evidence for #{description}" unless passed_command?(manifest, pattern)
end

def require_bool(manifest, errors, key)
  value = manifest.dig("checklist", key)
  errors << "checklist.#{key} must be true for completed closeout" unless value == true
end

plan_file = manifest["planFile"].to_s
plan_path = File.join(repo_root, plan_file)
status = manifest["status"].to_s
risk_tier = manifest["riskTier"].to_s
profiles = list(manifest["changeProfiles"])
checklist = manifest["checklist"] || {}
artifact_groups = manifest["artifacts"] || {}
evidence_commands = commands(manifest)

errors << "manifest status must be complete for closeout" unless status == "complete"
errors << "manifest is missing planFile" if plan_file.empty?
errors << "referenced plan file is missing: #{plan_file}" unless !plan_file.empty? && File.exist?(plan_path)
errors << "validationEvidence.commands must contain at least one command" if evidence_commands.empty?

evidence_commands.each_with_index do |entry, index|
  unless entry.is_a?(Hash)
    errors << "validationEvidence.commands[#{index}] must be an object"
    next
  end
  result = entry["result"].to_s
  command = entry["command"].to_s
  summary = entry["summary"].to_s
  errors << "validationEvidence.commands[#{index}].command is required" if command.empty?
  errors << "validationEvidence.commands[#{index}].summary is required" if summary.empty?
  errors << "validationEvidence.commands[#{index}] records failed evidence: #{command}" if result == "failed"
  if %w[skipped not_applicable].include?(result) && entry["skippedReason"].to_s.empty?
    errors << "validationEvidence.commands[#{index}].skippedReason is required when result is #{result}"
  end
end

if status == "complete"
  %w[
    tempPlanCreated
    codeImplemented
    backendTestsPassed
    docsSynced
    agentModelSynced
    destructivePolicyChecked
    multilingualCoverageChecked
  ].each { |key| require_bool(manifest, errors, key) }

  errors << "backlog.reviewed must be true for completed closeout" unless manifest.dig("backlog", "reviewed") == true
  errors << "planCompletion.reviewed must be true for completed closeout" unless manifest.dig("planCompletion", "reviewed") == true
  errors << "planCompletion.openTasks must be 0 for completed closeout" unless manifest.dig("planCompletion", "openTasks").to_i == 0
  errors << "closeoutDecision.status must be ready for completed closeout" unless manifest.dig("closeoutDecision", "status").to_s == "ready"

  if File.exist?(plan_path)
    open_tasks = File.readlines(plan_path).count { |line| line.start_with?("- [ ]") }
    errors << "referenced plan still has #{open_tasks} open task(s)" unless open_tasks.zero?
    plan_content = File.read(plan_path)
    completion_section = plan_content[/^## Completion Evidence\s*$([\s\S]*?)(?=^## |\z)/, 1]
    completion_status = completion_section && completion_section[/^\s*-\s*Status:\s*(.+?)\s*$/i, 1]
    errors << "referenced plan is missing ## Completion Evidence" unless completion_section
    if completion_status.to_s.empty? || completion_status.match?(/\A(TBD|draft|not started|unknown)\z/i)
      errors << "referenced plan completion evidence status is missing or not final"
    end
  end

  require_passed_command(manifest, errors, /make audit-todo/, "make audit-todo")
  if checklist["backendTestsPassed"] == true || profiles.include?("backend-logic")
    require_passed_command(manifest, errors, %r{(\./mvnw test|mvnw .* test|make audit-agent-safety)}, "backend test coverage")
  end
  if checklist["frontendValidationPassed"] == true || profiles.include?("frontend-contract")
    require_bool(manifest, errors, "frontendValidationPassed")
    require_passed_command(manifest, errors, /npm run type-check/, "frontend type-check")
    require_passed_command(manifest, errors, /npm run build/, "frontend build")
  end
  if %w[high executor-critical].include?(risk_tier)
    require_passed_command(manifest, errors, /make audit-agent-safety/, "high-risk agent safety audit")
  end
  if profiles.include?("agent-contract")
    require_passed_command(manifest, errors, /make audit-agent-safety/, "agent-contract safety audit")
    require_passed_command(manifest, errors, /make generate-agent-(operating-model|artifacts)/, "agent-contract generated model or artifacts")
  end
  if profiles.include?("frontend-contract")
    require_passed_command(manifest, errors, /npm run validate:contracts/, "frontend contract validation")
    require_passed_command(manifest, errors, /npm run type-check/, "frontend-contract type-check")
    require_passed_command(manifest, errors, /npm run build/, "frontend-contract build")
  end
  if profiles.include?("workflow-expansion")
    require_passed_command(manifest, errors, /(ScenarioTest|UseCaseContractTest)/, "workflow scenario or use-case contract test")
  end
end

all_paths = []
artifact_groups.each do |group, paths|
  next unless paths.is_a?(Array)

  paths.each do |path|
    all_paths << [group, path]
  end
end

duplicates = all_paths
  .group_by { |_group, path| path }
  .select { |_path, rows| rows.size > 1 }
duplicates.each do |path, rows|
  errors << "artifact path appears in multiple buckets: #{path} (#{rows.map(&:first).join(', ')})"
end

puts "Feature close-out audit"
puts "  Manifest: #{manifest_path}"
puts "  Plan:     #{plan_path}"
puts "  Status:   #{status.empty? ? 'unknown' : status}"
puts "  Risk:     #{risk_tier.empty? ? 'unknown' : risk_tier}"
puts "  Profiles: #{profiles.empty? ? 'none' : profiles.join(',')}"
puts "  Evidence: #{evidence_commands.size} command(s)"

if errors.any?
  warn "Feature close-out audit failed:"
  errors.each { |error| warn "  - #{error}" }
  exit 1
end

puts "Feature close-out audit passed"
RUBY

ruby "$repo_root/scripts/todo-audit.rb" --manifest "$manifest_arg"
