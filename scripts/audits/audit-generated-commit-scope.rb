#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "time"
require_relative "../local_tooling_common"

POLICY_PATH = "docs/generated/artifact-policy.yaml"

def git_status_entries
  LocalToolingCommon.run_command("git", "status", "--short").lines.map do |line|
    next if line.strip.empty?

    {status: line[0, 2].strip, path: line[3..].to_s.strip.split(" -> ").last}
  end.compact
rescue StandardError
  []
end

def load_policy
  YAML.load_file(File.join(LocalToolingCommon::REPO_ROOT, POLICY_PATH)).fetch("generated_artifact_policy")
end

def generated_path?(path)
  path.start_with?("docs/generated/") ||
    path == "docs/agent-operating-model.yaml" ||
    path.start_with?("apps/themuffinman/frontend/src/contracts/generated/")
end

def glob_match?(pattern, path)
  File.fnmatch?(pattern, path, File::FNM_EXTGLOB) || File.fnmatch?(pattern, path, File::FNM_PATHNAME | File::FNM_EXTGLOB)
end

def matches_any?(patterns, path)
  Array(patterns).any? { |pattern| glob_match?(pattern, path) }
end

def classify(path, policy)
  source_of_truth = Array(policy.dig("source_of_truth", "paths"))
  tracked = Array(policy.dig("tracked_review_context", "path_globs"))
  disposable = Array(policy.dig("disposable_local_context", "path_globs"))
  do_not_commit = Array(policy.dig("do_not_commit_by_default", "path_globs"))

  if source_of_truth.include?(path)
    ["task_required", "source-of-truth generated artifact tracked by policy"]
  elsif matches_any?(do_not_commit, path) || matches_any?(disposable, path)
    ["do_not_commit_by_default", "disposable local context or cache output"]
  elsif matches_any?(tracked, path)
    ["supporting_context", "tracked review context; commit only when refreshed for the task"]
  elsif generated_path?(path)
    ["stale_or_unrelated", "generated path is not covered by the artifact policy"]
  else
    ["source_change", "not a generated artifact"]
  end
end

def markdown(report)
  lines = ["# Generated Commit Scope", ""]
  lines << "- Generated at: `#{report[:generated_at]}`"
  lines << "- Source changes: `#{report[:source_changes].size}`"
  lines << "- Generated changes: `#{report[:generated_changes].size}`"
  lines << ""
  report[:summary].each do |status, count|
    lines << "- #{status}: `#{count}`"
  end
  lines << ""
  report[:generated_changes].first(80).each do |entry|
    lines << "- `#{entry[:recommendation]}` #{entry[:path]} (#{entry[:reason]})"
  end
  lines << ""
  lines.join("\n")
end

policy = load_policy
entries = git_status_entries
generated_changes = []
source_changes = []

entries.each do |entry|
  recommendation, reason = classify(entry[:path], policy)
  if recommendation == "source_change"
    source_changes << entry
  else
    generated_changes << entry.merge(recommendation: recommendation, reason: reason)
  end
end

report = {
  generated_at: Time.now.utc.iso8601,
  policy: POLICY_PATH,
  source_changes: source_changes,
  generated_changes: generated_changes,
  summary: generated_changes.group_by { |entry| entry[:recommendation] }.transform_values(&:size)
}

LocalToolingCommon.write_json("docs/generated/local-tooling/generated-commit-scope.json", report)
LocalToolingCommon.write_text("docs/generated/local-tooling/generated-commit-scope-summary.md", markdown(report))

puts "Generated commit scope"
puts "  source changes: #{source_changes.size}"
puts "  generated changes: #{generated_changes.size}"
report[:summary].sort.each { |status, count| puts "  - #{status}: #{count}" }
