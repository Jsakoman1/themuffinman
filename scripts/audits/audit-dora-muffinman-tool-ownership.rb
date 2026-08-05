#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("../..", __dir__)
CATALOG_PATH = File.join(ROOT, "docs/dora-muffinman-tool-ownership.yaml")
catalog = YAML.load_file(CATALOG_PATH)
abort "tool ownership catalog kind is invalid" unless catalog["kind"] == "dora_muffinman_tool_ownership" && catalog["version"].to_i == 1
groups = Array(catalog["groups"])
abort "tool ownership catalog is empty" if groups.empty?
allowed = %w[extract delegate product_retained]
failures = []
groups.each do |group|
  failures << "ownership group is missing id, decision, or reason" if group["id"].to_s.empty? || !allowed.include?(group["decision"]) || group["reason"].to_s.empty?
  failures << "ownership group #{group["id"]} has no audits" if Array(group["audits"]).empty?
end
declared = groups.flat_map { |group| Array(group["audits"]) }
failures << "ownership audit entries are duplicated" unless declared.uniq.length == declared.length
actual = Dir.glob(File.join(ROOT, "scripts/audits/audit-*.rb")).map { |path| File.basename(path) }.sort
failures << "unclassified local audits: #{(actual - declared).join(", ")}" unless (actual - declared).empty?
failures << "catalog references missing audits: #{(declared - actual).join(", ")}" unless (declared - actual).empty?
delegate = groups.select { |group| group["decision"] == "delegate" }.flat_map { |group| group["audits"] }
reusable_group = groups.find { |group| group["id"] == "reusable_static_analysis" }
failures << "reusable static analysis must require the shared plugin manifest" unless reusable_group && reusable_group["manifest_required"] == true && reusable_group["manifest_path"] == ".dora/plugins.yaml"
manifest = YAML.load_file(File.join(ROOT, ".dora/plugins.yaml"))
entrypoints = Array(manifest["plugins"]).map { |plugin| plugin["entrypoint"] }
reusable_wrappers = delegate.grep(/\Aaudit-(api-contract-drift|canonical-source-integrity|configuration-environment-drift|endpoint-callsite-linker|frontend-interaction-contract|frontend-route-surfaces|frontend-stale-surfaces|mapper-usage|module-dependency-direction|read-surface-inventory|ui-entrypoints)\.rb\z/)
missing_manifest = reusable_wrappers.reject { |name| entrypoints.include?("scripts/audits/#{name}") }
failures << "delegated wrappers missing manifest entrypoints: #{missing_manifest.join(", ")}" unless missing_manifest.empty?
Array(manifest["plugins"]).each { |plugin| failures << "plugin #{plugin["id"]} has no report path" unless plugin.dig("output", "path").to_s.match?(/\Adocs\/audit-output\//) }
abort "Dora/MuffinMan tool ownership audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora/MuffinMan tool ownership audit passed (#{actual.length} local audits classified)."
