#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "time"
require "yaml"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT
OUT_ROOT = "docs/generated/local-tooling/validation-evidence"

def options_and_command(argv)
  separator = argv.index("--")
  option_args = separator ? argv[0...separator] : argv
  command_args = separator ? argv[(separator + 1)..] : []
  options = option_args.each_with_object({}) do |arg, parsed|
    key, value = arg.split("=", 2)
    parsed[key] = value if value
  end
  command_args = options["command"].to_s.split(/\s+/) if command_args.empty? && options["command"].to_s.include?(" ")
  command_args = [options["command"]] if command_args.empty? && !options["command"].to_s.empty?
  [options, command_args]
end

def abs(path)
  File.expand_path(path.to_s, REPO_ROOT)
end

def list(value)
  value.is_a?(Array) ? value.compact : []
end

def feature_id(manifest, manifest_path)
  raw = manifest["featureId"].to_s
  raw = File.basename(manifest_path.to_s, ".yaml").sub(/-manifest\z/, "") if raw.empty?
  raw.gsub(/[^A-Za-z0-9_-]+/, "-")
end

def infer_scope(command)
  text = command.join(" ")
  return "backend" if text.match?(/mvnw|mvn test|\bgradle\b/)
  return "frontend" if text.match?(/npm .*type-check|npm .*build|npm .*validate/)
  return "docs" if text.match?(/audit-documentation|todo-audit|audit-todo/)
  return "generated-artifact" if text.match?(/generate|validate:contracts|audit-generated/)
  return "agent-safety" if text.match?(/audit-agent-safety/)
  return "local-tooling" if text.match?(/scripts\/audits|audit-local-tooling|ruby .*audit/)
  return "smoke" if text.match?(/smoke/)

  "other"
end

def summarize_output(stdout, stderr, status)
  lines = "#{stdout}\n#{stderr}".lines.map(&:strip).reject(&:empty?)
  tail = lines.last(4).join(" ")
  tail = status.success? ? "Command passed." : "Command failed." if tail.empty?
  tail[0, 500]
end

def command_entry(command, started_at, duration, stdout, stderr, status, output_path)
  {
    "command" => command.join(" "),
    "scope" => infer_scope(command),
    "result" => status.success? ? "passed" : "failed",
    "ranAt" => started_at.utc.iso8601,
    "durationSeconds" => duration.round(3),
    "summary" => summarize_output(stdout, stderr, status),
    "outputPath" => output_path
  }.compact
end

def write_manifest(path, manifest)
  File.write(abs(path), YAML.dump(manifest).sub(/\A---\n/, ""))
end

def write_companion(feature_id, manifest_path, entry)
  path = "#{OUT_ROOT}/#{feature_id}.json"
  existing =
    if File.exist?(abs(path))
      JSON.parse(File.read(abs(path)))
    else
      {
        "changeId" => feature_id,
        "status" => "in_progress",
        "recordedAt" => Time.now.utc.iso8601,
        "summary" => [],
        "commands" => [],
        "generatedArtifacts" => [],
        "skippedChecks" => []
      }
    end
  existing["recordedAt"] = Time.now.utc.iso8601
  existing["summary"] = ["Recorded validation evidence for #{manifest_path}."]
  existing["commands"] = list(existing["commands"]) + [entry]
  LocalToolingCommon.write_json(path, existing)
  path
end

options, command = options_and_command(ARGV)
manifest_path = options["manifest"]

if manifest_path.to_s.empty? || command.empty?
  warn "usage: ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> [output=<summary-path>] -- <command...>"
  exit 1
end

unless File.exist?(abs(manifest_path))
  warn "manifest not found: #{manifest_path}"
  exit 1
end

manifest = YAML.load_file(abs(manifest_path))
unless manifest.is_a?(Hash)
  warn "manifest is not a YAML object: #{manifest_path}"
  exit 1
end

started_at = Time.now
stdout, stderr, status = Open3.capture3(*command, chdir: REPO_ROOT)
duration = Time.now - started_at
output_path = options["output"].to_s
entry = command_entry(command, started_at, duration, stdout, stderr, status, output_path.empty? ? nil : output_path)

manifest["validationEvidence"] ||= {}
manifest["validationEvidence"]["commands"] = list(manifest.dig("validationEvidence", "commands")) + [entry]
write_manifest(manifest_path, manifest)
companion_path = write_companion(feature_id(manifest, manifest_path), manifest_path, entry)

puts "Validation evidence recorded"
puts "  manifest: #{manifest_path}"
puts "  result: #{entry["result"]}"
puts "  duration: #{entry["durationSeconds"]}s"
puts "  companion: #{companion_path}"

exit(status.exitstatus || (status.success? ? 0 : 1))
