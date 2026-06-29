#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "time"
require "yaml"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT
OUT_ROOT = "docs/generated/local-tooling/validation-evidence"
YAML_EVIDENCE_ROOT = ".agents/validation-evidence"

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

def csv_list(value)
  value.to_s.split(",").map(&:strip).reject(&:empty?)
end

def feature_id(manifest, manifest_path)
  raw = manifest["featureId"].to_s
  raw = File.basename(manifest_path.to_s, ".yaml").sub(/-manifest\z/, "") if raw.empty?
  raw.gsub(/[^A-Za-z0-9_-]+/, "-")
end

def infer_scope(command)
  text = Array(command).join(" ")
  return "backend" if text.match?(/mvnw|mvn test|\bgradle\b/)
  return "frontend" if text.match?(/npm .*type-check|npm .*build|npm .*validate/)
  return "docs" if text.match?(/audit-documentation|todo-audit|audit-todo/)
  return "generated-artifact" if text.match?(/generate|validate:contracts|audit-generated/)
  return "agent-safety" if text.match?(/audit-agent-safety/)
  return "manifest" if text.match?(/closeout|manifest|audit-plan-completion/)
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

def command_entry(command, started_at, duration, stdout, stderr, status, output_path, scope_override = nil)
  {
    "command" => Array(command).join(" "),
    "scope" => scope_override || infer_scope(command),
    "result" => status.success? ? "passed" : "failed",
    "ranAt" => started_at.utc.iso8601,
    "durationSeconds" => duration.round(3),
    "summary" => summarize_output(stdout, stderr, status),
    "outputPath" => output_path
  }.compact
end

def skipped_command_entry(command, scope, result, summary, reason)
  {
    "command" => command,
    "scope" => scope,
    "result" => result,
    "ranAt" => Time.now.utc.iso8601,
    "summary" => summary,
    "skippedReason" => reason
  }
end

def generated_artifact_entry(path, action, result, summary, output_path, skipped_reason)
  entry = {
    "path" => path,
    "action" => action,
    "result" => result,
    "ranAt" => Time.now.utc.iso8601,
    "summary" => summary
  }
  entry["outputPath"] = output_path unless output_path.to_s.empty?
  entry["skippedReason"] = skipped_reason unless skipped_reason.to_s.empty?
  entry
end

def skipped_check_entry(check, reason)
  {
    "check" => check,
    "reason" => reason
  }
end

def write_manifest(path, manifest)
  File.write(abs(path), YAML.dump(manifest).sub(/\A---\n/, ""))
end

def yaml_evidence_path(feature_id)
  "#{YAML_EVIDENCE_ROOT}/#{feature_id}.yaml"
end

def load_yaml_evidence(feature_id)
  path = yaml_evidence_path(feature_id)
  if File.exist?(abs(path))
    YAML.load_file(abs(path)) || {}
  else
    {
      "changeId" => feature_id,
      "status" => "in_progress",
      "recordedAt" => "unknown",
      "summary" => [],
      "commands" => [],
      "generatedArtifacts" => [],
      "skippedChecks" => []
    }
  end
end

def write_yaml_evidence(feature_id, evidence)
  path = yaml_evidence_path(feature_id)
  FileUtils.mkdir_p(File.dirname(abs(path)))
  File.write(abs(path), YAML.dump(evidence).sub(/\A---\n/, ""))
  path
end

def write_companion(feature_id, manifest_path, evidence)
  path = "#{OUT_ROOT}/#{feature_id}.json"
  payload = {
    "changeId" => feature_id,
    "status" => evidence["status"],
    "recordedAt" => evidence["recordedAt"],
    "summary" => evidence["summary"],
    "commands" => evidence["commands"],
    "generatedArtifacts" => evidence["generatedArtifacts"],
    "skippedChecks" => evidence["skippedChecks"],
    "manifestPath" => manifest_path
  }
  LocalToolingCommon.write_json(path, payload)
  path
end

def update_manifest_generated_artifacts!(manifest, artifact_entry)
  manifest["generatedArtifacts"] ||= {}
  manifest["generatedArtifacts"]["refreshedPaths"] ||= []
  if artifact_entry["action"] == "refreshed" && artifact_entry["result"] == "passed"
    manifest["generatedArtifacts"]["refreshedPaths"] = (manifest["generatedArtifacts"]["refreshedPaths"] + [artifact_entry["path"]]).uniq.sort
  end
  manifest["generatedArtifacts"]["notApplicableReason"] =
    if Array(manifest.dig("generatedArtifacts", "refreshedPaths")).empty?
      "No generated artifacts were refreshed."
    else
      "Generated artifacts are listed in refreshedPaths."
    end
end

options, command = options_and_command(ARGV)
manifest_path = options["manifest"]
mode = options["mode"].to_s.empty? ? "command" : options["mode"].to_s

if manifest_path.to_s.empty?
  warn "usage: ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> [mode=command|generated_artifact|skipped_check] [key=value ...] -- <command...>"
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

id = feature_id(manifest, manifest_path)
manifest["validationEvidence"] ||= {}
manifest["validationEvidence"]["commands"] = list(manifest.dig("validationEvidence", "commands"))

yaml_evidence = load_yaml_evidence(id)
yaml_evidence["changeId"] = id
yaml_evidence["status"] = "in_progress"
yaml_evidence["recordedAt"] = Time.now.utc.iso8601
yaml_evidence["summary"] = ["Recorded validation evidence for #{manifest_path}."]
yaml_evidence["commands"] = list(yaml_evidence["commands"])
yaml_evidence["generatedArtifacts"] = list(yaml_evidence["generatedArtifacts"])
yaml_evidence["skippedChecks"] = list(yaml_evidence["skippedChecks"])

case mode
when "command"
  if command.empty?
    warn "command mode requires a command after -- or command=<...>"
    exit 1
  end

  started_at = Time.now
  stdout, stderr, status = Open3.capture3(*command, chdir: REPO_ROOT)
  duration = Time.now - started_at
  output_path = options["output"].to_s
  entry = command_entry(command, started_at, duration, stdout, stderr, status, output_path.empty? ? nil : output_path, options["scope"])
  manifest["validationEvidence"]["commands"] << entry
  yaml_evidence["commands"] << entry
  yaml_path = write_yaml_evidence(id, yaml_evidence)
  companion_path = write_companion(id, manifest_path, yaml_evidence)
  write_manifest(manifest_path, manifest)

  puts "Validation evidence recorded"
  puts "  manifest: #{manifest_path}"
  puts "  result: #{entry["result"]}"
  puts "  duration: #{entry["durationSeconds"]}s"
  puts "  yaml: #{yaml_path}"
  puts "  companion: #{companion_path}"
  exit(status.exitstatus || (status.success? ? 0 : 1))
when "generated_artifact"
  artifact_paths = csv_list(options["path"])
  action = options["action"].to_s.empty? ? "refreshed" : options["action"]
  result = options["result"].to_s.empty? ? "passed" : options["result"]
  summary = options["summary"].to_s
  if artifact_paths.empty? || summary.empty?
    warn "generated_artifact mode requires path=<csv> and summary=<text>"
    exit 1
  end
  entries = artifact_paths.map do |artifact_path|
    generated_artifact_entry(artifact_path, action, result, summary, options["output"].to_s, options["skippedReason"].to_s)
  end
  entries.each do |entry|
    yaml_evidence["generatedArtifacts"] << entry
    update_manifest_generated_artifacts!(manifest, entry)
  end
  yaml_path = write_yaml_evidence(id, yaml_evidence)
  companion_path = write_companion(id, manifest_path, yaml_evidence)
  write_manifest(manifest_path, manifest)
  puts "Generated artifact evidence recorded"
  puts "  manifest: #{manifest_path}"
  puts "  artifacts: #{artifact_paths.join(', ')}"
  puts "  yaml: #{yaml_path}"
  puts "  companion: #{companion_path}"
when "skipped_check"
  check = options["check"].to_s
  reason = options["reason"].to_s
  if check.empty? || reason.empty?
    warn "skipped_check mode requires check=<name> and reason=<text>"
    exit 1
  end
  yaml_evidence["skippedChecks"] << skipped_check_entry(check, reason)
  manifest["validationEvidence"]["commands"] << skipped_command_entry(check, options["scope"].to_s.empty? ? "manifest" : options["scope"], "skipped", "Skipped check: #{check}", reason)
  yaml_path = write_yaml_evidence(id, yaml_evidence)
  companion_path = write_companion(id, manifest_path, yaml_evidence)
  write_manifest(manifest_path, manifest)
  puts "Skipped check evidence recorded"
  puts "  manifest: #{manifest_path}"
  puts "  check: #{check}"
  puts "  yaml: #{yaml_path}"
  puts "  companion: #{companion_path}"
else
  warn "unknown mode: #{mode}"
  exit 1
end
