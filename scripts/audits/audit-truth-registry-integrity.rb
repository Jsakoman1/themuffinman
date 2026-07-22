#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

REGISTRY_PATH = "docs/system-truth-registry.yaml"
REQUIRED_FIELDS = %w[id owner canonical_sources evidence_class confidence update_trigger].freeze
EVIDENCE_CLASSES = %w[source-trace validated-contract runtime-proof inferred-risk unknown].freeze
CONFIDENCE_LEVELS = %w[high medium low].freeze
ALLOWED_PATH_PREFIXES = %w[apps/ docs/ scripts/].freeze

registry = YAML.load_file(REGISTRY_PATH)
abort "truth registry version must be 1" unless registry["version"] == 1
abort "truth registry purpose is required" if registry["purpose"].to_s.strip.empty?
abort "truth registry reviewed_round must be a positive integer" unless registry["reviewed_round"].is_a?(Integer) && registry["reviewed_round"].positive?

rows = registry["fact_classes"]
abort "truth registry fact_classes must be a non-empty array" unless rows.is_a?(Array) && !rows.empty?

ids = rows.map { |row| row["id"] }
duplicates = ids.group_by(&:itself).select { |_id, entries| entries.length > 1 }.keys
abort "duplicate truth registry IDs: #{duplicates.join(', ')}" unless duplicates.empty?

paths_checked = []
rows.each do |row|
  id = row["id"] || "unknown"
  missing = REQUIRED_FIELDS.reject { |field| row.key?(field) && !row[field].to_s.strip.empty? }
  abort "#{id} missing fields: #{missing.join(', ')}" unless missing.empty?
  abort "#{id} has invalid evidence_class: #{row['evidence_class']}" unless EVIDENCE_CLASSES.include?(row["evidence_class"])
  abort "#{id} has invalid confidence: #{row['confidence']}" unless CONFIDENCE_LEVELS.include?(row["confidence"])

  sources = row["canonical_sources"]
  abort "#{id} canonical_sources must be a non-empty array" unless sources.is_a?(Array) && !sources.empty?
  (Array(sources) + Array(row["derived_artifacts"])).each do |path|
    abort "#{id} contains a non-string path" unless path.is_a?(String)
    safe_path = ALLOWED_PATH_PREFIXES.any? { |prefix| path.start_with?(prefix) } && !path.include?("..")
    abort "#{id} contains an unsafe or unsupported path: #{path}" unless safe_path
    abort "#{id} references missing path: #{path}" unless File.file?(path) || Dir.exist?(path)

    paths_checked << path
  end
end

rules = registry["rules"]
abort "truth registry rules must be a non-empty array" unless rules.is_a?(Array) && !rules.empty?

puts "Truth registry integrity audit passed"
puts "  fact classes: #{rows.length}"
puts "  canonical and derived paths checked: #{paths_checked.uniq.length}"
puts "  reviewed round: #{registry['reviewed_round']}"
