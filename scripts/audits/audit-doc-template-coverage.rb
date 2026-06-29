#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../local_tooling_common"

OUT_JSON = "docs/generated/local-tooling/doc-template-coverage.json"
OUT_MD = "docs/generated/local-tooling/doc-template-coverage-summary.md"

TEMPLATES = {
  "workflow" => {
    template: ".agents/templates/docs/new-workflow.template.md",
    docs: ["docs/business-logic.md", "docs/domain-technical.md", "docs/agent-operating-model.md"],
    sections: ["Behavior Summary", "State Model", "Permissions And Visibility", "Validation And Edge Cases", "Validation Evidence"]
  },
  "endpoint" => {
    template: ".agents/templates/docs/new-endpoint.template.md",
    docs: ["docs/domain-technical.md", "docs/business-logic.md", "docs/agent-operating-model.md"],
    sections: ["Endpoint Contract", "Behavior Summary", "Permissions And Visibility", "Validation And Errors", "Validation Evidence"]
  },
  "dto-contract" => {
    template: ".agents/templates/docs/new-dto-contract.template.md",
    docs: ["docs/domain-technical.md", "docs/business-logic.md", "docs/agent-operating-model.md"],
    sections: ["DTO Contract", "Field Semantics", "Permissions And Visibility", "Compatibility Notes", "Validation Evidence"]
  },
  "schema-migration" => {
    template: ".agents/templates/docs/schema-migration.template.md",
    docs: ["docs/domain-technical.md", "docs/business-logic.md"],
    sections: ["Migration Summary", "Domain Impact", "Runtime Compatibility", "Validation Evidence"]
  },
  "permission-rule" => {
    template: ".agents/templates/docs/new-permission-rule.template.md",
    docs: ["docs/business-logic.md", "docs/domain-technical.md", "docs/agent-operating-model.md"],
    sections: ["Rule Summary", "Enforcement Point", "Visibility And Consent", "Failure Behavior", "Validation Evidence"]
  },
  "module" => {
    template: ".agents/templates/docs/new-module.template.md",
    docs: ["docs/business-logic.md", "docs/domain-technical.md", "docs/agent-operating-model.md"],
    sections: ["Module Purpose", "Core Entities", "Workflows", "Permissions And Visibility", "Validation Evidence"]
  }
}.freeze

SECTION_TERMS = {
  "Behavior Summary" => [/behavior/i, /product behavior/i, /overview/i],
  "State Model" => [/state model/i, /state transition/i, /workflow state/i, /lifecycle/i],
  "Permissions And Visibility" => [/permission/i, /visibility/i, /access/i, /consent/i],
  "Validation And Edge Cases" => [/validation/i, /edge case/i, /invalid transition/i],
  "Validation And Errors" => [/validation/i, /errors/i, /failure/i, /4xx/i],
  "Validation Evidence" => [/validation evidence/i, /tests?/i, /audit/i],
  "Endpoint Contract" => [/endpoint/i, /requestmapping/i, /\bGET\b|\bPOST\b|\bPUT\b|\bPATCH\b|\bDELETE\b/],
  "DTO Contract" => [/\bDTO\b/i, /contract/i, /response/i, /request/i],
  "Field Semantics" => [/field/i, /nullability/i, /semantics/i],
  "Compatibility Notes" => [/compatibility/i, /contract generation/i, /additive/i],
  "Migration Summary" => [/migration/i, /flyway/i, /schema/i],
  "Domain Impact" => [/domain impact/i, /entity/i, /workflow/i, /invariant/i],
  "Runtime Compatibility" => [/runtime compatibility/i, /nullable/i, /rollout/i, /backfill/i],
  "Rule Summary" => [/rule/i, /protected action/i, /allowed/i, /denied/i],
  "Enforcement Point" => [/enforcement/i, /service/i, /controller/i, /repository/i],
  "Visibility And Consent" => [/visibility/i, /consent/i, /ownership/i, /circle/i],
  "Failure Behavior" => [/failure/i, /forbidden/i, /not-found/i, /redaction/i],
  "Module Purpose" => [/module/i, /purpose/i, /serves/i],
  "Core Entities" => [/core entities/i, /entities/i, /ownership/i],
  "Workflows" => [/workflow/i, /create/i, /approval/i, /booking/i]
}.freeze

def changed_files
  LocalToolingCommon.git_changed_files.select { |path| File.file?(File.join(LocalToolingCommon::REPO_ROOT, path)) }
end

def files_from_args
  raw = ARGV.find { |arg| arg.start_with?("files=") }&.split("=", 2)&.last
  files = raw ? raw.split(",") : ARGV.reject { |arg| arg.include?("=") || arg.start_with?("--") }
  files.map(&:strip).reject(&:empty?).select { |path| File.file?(File.join(LocalToolingCommon::REPO_ROOT, path)) }
end

def read(path)
  File.read(File.join(LocalToolingCommon::REPO_ROOT, path))
rescue Errno::ENOENT
  ""
end

def detect_change_types(path, content)
  types = []
  types << "endpoint" if path.end_with?("Controller.java") || content.match?(/@(Get|Post|Put|Patch|Delete)Mapping|@RequestMapping/)
  types << "dto-contract" if path.include?("/dto/") || path.match?(/(?:DTO|Request|Response)\.java\z/)
  types << "schema-migration" if path.include?("/db/migration/") || path.end_with?(".sql")
  types << "workflow" if path.include?("/service/") && content.match?(/Status|Transition|Workflow|UseCase|state|approve|decline|withdraw|complete|cancel/i)
  types << "permission-rule" if content.match?(/permission|visibility|forbidden|deny|allowed|owner|admin|consent|circle/i)
  types << "module" if path.match?(%r{apps/themuffinman/src/main/java/com/themuffinman/app/[^/]+/(controller|service|model|dto)/}) && content.match?(/@Entity|@RestController|Service/)
  types.uniq
end

def doc_text(paths)
  paths.map { |path| [path, read(path)] }.to_h
end

def section_present?(section, docs)
  patterns = SECTION_TERMS.fetch(section, [/#{Regexp.escape(section)}/i])
  docs.values.any? do |content|
    patterns.any? { |pattern| content.match?(pattern) }
  end
end

def row_for(type, files)
  spec = TEMPLATES.fetch(type)
  docs = doc_text(spec[:docs])
  section_rows = spec[:sections].map do |section|
    {section: section, present: section_present?(section, docs)}
  end
  {
    change_type: type,
    template: spec[:template],
    files: files.first(25),
    docs_checked: spec[:docs],
    sections: section_rows,
    missing_sections: section_rows.reject { |row| row[:present] }.map { |row| row[:section] },
    status: section_rows.all? { |row| row[:present] } ? "covered" : "review"
  }
end

files = files_from_args
files = changed_files if files.empty?
typed = files.each_with_object(Hash.new { |hash, key| hash[key] = [] }) do |path, groups|
  content = read(path)
  detect_change_types(path, content).each { |type| groups[type] << path }
end
rows = typed.map { |type, paths| row_for(type, paths.uniq) }.sort_by { |row| row[:change_type] }
report = {
  generated_at: Time.now.utc.iso8601,
  file_count: files.size,
  files_considered: files.first(100),
  change_types: rows,
  review_needed: rows.select { |row| row[:status] == "review" },
  notes: [
    "This audit checks for template-section coverage signals in living docs; it does not prove prose quality.",
    "Use the referenced `.agents/templates/docs/*.template.md` file when a section is missing or ambiguous."
  ]
}

lines = ["# Doc Template Coverage", ""]
lines << "- Generated at: `#{report[:generated_at]}`"
lines << "- Files considered: `#{report[:file_count]}`"
lines << "- Change types: `#{rows.size}`"
lines << "- Review needed: `#{report[:review_needed].size}`"
lines << ""
rows.each do |row|
  lines << "## #{row[:change_type]}"
  lines << ""
  lines << "- Template: `#{row[:template]}`"
  lines << "- Status: `#{row[:status]}`"
  lines << "- Missing sections: #{row[:missing_sections].empty? ? '`none`' : row[:missing_sections].map { |section| "`#{section}`" }.join(', ')}"
  lines << ""
end

LocalToolingCommon.write_json(OUT_JSON, report)
LocalToolingCommon.write_text(OUT_MD, lines.join("\n"))
puts "Doc template coverage"
puts "  files considered: #{report[:file_count]}"
puts "  change types: #{rows.size}"
puts "  review needed: #{report[:review_needed].size}"
