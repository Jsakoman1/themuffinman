#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_discovery"

Dir.mktmpdir("dora-plugin-discovery") do |root|
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "architecture", "builtin" => "architecture-integrity", "source_roots" => [{"id" => "backend", "path" => "backend"}], "inputs" => {"rules" => ["controller"]}, "output" => {"kind" => "report", "path" => "docs/audit-output/architecture.json"}}]}
  path = File.join(root, "plugins.yaml")
  File.write(path, YAML.dump(manifest))
  catalog = Dora::PluginDiscovery.catalog!(manifest_path: path)
  abort "plugin discovery omitted supported built-ins" unless catalog.fetch("builtins").include?("architecture-integrity")
  declared = catalog.fetch("declared_plugins").first
  abort "plugin discovery omitted declared inputs" unless declared.dig("inputs", "rules") == ["controller"]
  abort "plugin discovery omitted declared source roots" unless declared.fetch("source_roots") == [{"id" => "backend", "path" => "backend"}]
end

puts "Dora plugin discovery test passed (built-ins and declared plugin inputs are inspectable)."
