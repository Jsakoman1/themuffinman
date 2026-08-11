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
stdout, stderr, status = Open3.capture3("bin/dora", "validate-adapter", adapter_path, chdir: ROOT)
failures << "MuffinMan Dora adapter validation failed: #{[stdout, stderr].join("\n").strip}" unless status.success?
stdout, stderr, status = Open3.capture3("bin/dora", "plugin-contract", ".dora/plugins.yaml", chdir: ROOT)
failures << "MuffinMan Dora plugin manifest validation failed: #{[stdout, stderr].join("\n").strip}" unless status.success?
{
  "scripts/audits/audit-api-contract-drift.rb" => "http-contract-drift",
  "scripts/audits/audit-endpoint-callsite-linker.rb" => "http-contract-linker",
  "scripts/audits/audit-frontend-route-surfaces.rb" => "vue-navigation",
  "scripts/audits/audit-configuration-environment-drift.rb" => "spring-configuration-drift",
  "scripts/audits/audit-mapper-usage.rb" => "spring-mapper-usage",
  "scripts/audits/audit-ui-entrypoints.rb" => "vue-ui-entrypoints",
  "scripts/audits/audit-frontend-interaction-contract.rb" => "vue-interaction-hygiene",
  "scripts/audits/audit-frontend-stale-surfaces.rb" => "vue-stale-surface-hygiene",
  "scripts/audits/audit-canonical-source-integrity.rb" => "canonical-source-integrity",
  "scripts/audits/audit-module-dependency-direction.rb" => "architecture-integrity",
  "scripts/audits/audit-read-surface-inventory.rb" => "vue-read-surface-hygiene"
}.each do |path, plugin_id|
  wrapper = File.read(File.join(ROOT, path))
  plugin = Array(YAML.load_file(File.join(ROOT, ".dora/plugins.yaml"))["plugins"]).find { |candidate| candidate["id"] == plugin_id }
  failures << "#{path} does not delegate through its declared Dora built-in plugin" unless wrapper.include?("plugin-run") && wrapper.include?(plugin_id) && plugin && plugin["builtin"].to_s != "" && !wrapper.include?("DORA_PLUGIN_RUNNER")
  stdout, stderr, status = Open3.capture3("bin/dora", "plugin-run", ".dora/plugins.yaml", plugin_id, chdir: ROOT)
  failures << "Dora runner failed for #{plugin_id}: #{[stdout, stderr].join("\n").strip}" unless status.success?
end
adapter = YAML.load_file(File.join(ROOT, adapter_path)) if File.file?(File.join(ROOT, adapter_path))
distribution = adapter.is_a?(Hash) ? adapter.fetch("distribution", {}) : {}
%w[source_repository source_ref source_commit].each do |field|
  failures << "MuffinMan Dora distribution is missing #{field}" if distribution[field].to_s.empty?
end
failures << "MuffinMan Dora distribution source_ref must be versioned" unless distribution["source_ref"].to_s.match?(/\Av\d+\.\d+\.\d+\z/)
failures << "MuffinMan Dora distribution source_commit must be immutable" unless distribution["source_commit"].to_s.match?(/\A[0-9a-f]{40}\z/)
handoff_version = distribution["source_ref"].to_s.split(".").first(2).join.delete(".")
handoff_path = File.join(ROOT, "docs/dora-release-handoff-#{handoff_version}.yaml")
failures << "release handoff is missing for pinned Dora ref #{distribution["source_ref"]}" unless File.file?(handoff_path)
abort "Dora/MuffinMan compatibility audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
handoff = YAML.load_file(handoff_path)
release = handoff.fetch("release", {})
consumption = handoff.fetch("consumption", {})
failures << "current Dora handoff is not published and pinned" unless handoff["status"] == "published" && consumption["status"] == "pinned"
failures << "MuffinMan adapter ref differs from current Dora handoff" unless distribution["source_ref"] == release["version"] && distribution["source_ref"] == consumption["pinned_ref"]
failures << "MuffinMan adapter commit differs from current Dora handoff" unless distribution["source_commit"] == release["immutable_commit"] && distribution["source_commit"] == consumption["pinned_commit"]

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
