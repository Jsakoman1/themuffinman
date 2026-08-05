#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

Dir.mktmpdir("dora-vue-builtin") do |root|
  FileUtils.mkdir_p(File.join(root, "frontend", "src"))
  File.write(File.join(root, "frontend", "src", "router.ts"), "{ path: '/items/:id' }")
  File.write(File.join(root, "frontend", "src", "navigation.ts"), "const surface = 'items'")
  File.write(File.join(root, "frontend", "src", "Item.vue"), "<button aria-label=\"Open item\">Open</button><section data-read-surface=\"item\"></section>")
  manifest = {
    "kind" => "dora_plugin_manifest", "version" => 1,
    "plugins" => [
      {"id" => "navigation", "builtin" => "vue-navigation", "source_roots" => [{"id" => "frontend", "path" => "frontend/src"}], "inputs" => {"router_path" => "frontend/src/router.ts", "navigation_paths" => ["frontend/src/navigation.ts"], "required_surfaces" => ["items"]}, "output" => {"kind" => "static-analysis-report", "path" => "reports/navigation.json"}},
      {"id" => "surfaces", "builtin" => "vue-surface-hygiene", "source_roots" => [{"id" => "frontend", "path" => "frontend/src"}], "inputs" => {"source_glob" => "frontend/src/**/*.vue", "required_markers" => ["aria-label"], "required_read_markers" => ["data-read-surface"], "required_entrypoints" => ["Open item"], "forbidden_markers" => ["legacy-only"]}, "output" => {"kind" => "static-analysis-report", "path" => "reports/surfaces.json"}}
    ]
  }
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))

  navigation = Dora::PluginRunner.run!(manifest_path, plugin_id: "navigation", project_root: root)
  abort "Vue navigation built-in did not link declared surface" unless navigation.fetch("findings").first.fetch("surfaces").first.fetch("in_router")
  surfaces = Dora::PluginRunner.run!(manifest_path, plugin_id: "surfaces", project_root: root).fetch("findings").first
  abort "Vue surface built-in missed read-surface marker" unless surfaces.fetch("read_markers").first.fetch("present")
  abort "Vue surface built-in reported an absent stale marker" unless surfaces.fetch("stale_markers").empty?
  abort "Vue built-in reports are missing" unless %w[navigation.json surfaces.json].all? { |name| File.file?(File.join(root, "reports", name)) }
end

puts "Dora Vue built-in plugin test passed (navigation, interaction, stale, read-surface, and entrypoint evidence)."
