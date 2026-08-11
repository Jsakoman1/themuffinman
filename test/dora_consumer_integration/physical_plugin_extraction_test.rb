#!/usr/bin/env ruby
# frozen_string_literal: true

# This is a MuffinMan consumer integration test. Dora's independent release lane
# must not need the consumer manifest, wrapper scripts, or application source.
require "yaml"

ROOT = File.expand_path("../..", __dir__)
manifest = YAML.load_file(File.join(ROOT, ".dora/plugins.yaml"))
plugins = Array(manifest.fetch("plugins")).to_h { |plugin| [plugin.fetch("id"), plugin] }
wrappers = {
  "scripts/audits/audit-api-contract-drift.rb" => "http-contract-drift",
  "scripts/audits/audit-canonical-source-integrity.rb" => "canonical-source-integrity",
  "scripts/audits/audit-configuration-environment-drift.rb" => "spring-configuration-drift",
  "scripts/audits/audit-endpoint-callsite-linker.rb" => "http-contract-linker",
  "scripts/audits/audit-frontend-interaction-contract.rb" => "vue-interaction-hygiene",
  "scripts/audits/audit-frontend-route-surfaces.rb" => "vue-navigation",
  "scripts/audits/audit-frontend-stale-surfaces.rb" => "vue-stale-surface-hygiene",
  "scripts/audits/audit-mapper-usage.rb" => "spring-mapper-usage",
  "scripts/audits/audit-module-dependency-direction.rb" => "architecture-integrity",
  "scripts/audits/audit-read-surface-inventory.rb" => "vue-read-surface-hygiene",
  "scripts/audits/audit-ui-entrypoints.rb" => "vue-ui-entrypoints"
}
wrappers.each do |path, plugin_id|
  plugin = plugins[plugin_id]
  abort "#{path} is not backed by a Dora built-in plugin" unless plugin && plugin.fetch("builtin").match?(/\A[a-z][a-z0-9-]*\z/)
  source = File.read(File.join(ROOT, path))
  expected = "exec(\"dora/bin/dora\", \"plugin-run\", \".dora/plugins.yaml\", \"#{plugin_id}\")"
  statements = source.lines.grep_v(/^#!|^#|^\s*$/)
  abort "#{path} retains a reusable local analysis algorithm" unless statements == ["#{expected}\n"]
end

puts "MuffinMan Dora plugin wrapper integration test passed."
