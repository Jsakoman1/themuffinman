#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../local_tooling_common"

OUT_JSON = "docs/generated/local-tooling/manifest-decision.json"
OUT_MD = "docs/generated/local-tooling/manifest-decision-summary.md"

def options(argv)
  argv.each_with_object({}) do |arg, parsed|
    key, value = arg.split("=", 2)
    parsed[key] = value if value
  end
end

def files_from_args(argv, parsed)
  files = []
  files += parsed["files"].to_s.split(",") if parsed["files"]
  files += argv.reject { |arg| arg.start_with?("--") || arg.include?("=") }
  files = LocalToolingCommon.git_changed_files if files.empty?
  files.map(&:strip).reject(&:empty?).uniq.select { |path| File.file?(File.join(LocalToolingCommon::REPO_ROOT, path)) }
end

def category(path)
  LocalToolingCommon.path_category(path)
end

def domain(path)
  LocalToolingCommon.domain_for_path(path)
end

def generated?(path)
  LocalToolingCommon.generated_path?(path) || path.start_with?("docs/generated/")
end

def content(path)
  File.read(File.join(LocalToolingCommon::REPO_ROOT, path))
rescue Errno::ENOENT, Errno::EISDIR
  ""
end

def rule(id, required, files, reason)
  return nil if files.empty?

  {id: id, requires_manifest: required, file_count: files.size, files: files.first(25), reason: reason}
end

parsed = options(ARGV)
files = files_from_args(ARGV, parsed)
categories = files.group_by { |path| category(path) }
domains = files.map { |path| domain(path) }.reject { |value| %w[unknown common docs testing].include?(value) }.uniq.sort
runtime_files = files.reject { |path| generated?(path) || path.start_with?(".agents/") || path.start_with?("docs/") || path.start_with?("scripts/") }
logic_files = files.select do |path|
  text = content(path)
  path.match?(%r{/(service|controller|model|dto|repository|db/migration)/}) ||
    text.match?(/\b(permission|workflow|transition|state|visibility|validation|contract|manifest|agent|automation|sandbox)\b/i)
end
schema_files = files.select { |path| path.include?("/db/migration/") || path.end_with?(".schema.json") }
agent_contract_files = files.select { |path| path.start_with?("docs/agent-operating-model") || path.include?("AdminAgent") || path.include?("agent/") }
frontend_contract_files = files.select { |path| path.include?("/frontend/src/contracts/") || path.include?("/frontend/scripts/generate") || path.include?("/frontend/src/modules/") && content(path).include?("api") }
generated_files = files.select { |path| generated?(path) }
automation_safety_files = files.select do |path|
  path.start_with?("scripts/") ||
    path == "Makefile" ||
    path.include?("feature-closeout") ||
    path.include?("validation-evidence") ||
    path.include?("todo-audit") ||
    path.include?("documentation-sync")
end
workflow_files = files.select { |path| content(path).match?(/\b(workflow|transition|state machine|ScenarioTest|UseCaseContractTest)\b/i) }

rules = [
  rule("multi_file_multi_layer_change", true, files.size > 1 && categories.keys.size > 1 ? files : [], "Multiple files across multiple change categories need a manifest for closeout traceability."),
  rule("high_risk_or_executor_critical_surface", true, logic_files + automation_safety_files, "Backend logic, automation safety, or validation tooling changes need explicit evidence."),
  rule("agent_contract_change", true, agent_contract_files, "Agent-facing contracts and operating-model surfaces require manifest tracking."),
  rule("workflow_expansion_change", true, workflow_files, "Workflow or scenario surfaces require scenario evidence and docs synchronization."),
  rule("frontend_contract_change", true, frontend_contract_files, "Frontend contract or API surfaces require frontend validation evidence."),
  rule("schema_or_generated_artifact_change", true, schema_files + generated_files, "Schema and generated-artifact changes require explicit generated-artifact evidence."),
  rule("mixed_product_domains", true, domains.size > 1 ? files : [], "Multiple product domains in one changeset require explicit scope and residual-risk review.")
].compact

allow_skip = files.size <= 1 &&
  rules.none? { |entry| entry[:requires_manifest] } &&
  files.all? { |path| category(path) == "docs" || category(path) == "script" || path.end_with?(".md") }

decision = if rules.any? { |entry| entry[:requires_manifest] }
  "required"
elsif allow_skip
  "not_required"
else
  "review"
end

skip_reason = if decision == "not_required"
  "Cosmetic or single-surface contract-neutral change; no manifest required if the human closeout notes the reason."
elsif decision == "review"
  "No hard manifest rule matched, but the changeset is not a simple cosmetic single-file change."
else
  nil
end

report = {
  generated_at: Time.now.utc.iso8601,
  changed_file_count: files.size,
  categories: categories.keys.sort,
  domains: domains,
  decision: decision,
  manifest_required: decision == "required",
  skip_allowed: decision == "not_required",
  skip_reason: skip_reason,
  rules: rules,
  files_considered: files.first(120)
}

LocalToolingCommon.write_json(OUT_JSON, report)
lines = ["# Manifest Decision", ""]
lines << "- Changed files: `#{files.size}`"
lines << "- Decision: `#{decision}`"
lines << "- Manifest required: `#{report[:manifest_required]}`"
lines << "- Skip allowed: `#{report[:skip_allowed]}`"
lines << "- Skip reason: #{skip_reason}" if skip_reason
lines << ""
rules.each do |entry|
  lines << "- `#{entry[:id]}` files=#{entry[:file_count]} reason=#{entry[:reason]}"
end
LocalToolingCommon.write_text(OUT_MD, lines.join("\n") + "\n")

puts "Manifest decision"
puts "  changed files: #{files.size}"
puts "  decision: #{decision}"
puts "  manifest required: #{report[:manifest_required]}"
