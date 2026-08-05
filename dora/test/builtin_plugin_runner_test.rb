#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

Dir.mktmpdir("dora-builtin-plugin-runner") do |root|
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "src", "Entry.java"), "class Entry {}\n")
  manifest = {
    "kind" => "dora_plugin_manifest", "version" => 1,
    "plugins" => [{
      "id" => "architecture-check", "builtin" => "architecture-integrity",
      "source_roots" => [{"id" => "backend", "path" => "src"}],
      "inputs" => {"paths" => ["src"], "rules" => [{"id" => "no-secrets", "source_glob" => "src/**/*.java", "forbidden_pattern" => "secret"}]},
      "output" => {"kind" => "static-analysis-report", "path" => "reports/architecture.json"}
    }]
  }
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))

  result = Dora::PluginRunner.run!(manifest_path, plugin_id: "architecture-check", project_root: root)
  abort "built-in plugin was not selected" unless result.fetch("builtin") == "architecture-integrity"
  abort "built-in report was not written" unless File.read(File.join(root, "reports/architecture.json")).include?("architecture-check")
  abort "built-in manifest unexpectedly needs a project Ruby file" if File.exist?(File.join(root, "plugins"))

  manifest.fetch("plugins").first["builtin"] = "missing-plugin"
  File.write(manifest_path, YAML.dump(manifest))
  begin
    Dora::PluginRunner.run!(manifest_path, plugin_id: "architecture-check", project_root: root)
    abort "runner accepted an unsupported built-in plugin"
  rescue ArgumentError => error
    abort "wrong built-in failure: #{error.message}" unless error.message.include?("not supported")
  end
end

puts "Dora built-in plugin runner test passed (no project Ruby entrypoint required)."
