#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require "yaml"
require_relative "../local_tooling_common"

OUT_JSON = "docs/generated/local-tooling/doc-sync-duplicates.json"
OUT_MD = "docs/generated/local-tooling/doc-sync-duplicates-summary.md"
DOCS = [
  "AGENTS.md",
  "docs/agent-operating-model.md",
  "docs/agent-operating-model.yaml",
  "docs/documentation-sync-policy.md",
  "docs/change-completion-checklist.md",
  "docs/domain-technical.md",
  "docs/business-logic.md"
].freeze
FRAGMENT_PATTERNS = [
  /\bupdate all affected living docs\b/i,
  /\bcopy the exact canonical sentence\b/i,
  /\bdo not paraphrase\b/i,
  /\breview and extend affected admin or sandbox generation flows\b/i,
  /\bsandbox behavior must stay separate\b/i
].freeze
CONFLICT_GROUPS = {
  "sandbox_production_separation" => [/sandbox.*separate/i, /production.*mutation/i, /real-life product flow/i, /production-like voice flow/i],
  "docs_required_for_logic_changes" => [/logic.*docs/i, /logic-only change/i, /affected docs.*validation tests/i],
  "manifest_requirement" => [/manifest.*required/i, /manifest.*optional/i, /may skip.*manifest/i],
  "backlog_resolution" => [/record new deferred/i, /remove.*open backlog/i, /clear matching inline/i]
}.freeze

def abs(path)
  File.join(LocalToolingCommon::REPO_ROOT, path)
end

def read(path)
  File.read(abs(path))
rescue Errno::ENOENT
  ""
end

def canonical_phrases
  yaml = YAML.safe_load(read("docs/agent-operating-model/sections/documentation_sync.yaml"), aliases: true) || {}
  Array(yaml.dig("documentation_sync", "rules")).flat_map do |rule|
    Array(rule["must_contain_all"]).map { |phrase| {rule_id: rule["id"], phrase: phrase.to_s.strip} }
  end.reject { |row| row[:phrase].empty? }
end

def lines_for(path)
  read(path).lines.each_with_index.map do |line, index|
    {path: path, line: index + 1, text: line.strip}
  end
end

all_lines = DOCS.flat_map { |path| lines_for(path) }
phrases = canonical_phrases

protected_duplicates = phrases.map do |phrase|
  matches = all_lines.select { |row| row[:text].include?(phrase[:phrase]) }
  next nil if matches.size <= 1

  {
    rule_id: phrase[:rule_id],
    phrase: phrase[:phrase],
    occurrence_count: matches.size,
    occurrences: matches
  }
end.compact

fragment_only = all_lines.select do |row|
  next false if row[:text].empty? || row[:text].start_with?("#")
  next false if phrases.any? { |phrase| row[:text].include?(phrase[:phrase]) }

  FRAGMENT_PATTERNS.any? { |pattern| row[:text].match?(pattern) }
end.map do |row|
  row.merge(recommendation: "Review whether this fragment should point at the canonical documentation_sync rule instead of restating partial policy wording.")
end

conflicts = CONFLICT_GROUPS.map do |id, patterns|
  matches = all_lines.select { |row| patterns.any? { |pattern| row[:text].match?(pattern) } }
  normalized = matches.map { |row| row[:text].downcase.gsub(/[`*_.,:;]/, "").gsub(/\s+/, " ") }.uniq
  next nil if normalized.size <= 1

  {
    id: id,
    variant_count: normalized.size,
    occurrences: matches.first(30),
    recommendation: "Review variants for wording drift; preserve exact protected canonical phrases when consolidation is needed."
  }
end.compact

report = {
  generated_at: Time.now.utc.iso8601,
  docs_scanned: DOCS,
  canonical_phrase_count: phrases.size,
  protected_duplicate_count: protected_duplicates.size,
  fragment_only_count: fragment_only.size,
  conflict_group_count: conflicts.size,
  protected_duplicates: protected_duplicates,
  fragment_only_policy_bullets: fragment_only.first(100),
  conflict_groups: conflicts,
  notes: [
    "This audit is report-only and does not rewrite documentation.",
    "Duplicate protected phrases are allowed when YAML requires the same canonical sentence in multiple targets; review only when duplication creates maintenance noise."
  ]
}

LocalToolingCommon.write_json(OUT_JSON, report)
lines = ["# Doc Sync Duplicates", ""]
lines << "- Decision: `#{report[:protected_duplicate_count].to_i.zero? && report[:conflict_group_count].to_i.zero? ? "clear" : "review"}`"
lines << "- Why: protected duplicates=#{report[:protected_duplicate_count]}, conflicts=#{report[:conflict_group_count]}"
lines << "- Next action: review the fragments listed below"
lines << "- Evidence: canonical phrases=#{report[:canonical_phrase_count]}, fragment-only=#{report[:fragment_only_count]}"
lines << ""
fragment_only.first(3).each do |row|
  lines << "- fragment `#{row[:path]}:#{row[:line]}`"
end
conflicts.first(3).each do |row|
  lines << "- conflict `#{row[:id]}` variants=#{row[:variant_count]}"
end
LocalToolingCommon.write_text(OUT_MD, lines.join("\n") + "\n")

puts "Doc sync duplicates"
puts "  protected duplicate groups: #{report[:protected_duplicate_count]}"
puts "  fragment-only bullets: #{report[:fragment_only_count]}"
puts "  conflict groups: #{report[:conflict_group_count]}"
