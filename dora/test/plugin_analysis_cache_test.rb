#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

Dir.mktmpdir("dora-plugin-analysis-cache") do |root|
  FileUtils.mkdir_p(File.join(root, "plugins"))
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "plugins", "alpha.rb"), "puts 'alpha analysis'\n")
  File.write(File.join(root, "src", "note.txt"), "first")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "alpha", "entrypoint" => "plugins/alpha.rb", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "fixture"}, "output" => {"kind" => "static-analysis-report", "path" => "reports/alpha.json"}}]}
  path = File.join(root, "plugins.yaml")
  File.write(path, YAML.dump(manifest))
  first = Dora::PluginRunner.run!(path, plugin_id: "alpha", project_root: root)
  second = Dora::PluginRunner.run!(path, plugin_id: "alpha", project_root: root)
  File.write(File.join(root, "src", "note.txt"), "changed")
  changed = Dora::PluginRunner.run!(path, plugin_id: "alpha", project_root: root)
  abort "first plugin run was unexpectedly cached" if first.dig("cache", "hit")
  abort "unchanged plugin inputs did not hit cache" unless second.dig("cache", "hit") && second.dig("cache", "input_digest") == first.dig("cache", "input_digest")
  abort "changed plugin input did not invalidate cache" if changed.dig("cache", "hit") || changed.dig("cache", "input_digest") == first.dig("cache", "input_digest")
end

puts "Dora plugin analysis cache test passed (declared plugin analysis reports hits and invalidates changed source inputs)."
