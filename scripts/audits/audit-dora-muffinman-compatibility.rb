#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "shellwords"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
MATRIX_PATH = File.join(ROOT, "docs/dora-muffinman-compatibility-matrix.yaml")

def target_body(target)
  lines = File.readlines(File.join(ROOT, "Makefile"), chomp: true)
  start = lines.index { |line| line == "#{target}:" }
  return nil unless start

  lines[(start + 1)..].take_while { |line| line.empty? || line.start_with?("\t") }
end

failures = []
matrix = YAML.load_file(MATRIX_PATH)
failures << "compatibility matrix kind is invalid" unless matrix["kind"] == "dora_muffinman_compatibility_matrix" && matrix["version"].to_i == 1
adapter_path = matrix["adapter"].to_s
failures << "compatibility matrix adapter is missing" if adapter_path.empty?
stdout, stderr, status = Open3.capture3("dora/bin/dora", "validate-adapter", adapter_path, chdir: ROOT)
failures << "MuffinMan Dora adapter validation failed: #{[stdout, stderr].join("\n").strip}" unless status.success?

entries = Array(matrix["entries"])
failures << "compatibility matrix has no entries" if entries.empty?
entries.each do |entry|
  %w[target dora_executor evidence_paths proof].each do |field|
    failures << "compatibility entry is missing #{field}" if entry[field].to_s.empty?
  end
  target = entry["target"].to_s
  body = target_body(target)
  if body.nil?
    failures << "missing retained Make target: #{target}"
    next
  end

  failures << "#{target} does not invoke the declared Dora executor" unless body.any? { |line| line.include?(entry["dora_executor"]) }
  evidence_paths = Array(entry["evidence_paths"])
  failures << "#{target} has no evidence path" if evidence_paths.empty?
  evidence_paths.each { |path| failures << "#{target} evidence path is missing: #{path}" unless File.exist?(File.join(ROOT, path)) }
end

fixture_trace = matrix["fixture_trace"].to_s
failures << "compatibility matrix fixture_trace is missing" if fixture_trace.empty?
unless fixture_trace.empty?
  stdout, stderr, status = Open3.capture3(*Shellwords.shellsplit(fixture_trace), chdir: ROOT)
  failures << "Dora fixture trace failed: #{[stdout, stderr].join("\n").strip}" unless status.success?
end

abort "Dora/MuffinMan compatibility audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora/MuffinMan compatibility audit passed (#{entries.map { |entry| entry["target"] }.join(", ")} delegate through Dora with a standalone fixture trace)."
