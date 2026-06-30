#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "pathname"
require "time"
require "fileutils"

ROOT = Pathname.new(__dir__).join("..", "..").expand_path
SOURCE_PATH = ROOT.join("docs/validation-memory.json")
SUMMARY_PATH = ROOT.join("docs/generated/local-tooling/validation-memory-closeout-card-summary.md")
JSON_REPORT_PATH = ROOT.join("docs/generated/local-tooling/validation-memory-closeout-card.json")

def read_json(path)
  JSON.parse(path.read)
end

def write(path, content)
  FileUtils.mkdir_p(path.dirname)
  path.write(content)
end

payload = read_json(SOURCE_PATH)
canonical = payload.fetch("canonicalCommands", {})

report = {
  generatedAt: Time.now.utc.iso8601,
  source: "docs/validation-memory.json",
  purpose: "Ultra-short closeout command card derived from canonical validation memory.",
  closeout: Array(canonical["closeout"]),
  backendLogic: Array(canonical["backendLogic"]),
  frontendContract: Array(canonical["frontendContract"]),
  agentContract: Array(canonical["agentContract"]),
  workflowExpansion: Array(canonical["workflowExpansion"])
}

lines = []
lines << "# Validation Memory Closeout Card"
lines << ""
lines << "- Source: `docs/validation-memory.json`"
lines << "- Generated at: `#{report[:generatedAt]}`"
lines << ""
lines << "## Default Closeout"
report[:closeout].each { |command| lines << "- `#{command}`" }
lines << ""
lines << "## Add By Change Profile"
lines << "- Backend logic: `#{report[:backendLogic].join("`, `")}`"
lines << "- Frontend contract: `#{report[:frontendContract].join("`, `")}`"
lines << "- Agent contract: `#{report[:agentContract].join("`, `")}`"
lines << "- Workflow expansion: `#{report[:workflowExpansion].join("`, `")}`"
lines << ""
lines << "## Rule"
lines << "- Record the exact canonical command string in manifest evidence when validators expect it."
lines << ""

write(JSON_REPORT_PATH, JSON.pretty_generate(report) + "\n")
write(SUMMARY_PATH, lines.join("\n"))

puts "Validation memory closeout card"
puts "  json: #{JSON_REPORT_PATH.relative_path_from(ROOT)}"
puts "  summary: #{SUMMARY_PATH.relative_path_from(ROOT)}"
