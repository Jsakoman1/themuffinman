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
plugins = Array(manifest["plugins"])
plugins_by_id = plugins.to_h { |plugin| [plugin["id"], plugin] }
wrapper_plugins = {
  "audit-api-contract-drift.rb" => "http-contract-drift",
  "audit-canonical-source-integrity.rb" => "canonical-source-integrity",
  "audit-configuration-environment-drift.rb" => "spring-configuration-drift",
  "audit-endpoint-callsite-linker.rb" => "http-contract-linker",
  "audit-frontend-interaction-contract.rb" => "vue-interaction-hygiene",
  "audit-frontend-route-surfaces.rb" => "vue-navigation",
  "audit-frontend-stale-surfaces.rb" => "vue-stale-surface-hygiene",
  "audit-mapper-usage.rb" => "spring-mapper-usage",
  "audit-module-dependency-direction.rb" => "architecture-integrity",
  "audit-read-surface-inventory.rb" => "vue-read-surface-hygiene",
  "audit-ui-entrypoints.rb" => "vue-ui-entrypoints"
}
wrapper_plugins.each do |name, plugin_id|
  plugin = plugins_by_id[plugin_id]
  failures << "delegated wrapper #{name} has no Dora built-in manifest plugin" unless plugin && plugin["builtin"].to_s != ""
  wrapper = File.read(File.join(ROOT, "scripts/audits", name))
  expected = "exec(\"bin/dora\", \"plugin-run\", \".dora/plugins.yaml\", \"#{plugin_id}\")"
  failures << "delegated wrapper #{name} contains local analysis instead of a Dora execution shell" unless wrapper.include?(expected) && !wrapper.include?("DORA_PLUGIN_RUNNER") && wrapper.lines.grep_v(/^#!|^#|^\s*$/).length == 1
end
plugins.each do |plugin|
  failures << "plugin #{plugin["id"]} has no Dora built-in" if plugin["builtin"].to_s.empty?
  failures << "plugin #{plugin["id"]} has no report path" unless plugin.dig("output", "path").to_s.match?(/\Adocs\/audit-output\//)
end
abort "Dora/MuffinMan tool ownership audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora/MuffinMan tool ownership audit passed (#{actual.length} local audits classified)."
