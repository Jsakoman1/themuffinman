#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

Dir.mktmpdir("dora-plugin-finding-pipeline") do |root|
  FileUtils.mkdir_p(File.join(root, "plugins"))
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "plugins", "alpha.rb"), "puts 'alpha diagnostic'\n")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "alpha", "entrypoint" => "plugins/alpha.rb", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "fixture"}, "output" => {"kind" => "static-analysis-report", "path" => "reports/alpha.json"}}]}
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))
  report = Dora::PluginRunner.run!(manifest_path, plugin_id: "alpha", project_root: root)
  finding = report.fetch("findings").first
  abort "plugin finding is not standard" unless finding.slice("kind", "version", "severity") == {"kind" => "dora_finding", "version" => 1, "severity" => "info"}
  abort "plugin finding lost raw diagnostic output" unless finding.dig("details", "output").include?("alpha diagnostic")
  abort "plugin finding claims completion" unless finding.fetch("diagnostic_boundary").include?("does not prove")
end

puts "Dora plugin finding pipeline test passed (declared plugin output becomes standard diagnostic findings)."
