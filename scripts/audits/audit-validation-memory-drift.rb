#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "pathname"
require "time"

ROOT = Pathname.new(__dir__).join("../..").expand_path
MARKDOWN_PATH = ROOT.join("docs/validation-memory.md")
JSON_PATH = ROOT.join("docs/validation-memory.json")
SUMMARY_PATH = ROOT.join("docs/generated/local-tooling/validation-memory-drift-summary.md")
JSON_REPORT_PATH = ROOT.join("docs/generated/local-tooling/validation-memory-drift.json")

markdown = File.exist?(MARKDOWN_PATH) ? File.read(MARKDOWN_PATH) : ""
json = File.exist?(JSON_PATH) ? JSON.parse(File.read(JSON_PATH)) : {}

issues = []
checks = []

def record_check(checks, label, ok, detail)
  checks << {"label" => label, "status" => ok ? "ok" : "drift", "detail" => detail}
end

unless File.exist?(MARKDOWN_PATH)
  issues << "Missing docs/validation-memory.md"
end

unless File.exist?(JSON_PATH)
  issues << "Missing docs/validation-memory.json"
end

canonical_commands = json.fetch("canonicalCommands", {})

{
  "frontendContract" => "frontend contract canonical commands",
  "backendLogic" => "backend logic canonical commands",
  "agentContract" => "agent contract canonical commands",
  "workflowExpansion" => "workflow expansion canonical commands",
  "closeout" => "closeout canonical commands"
}.each do |key, label|
  commands = Array(canonical_commands[key])
  commands.each do |command|
    ok = markdown.include?(command)
    issues << "Markdown is missing canonical command from JSON: #{command}" unless ok
    record_check(checks, label, ok, command)
  end
end

json_human_doc = json.dig("gatewayHints", "primaryHumanDoc").to_s
human_doc_ok = json_human_doc == "docs/validation-memory.md"
issues << "JSON primaryHumanDoc must equal docs/validation-memory.md" unless human_doc_ok
record_check(checks, "primary human doc", human_doc_ok, json_human_doc)

schema_ref_ok = markdown.include?("docs/validation-memory.schema.json")
issues << "Markdown is missing docs/validation-memory.schema.json reference" unless schema_ref_ok
record_check(checks, "schema reference", schema_ref_ok, "docs/validation-memory.schema.json")

gateway_note_ok = markdown.match?(/auto-include/i)
issues << "Markdown is missing gateway auto-include note" unless gateway_note_ok
record_check(checks, "gateway auto-include note", gateway_note_ok, "auto-include")

report = {
  "generated_at" => Time.now.utc.iso8601,
  "markdown" => MARKDOWN_PATH.relative_path_from(ROOT).to_s,
  "json" => JSON_PATH.relative_path_from(ROOT).to_s,
  "issue_count" => issues.size,
  "checks" => checks,
  "issues" => issues
}

SUMMARY_PATH.dirname.mkpath
JSON_REPORT_PATH.dirname.mkpath
File.write(JSON_REPORT_PATH, JSON.pretty_generate(report) + "\n")

summary_lines = []
summary_lines << "# Validation Memory Drift"
summary_lines << ""
summary_lines << "- Markdown: `#{report["markdown"]}`"
summary_lines << "- JSON: `#{report["json"]}`"
summary_lines << "- Issue Count: `#{issues.size}`"
summary_lines << ""
summary_lines << "## Checks"
summary_lines << ""
checks.each do |check|
  summary_lines << "- `#{check["status"]}` #{check["label"]}: `#{check["detail"]}`"
end
if issues.any?
  summary_lines << ""
  summary_lines << "## Issues"
  summary_lines << ""
  issues.each do |issue|
    summary_lines << "- #{issue}"
  end
end
File.write(SUMMARY_PATH, summary_lines.join("\n") + "\n")

puts "Validation Memory Drift"
puts "  count: #{issues.size}"
puts "  json: docs/generated/local-tooling/validation-memory-drift.json"
puts "  summary: docs/generated/local-tooling/validation-memory-drift-summary.md"

exit 1 if issues.any?
