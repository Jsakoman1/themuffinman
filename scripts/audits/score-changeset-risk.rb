#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../local_tooling_common"

def changed_files(argv)
  files = argv.reject { |arg| arg.start_with?("--") || arg.include?("=") }
  files = LocalToolingCommon.git_changed_files if files.empty?
  files.uniq
end

def category(path)
  LocalToolingCommon.path_category(path)
end

def domain(path)
  LocalToolingCommon.domain_for_path(path)
end

def content(path)
  File.read(File.join(LocalToolingCommon::REPO_ROOT, path))
rescue Errno::ENOENT, Errno::EISDIR
  ""
end

def factor(id, weight, files, reason)
  return nil if files.empty?

  {id: id, weight: weight, file_count: files.size, files: files.first(25), reason: reason}
end

files = changed_files(ARGV)
categories = files.group_by { |path| category(path) }
domains = files.map { |path| domain(path) }.reject { |value| %w[unknown common docs testing].include?(value) }.uniq.sort
generated = files.select { |path| LocalToolingCommon.generated_path?(path) || path.start_with?("docs/generated/") }
service_logic = files.select do |path|
  category(path) == "backend_service" &&
    content(path).match?(/\b(permission|allowed|authorize|workflow|transition|state|visibility|validate|eligib|apply|approve|decline|withdraw)\b/i)
end

factors = [
  factor("controller_contract_change", 20, categories.fetch("backend_controller", []), "Controller mappings can change endpoint contracts."),
  factor("dto_or_model_contract_change", 18, categories.fetch("backend_dto", []) + categories.fetch("backend_model", []), "DTO/model changes can affect API, frontend, persistence, or automation contracts."),
  factor("migration_or_schema_change", 18, files.select { |path| path.include?("/db/migration/") }, "Schema changes require migration and documentation review."),
  factor("service_workflow_or_permission_logic", 18, service_logic, "Service changes mention workflow, permission, validation, state, or visibility logic."),
  factor("frontend_contract_or_api_change", 12, categories.fetch("frontend_api", []) + categories.fetch("frontend_contract", []), "Frontend API/contract surfaces can drift from backend contracts."),
  factor("agent_or_docs_contract_change", 12, files.select { |path| path.start_with?("docs/agent-operating-model") || path == "docs/documentation-sync-policy.md" || path == "docs/change-completion-checklist.md" }, "Agent operating docs and sync policies affect automation safety."),
  factor("generated_artifact_churn", generated.size > 10 ? 10 : 0, generated, "Large generated artifact churn needs commit-scope review."),
  factor("mixed_product_domains", domains.size > 1 ? 10 : 0, domains, "Multiple product domains in one changeset increase review scope."),
  factor("tooling_or_infrastructure_change", 6, files.select { |path| path.start_with?("scripts/") || path == "Makefile" || path.end_with?("package.json") }, "Tooling and infrastructure changes can affect validation behavior.")
].compact.reject { |entry| entry[:weight].zero? }

score = factors.sum { |entry| entry[:weight] }
tier = if score >= 60
  "high"
elsif score >= 25
  "medium"
else
  "low"
end

report = {
  generated_at: Time.now.utc.iso8601,
  changed_file_count: files.size,
  score: score,
  risk_tier: tier,
  factors: factors,
  recommended_commands: [
    "make audit-generated-commit-scope",
    "make audit-router",
    tier == "high" ? "make audit-agent-safety" : nil,
    generated.any? ? "make audit-generated-artifact-freshness" : nil
  ].compact.uniq
}

LocalToolingCommon.write_json("docs/generated/local-tooling/changeset-risk.json", report)
lines = ["# Changeset Risk", ""]
lines << "- Changed files: `#{report[:changed_file_count]}`"
lines << "- Score: `#{score}`"
lines << "- Risk tier: `#{tier}`"
lines << ""
factors.sort_by { |entry| -entry[:weight] }.first(6).each do |entry|
  lines << "- `#{entry[:id]}` +#{entry[:weight]} (`#{entry[:file_count]}`)"
end
lines << ""
LocalToolingCommon.write_text("docs/generated/local-tooling/changeset-risk-summary.md", lines.join("\n"))

puts "Changeset risk"
puts "  changed files: #{files.size}"
puts "  score: #{score}"
puts "  tier: #{tier}"
