#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

CLI = File.expand_path("../bin/dora", __dir__)

Dir.mktmpdir("dora-plugin-runner") do |root|
  FileUtils.mkdir_p(File.join(root, "plugins"))
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "plugins", "alpha.rb"), "puts 'alpha plugin ran'\n")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "alpha", "entrypoint" => "plugins/alpha.rb", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "test"}, "output" => {"kind" => "static-analysis-report", "path" => "reports/alpha.json"}}]}
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))

  result = Dora::PluginRunner.run!(manifest_path, plugin_id: "alpha", project_root: root)
  abort "declared plugin did not run" unless result.fetch("findings").first.fetch("output").include?("alpha plugin ran")
  abort "plugin report was not written" unless File.read(File.join(root, "reports/alpha.json")).include?("does not prove")
  _output, status = Open3.capture2e(CLI, "plugin-run", manifest_path, "missing", chdir: root)
  abort "runner accepted an undeclared plugin" if status.success?
end

puts "Dora plugin runner test passed (declared local plugins only)."
