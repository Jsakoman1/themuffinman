#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../audit_support"

TEST_ROOT = "apps/themuffinman/src/test/java"
OUT_JSON = "docs/audit-output/test-fixture-duplication.json"
OUT_MD = "docs/audit-output/test-fixture-duplication-summary.md"

FOCUS_PATTERNS = {
  "users" => /\b(new AppUser|TestFixtures\.(user|admin|userWithProfileLocation)|createUser\s*\()/,
  "circles" => /\b(new Circle(Group|Membership|Request)|TestFixtures\.circle|createCircle\s*\()/,
  "quests" => /\b(new Quest\b|TestFixtures\.quest|QuestRequestDTO\.builder|createQuest\s*\()/,
  "applications" => /\b(new QuestApplication\b|TestFixtures\.questApplication|QuestApplicationRequestDTO\.builder|createApplication\s*\()/,
  "location-settings" => /\b(LocationSettings|UserLocationMode|ExactLocationVisibilityScope|setLocation|setExactLocation)/,
  "chat" => /\b(new Chat(Conversation|Message|Presence)|TestFixtures\.conversation|createConversation\s*\()/,
}.freeze

ENTITY_PATTERNS = {
  "AppUser" => /new AppUser\s*\(|TestFixtures\.(user|admin|userWithProfileLocation)\s*\(/,
  "CircleGroup" => /new CircleGroup\s*\(|TestFixtures\.circle\s*\(/,
  "Quest" => /new Quest\s*\(|TestFixtures\.quest\s*\(/,
  "QuestApplication" => /new QuestApplication\s*\(|TestFixtures\.questApplication\s*\(/,
  "ChatConversation" => /new ChatConversation\s*\(|TestFixtures\.conversation\s*\(/,
  "QuestRequestDTO" => /QuestRequestDTO\.builder\s*\(/,
  "QuestApplicationRequestDTO" => /QuestApplicationRequestDTO\.builder\s*\(/,
}.freeze

def rel_glob(*patterns)
  AuditSupport.repo_glob(*patterns).map { |path| AuditSupport.relative_path(path) }
end

def read(path)
  File.read(File.join(AuditSupport::REPO_ROOT, path))
end

def domain_for(path)
  path.split("/com/themuffinman/app/", 2).last.to_s.split("/", 2).first || "unknown"
end

def test_rows
  rel_glob("#{TEST_ROOT}/**/*Test.java").map do |path|
    content = read(path)
    focus_hits = FOCUS_PATTERNS.each_with_object({}) { |(name, pattern), hits| hits[name] = content.scan(pattern).size }
    entity_hits = ENTITY_PATTERNS.each_with_object({}) { |(name, pattern), hits| hits[name] = content.scan(pattern).size }
    set_calls = content.scan(/\.set[A-Z][A-Za-z0-9_]*\s*\(/).size
    builder_calls = content.scan(/[A-Z][A-Za-z0-9_]*DTO\.builder\s*\(/).size
    before_each = content.scan(/@BeforeEach/).size
    helper_methods = content.scan(/private\s+(?:static\s+)?[A-Za-z0-9_<>, ?]+\s+(create|build|fixture)[A-Z][A-Za-z0-9_]*\s*\(/).size
    score = focus_hits.values.sum + entity_hits.values.sum + (set_calls / 3) + (builder_calls * 2) + (before_each * 3) + (helper_methods * 2)
    {
      file: path,
      domain: domain_for(path),
      setup_score: score,
      focus_hits: focus_hits.select { |_, count| count.positive? },
      entity_hits: entity_hits.select { |_, count| count.positive? },
      set_call_count: set_calls,
      dto_builder_count: builder_calls,
      before_each_count: before_each,
      local_helper_count: helper_methods,
      uses_shared_test_fixtures: content.include?("TestFixtures")
    }
  end
end

def cluster_candidates(rows)
  FOCUS_PATTERNS.keys.map do |focus|
    matching = rows.select { |row| row[:focus_hits].fetch(focus, 0).positive? }
    total_hits = matching.sum { |row| row[:focus_hits].fetch(focus, 0) }
    next if matching.size < 2 || total_hits < 4

    {
      focus: focus,
      file_count: matching.size,
      total_hits: total_hits,
      top_files: matching.sort_by { |row| -row[:setup_score] }.first(8).map { |row| row[:file] },
      recommendation: recommendation_for(focus, matching, total_hits)
    }
  end.compact.sort_by { |row| [-row[:file_count], -row[:total_hits], row[:focus]] }
end

def recommendation_for(focus, rows, total_hits)
  if rows.count { |row| row[:uses_shared_test_fixtures] }.positive?
    "Extend existing `TestFixtures` coverage for repeated #{focus} setup instead of adding another local helper."
  elsif total_hits >= 8 || rows.size >= 4
    "Add or extend shared fixture-builder methods for repeated #{focus} setup."
  else
    "Review for local helper extraction if the repeated #{focus} setup changes again."
  end
end

def entity_repetition(rows)
  ENTITY_PATTERNS.keys.map do |entity|
    matching = rows.select { |row| row[:entity_hits].fetch(entity, 0).positive? }
    next if matching.size < 2

    {
      entity: entity,
      file_count: matching.size,
      total_hits: matching.sum { |row| row[:entity_hits].fetch(entity, 0) },
      files: matching.map { |row| row[:file] }.first(12)
    }
  end.compact.sort_by { |row| [-row[:file_count], -row[:total_hits], row[:entity]] }
end

def markdown(report)
  lines = ["# Test Fixture Duplication Audit", ""]
  lines << "- Generated at: `#{report[:generated_at]}`"
  lines << "- Tests scanned: `#{report[:tests_scanned]}`"
  lines << "- Extraction candidates: `#{report[:extraction_candidates].size}`"
  lines << ""
  lines << "## Extraction Candidates"
  lines << ""
  report[:extraction_candidates].first(12).each do |candidate|
    lines << "- `#{candidate[:focus]}` files=#{candidate[:file_count]} hits=#{candidate[:total_hits]}: #{candidate[:recommendation]}"
  end
  lines << "- None" if report[:extraction_candidates].empty?
  lines << ""
  lines << "## High Setup Files"
  lines << ""
  report[:high_setup_files].first(12).each do |row|
    lines << "- `#{row[:file]}` score=#{row[:setup_score]} focus=#{row[:focus_hits].keys.join(',')}"
  end
  lines.join("\n") + "\n"
end

rows = test_rows
report = {
  generated_at: Time.now.utc.iso8601,
  tests_scanned: rows.size,
  shared_fixture_file: "apps/themuffinman/src/test/java/com/themuffinman/app/testing/TestFixtures.java",
  extraction_candidates: cluster_candidates(rows),
  repeated_entities: entity_repetition(rows),
  high_setup_files: rows.select { |row| row[:setup_score] >= 12 }.sort_by { |row| -row[:setup_score] }.first(20),
  rows: rows
}

AuditSupport.write_json(OUT_JSON, report)
AuditSupport.write_text(OUT_MD, markdown(report))
puts "Test fixture duplication audit"
puts "  tests scanned: #{report[:tests_scanned]}"
puts "  extraction candidates: #{report[:extraction_candidates].size}"
puts "  high setup files: #{report[:high_setup_files].size}"
