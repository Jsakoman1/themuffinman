#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require "time"
require "yaml"
require_relative "../local_tooling_common"

OUT_JSON = "docs/generated/local-tooling/docs-as-tests.json"
OUT_MD = "docs/generated/local-tooling/docs-as-tests-summary.md"

DOC_SOURCES = [
  "docs/business-logic.md",
  "docs/domain-technical.md",
  "docs/documentation-sync-policy.md",
  "docs/change-completion-checklist.md"
].freeze
EVIDENCE_PATTERNS = [
  "apps/themuffinman/src/test/java/**/*Test.java",
  "scripts/audits/*.rb",
  "docs/agent-operating-model/sections/*.yaml",
  "docs/regression-scenario-catalog.yaml",
  "docs/workflow-state-machines.yaml"
].freeze
BEHAVIOR_KEYWORDS = %w[
  permission permissions ownership owner visibility exact-location exact location workflow workflows state transition
  validation validations sandbox production admin-generation sandbox-generation mutation mutations endpoint contract dto
  source-of-truth generated-artifact manifest closeout evidence
].freeze
NORMATIVE_PATTERN = /\b(must|must not|should|should not|only|cannot|never|required|requires|fail|fails|separate|separated|protected|canonical)\b/i

def rel_glob(*patterns)
  LocalToolingCommon.repo_glob(*patterns).map { |path| LocalToolingCommon.relative_path(path) }
end

def read(path)
  File.read(File.join(LocalToolingCommon::REPO_ROOT, path))
rescue Errno::ENOENT
  ""
end

def canonical_phrases
  yaml = YAML.safe_load(read("docs/agent-operating-model/sections/documentation_sync.yaml"), aliases: true) || {}
  Array(yaml["rules"]).flat_map do |rule|
    Array(rule["must_contain_all"]).map do |phrase|
      {
        source: "docs/agent-operating-model/sections/documentation_sync.yaml",
        line: nil,
        statement: phrase.to_s.strip,
        kind: "protected_phrase",
        rule_id: rule["id"]
      }
    end
  end.reject { |row| row[:statement].empty? }
end

def markdown_statements
  DOC_SOURCES.flat_map do |path|
    read(path).lines.each_with_index.each_with_object([]) do |(line, index), rows|
      text = line.sub(/\A\s*[-*]\s*/, "").strip
      next rows if text.empty? || text.start_with?("#")
      next rows unless text.match?(NORMATIVE_PATTERN)
      next rows unless BEHAVIOR_KEYWORDS.any? { |keyword| text.downcase.include?(keyword) }

      rows << {
        source: path,
        line: index + 1,
        statement: text,
        kind: "behavioral_statement",
        rule_id: nil
      }
    end
  end
end

def slug_terms(text)
  text.downcase.scan(/[a-z][a-z0-9-]{3,}/).reject do |term|
    %w[that this with from when then into they them must should only cannot required requires separate protected canonical].include?(term)
  end.uniq.first(14)
end

def domain_for_statement(statement)
  text = statement.downcase
  return "workmarket" if text.match?(/quest|application|workmarket|review/)
  return "social" if text.match?(/circle|contact|relationship/)
  return "chat" if text.match?(/chat|conversation|message|presence/)
  return "location" if text.match?(/location|address|nearby/)
  return "agent" if text.match?(/agent|planner|manifest|closeout|sandbox|generated|source-of-truth|documentation/)

  "shared"
end

def expected_evidence(statement)
  text = statement.downcase
  evidence = []
  evidence << "scenario_or_workflow_test" if text.match?(/workflow|state|transition|lifecycle/)
  evidence << "permission_or_policy_test" if text.match?(/permission|ownership|owner|admin|visibility|access/)
  evidence << "sandbox_or_generation_audit" if text.match?(/sandbox|admin-generation|synthetic|production/)
  evidence << "documentation_sync_audit" if text.match?(/protected|canonical|documentation|docs|source-of-truth/)
  evidence << "contract_or_dto_audit" if text.match?(/endpoint|contract|dto|generated artifact|frontend/)
  evidence << "validation_or_closeout_audit" if text.match?(/manifest|closeout|evidence|validation/)
  evidence.uniq
end

def evidence_corpus
  rel_glob(*EVIDENCE_PATTERNS).map do |path|
    {path: path, content: read(path).downcase}
  end
end

def evidence_matches(statement, corpus)
  terms = slug_terms(statement[:statement])
  domain = domain_for_statement(statement[:statement])
  strong_terms = (terms + [domain]).uniq
  corpus.each_with_object([]) do |entry, rows|
    path = entry[:path].downcase
    content = entry[:content]
    score = strong_terms.count { |term| path.include?(term) || content.include?(term) }
    next rows if score < 2

    rows << {path: entry[:path], score: score}
  end.sort_by { |row| [-row[:score], row[:path]] }.first(10)
end

def priority_for(statement, expected, matches)
  return "none" if matches.any?
  return "high" if statement[:kind] == "protected_phrase"
  return "high" if expected.include?("scenario_or_workflow_test") || expected.include?("permission_or_policy_test")
  return "medium" if expected.any?

  "low"
end

statements = (canonical_phrases + markdown_statements).uniq { |row| [row[:source], row[:line], row[:statement]] }
corpus = evidence_corpus
entries = statements.map do |statement|
  expected = expected_evidence(statement[:statement])
  matches = evidence_matches(statement, corpus)
  {
    source: statement[:source],
    line: statement[:line],
    kind: statement[:kind],
    rule_id: statement[:rule_id],
    domain: domain_for_statement(statement[:statement]),
    statement: statement[:statement],
    expected_evidence: expected,
    evidence_matches: matches,
    priority: priority_for(statement, expected, matches)
  }
end.sort_by { |entry| [entry[:source], entry[:line].to_i, entry[:statement]] }

review_needed = entries.reject { |entry| entry[:priority] == "none" }.sort_by do |entry|
  [%w[high medium low].index(entry[:priority]) || 9, entry[:source], entry[:line].to_i]
end

report = {
  generated_at: Time.now.utc.iso8601,
  statement_count: entries.size,
  protected_phrase_count: entries.count { |entry| entry[:kind] == "protected_phrase" },
  review_needed_count: review_needed.size,
  high_priority_count: review_needed.count { |entry| entry[:priority] == "high" },
  sources: DOC_SOURCES,
  entries: entries,
  review_needed: review_needed.first(100),
  notes: [
    "This report maps documentation statements to nearby test or audit signals; it is not semantic proof.",
    "A review-needed row means the statement did not have enough static evidence matches in tests, audits, or machine-readable operating docs."
  ]
}

lines = ["# Docs As Tests", ""]
lines << "- Generated at: `#{report[:generated_at]}`"
lines << "- Statements scanned: `#{report[:statement_count]}`"
lines << "- Protected phrases scanned: `#{report[:protected_phrase_count]}`"
lines << "- Review needed: `#{report[:review_needed_count]}`"
lines << "- High priority: `#{report[:high_priority_count]}`"
lines << ""
review_needed.first(30).each do |entry|
  location = entry[:line] ? "#{entry[:source]}:#{entry[:line]}" : entry[:source]
  lines << "- `#{entry[:priority]}` #{location} #{entry[:statement][0, 140]}"
end
lines << ""
lines << "Advisory only: static evidence matches help route review; they do not prove behavior."

LocalToolingCommon.write_json(OUT_JSON, report)
LocalToolingCommon.write_text(OUT_MD, lines.join("\n") + "\n")
puts "Docs as tests"
puts "  statements scanned: #{report[:statement_count]}"
puts "  review needed: #{report[:review_needed_count]}"
puts "  high priority: #{report[:high_priority_count]}"
