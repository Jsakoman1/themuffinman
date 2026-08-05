#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"
require_relative "../lib/dora/plugins/architecture_integrity"

fixture = YAML.load_file(File.expand_path("../fixtures/source-analysis/architecture-rules.yaml", __dir__))
Dir.mktmpdir("dora-architecture-builtin") do |root|
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "src", "safe.rb"), "module Safe; end\n")
  direct = Dora::Plugins::ArchitectureIntegrity.analyze!(root: root, source_roots: fixture.fetch("source_roots"), inputs: fixture.fetch("inputs"))
  abort "architecture analyzer did not validate declared roots" unless direct.fetch("paths") == ["src"]
  abort "architecture analyzer reported a false finding" unless direct.fetch("forbidden").empty?

  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "architecture", "builtin" => "architecture-integrity", "source_roots" => fixture.fetch("source_roots"), "inputs" => fixture.fetch("inputs"), "output" => fixture.fetch("output")}]} 
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))
  report = Dora::PluginRunner.run!(manifest_path, plugin_id: "architecture", project_root: root)
  abort "architecture rule did not run through Dora built-in execution" unless report.fetch("builtin") == "architecture-integrity"
  abort "architecture built-in report is missing" unless File.file?(File.join(root, "reports", "architecture-integrity.json"))
end

puts "Dora architecture built-in plugin test passed (declared portable rules and diagnostic report)."
