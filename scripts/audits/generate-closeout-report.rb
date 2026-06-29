#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "yaml"
require "time"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT
OUT_ROOT = "docs/generated/local-tooling/closeout-reports"

def options(argv)
  argv.each_with_object({}) do |arg, parsed|
    key, value = arg.split("=", 2)
    parsed[key] = value if value
  end
end

def abs(path)
  File.join(REPO_ROOT, path.to_s)
end

def git_changed_files
  stdout, _stderr, status = Open3.capture3("git", "status", "--short", chdir: REPO_ROOT)
  return [] unless status.success?

  stdout.lines.map do |line|
    path = line[3..]&.strip
    next if path.to_s.empty?

    path.split(" -> ").last
  end.compact.sort
end

def list(value)
  value.is_a?(Array) ? value.compact : []
end

def feature_id(manifest, manifest_path)
  raw = manifest["featureId"].to_s
  raw = File.basename(manifest_path.to_s, ".yaml").sub(/-manifest\z/, "") if raw.empty?
  raw.gsub(/[^A-Za-z0-9_-]+/, "-")
end

def docs_delta(manifest)
  artifact_docs = list(manifest.dig("artifacts", "docPaths"))
  explicit = manifest["docDelta"]
  {
    summary: explicit || manifest["summary"] || [],
    doc_paths: artifact_docs,
    generated_artifacts: manifest["generatedArtifacts"] || {},
    plan_completion: manifest["planCompletion"] || {}
  }
end

def residual_risks(manifest)
  risks = list(manifest.dig("closeoutDecision", "residualRisks"))
  reason = manifest.dig("closeoutDecision", "reason").to_s
  risks << reason unless reason.empty? || reason.match?(/\Aready\z/i)
  risks.empty? ? ["none declared"] : risks
end

def write_summary(path, payload)
  lines = ["# Closeout Report #{payload[:feature_id]}", ""]
  lines << "- Title: #{payload[:title]}"
  lines << "- Status: `#{payload[:status]}`"
  lines << "- Manifest: `#{payload[:manifest]}`"
  lines << "- Changed files in worktree: `#{payload[:changed_files].size}`"
  lines << ""
  lines << "## Artifacts"
  lines << ""
  payload[:artifacts].each do |group, paths|
    next if paths.nil?

    count = paths.respond_to?(:size) ? paths.size : 1
    lines << "- #{group}: `#{count}`"
  end
  lines << ""
  lines << "## Validation"
  lines << ""
  payload[:validation_commands].first(12).each do |command|
    lines << "- `#{command[:result] || 'unknown'}` #{command[:command]}"
  end
  lines << "- No validation commands recorded." if payload[:validation_commands].empty?
  lines << ""
  lines << "## Backlog"
  lines << ""
  lines << "- Reviewed: `#{payload[:backlog_delta][:reviewed]}`"
  lines << "- Created IDs: `#{payload[:backlog_delta][:created_ids].join(', ')}`"
  lines << "- Resolved IDs: `#{payload[:backlog_delta][:resolved_ids].join(', ')}`"
  lines << ""
  lines << "## Residual Risk"
  lines << ""
  payload[:residual_risks].each { |risk| lines << "- #{risk}" }
  lines << ""
  LocalToolingCommon.write_text(path, lines.join("\n"))
end

parsed = options(ARGV)
manifest_path = parsed["manifest"]

if manifest_path.to_s.empty?
  warn "usage: ruby scripts/audits/generate-closeout-report.rb manifest=<manifest-file>"
  exit 1
end

unless File.exist?(abs(manifest_path))
  warn "manifest not found: #{manifest_path}"
  exit 1
end

manifest = YAML.load_file(abs(manifest_path))
id = feature_id(manifest, manifest_path)
payload = {
  generated_at: Time.now.utc.iso8601,
  feature_id: id,
  title: manifest["title"].to_s,
  status: manifest["status"].to_s,
  manifest: manifest_path,
  plan_file: manifest["planFile"].to_s,
  changed_files: git_changed_files,
  artifacts: {
    code_paths: list(manifest.dig("artifacts", "codePaths")),
    doc_paths: list(manifest.dig("artifacts", "docPaths")),
    test_paths: list(manifest.dig("artifacts", "testPaths")),
    generator_commands: list(manifest.dig("artifacts", "generatorCommands")),
    audit_commands: list(manifest.dig("artifacts", "auditCommands"))
  },
  validation_commands: list(manifest.dig("validationEvidence", "commands")).map do |entry|
    entry.transform_keys(&:to_sym)
  end,
  docs_delta: docs_delta(manifest),
  generated_artifacts: manifest["generatedArtifacts"] || {},
  backlog_delta: {
    reviewed: manifest.dig("backlog", "reviewed") == true,
    created_ids: list(manifest.dig("backlog", "createdIds")),
    resolved_ids: list(manifest.dig("backlog", "resolvedIds"))
  },
  residual_risks: residual_risks(manifest),
  closeout_decision: manifest["closeoutDecision"] || {}
}

json_path = "#{OUT_ROOT}/#{id}.json"
summary_path = "#{OUT_ROOT}/#{id}-summary.md"
LocalToolingCommon.write_json(json_path, payload)
write_summary(summary_path, payload)

puts "Closeout report"
puts "  feature: #{id}"
puts "  json: #{json_path}"
puts "  summary: #{summary_path}"
