#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)

Dir.mktmpdir("dora-finding-export-command") do |root|
  path = File.join(root, "plugin-report.json")
  report = {"kind" => "dora_plugin_report", "version" => 1, "finding_contract" => "dora_finding", "findings" => [{"kind" => "dora_finding", "version" => 1, "id" => "alpha-missing-test", "severity" => "warning", "location" => {"path" => "src/item.rb", "line" => 8}, "explanation" => "The source has no declared test.", "repair" => "Add a focused test.", "evidence" => ["plugin:alpha"], "diagnostic_boundary" => "does not prove completion"}]}
  File.write(path, JSON.generate(report))
  before = File.read(path)
  output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "findings-export", path)
  abort "finding export command failed: #{output}" unless status.success?
  export = YAML.safe_load(output)
  abort "finding export command lost warning" unless export.dig("annotations", 0, "level") == "warning"
  abort "finding export command is not read-only" unless export.fetch("read_only") && File.read(path) == before
  abort "finding export command made a completion claim" unless export.fetch("diagnostic_boundary").include?("do not prove")
end

puts "Dora finding export command test passed (standard findings export through a read-only public CLI command)."
