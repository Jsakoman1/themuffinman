#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "pathname"
require "date"
require "time"

ROOT = Pathname.new(__dir__).join("../..").expand_path
EVIDENCE_DIR = ROOT.join(".agents/validation-evidence")

VAGUE_SUMMARIES = [
  "build passed",
  "tests passed",
  "tests not run",
  "not run",
  "passed",
  "failed",
  "ok",
  "done",
  "n/a",
  "na",
  "tbd",
  "todo",
  "unknown"
].freeze

def blank?(value)
  value.nil? || value.to_s.strip.empty?
end

def vague?(value)
  text = value.to_s.strip.downcase
  return true if text.empty?
  return true if text.include?("replace this")
  return true if text.include?("placeholder")

  VAGUE_SUMMARIES.include?(text)
end

def valid_time?(value)
  return true if value.to_s == "unknown"

  Time.iso8601(value.to_s)
  true
rescue ArgumentError
  false
end

def each_yaml_file
  return [] unless EVIDENCE_DIR.directory?

  EVIDENCE_DIR.children
              .select { |path| path.file? && path.extname == ".yaml" }
              .sort_by(&:to_s)
end

violations = []

each_yaml_file.each do |path|
  relative_path = path.relative_path_from(ROOT).to_s
  evidence = YAML.safe_load(File.read(path), permitted_classes: [Time, Date], aliases: false) || {}

  Array(evidence["summary"]).each_with_index do |summary, index|
    violations << "#{relative_path}: summary[#{index}] is too vague" if vague?(summary)
  end

  Array(evidence["commands"]).each_with_index do |command, index|
    prefix = "#{relative_path}: commands[#{index}]"
    result = command["result"].to_s

    if blank?(command["command"])
      violations << "#{prefix} must name the exact command"
    end

    if blank?(command["scope"])
      violations << "#{prefix} must name the validation scope"
    end

    if blank?(command["ranAt"]) || !valid_time?(command["ranAt"])
      violations << "#{prefix} must include ranAt as ISO-8601 or unknown"
    end

    if vague?(command["summary"])
      violations << "#{prefix} summary is too vague"
    end

    if %w[skipped not_applicable].include?(result)
      if vague?(command["skippedReason"])
        violations << "#{prefix} must include a concrete skippedReason"
      end
    elsif %w[passed failed].include?(result) && command.key?("skippedReason") && !blank?(command["skippedReason"])
      violations << "#{prefix} must not include skippedReason when result is #{result}"
    end
  end

  Array(evidence["generatedArtifacts"]).each_with_index do |artifact, index|
    prefix = "#{relative_path}: generatedArtifacts[#{index}]"
    result = artifact["result"].to_s

    violations << "#{prefix} must name the artifact path" if blank?(artifact["path"])
    violations << "#{prefix} must name the artifact action" if blank?(artifact["action"])
    violations << "#{prefix} must include ranAt as ISO-8601 or unknown" if blank?(artifact["ranAt"]) || !valid_time?(artifact["ranAt"])
    violations << "#{prefix} summary is too vague" if vague?(artifact["summary"])

    if result == "failed"
      violations << "#{prefix} must not record failed generated-artifact evidence"
    elsif %w[skipped not_applicable].include?(result)
      violations << "#{prefix} must include a concrete skippedReason" if vague?(artifact["skippedReason"])
    elsif result != "passed"
      violations << "#{prefix} must use result passed, failed, skipped, or not_applicable"
    end
  end

  Array(evidence["skippedChecks"]).each_with_index do |check, index|
    prefix = "#{relative_path}: skippedChecks[#{index}]"
    violations << "#{prefix} must name the skipped check" if blank?(check["check"])
    violations << "#{prefix} must include a concrete reason" if vague?(check["reason"])
  end
end

puts "Validation evidence quality audit"
puts "  evidence files checked: #{each_yaml_file.length}"

if violations.any?
  violations.each { |violation| warn "  - #{violation}" }
  exit 1
end

puts "  vague evidence issues: 0"
