#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugin_adapter"

Dir.mktmpdir("dora-plugin-adapter") do |root|
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "src", "sample.rb"), "safe\n")
  architecture = Dora::PluginAdapter.run!(id: "architecture-integrity", root: root, inputs: {"paths" => ["src/sample.rb"], "rules" => [{"id" => "forbidden", "source_glob" => "src/**/*.rb", "forbidden_pattern" => "forbidden"}]})
  abort "architecture adapter did not return findings" unless architecture.fetch("findings").first.fetch("forbidden").empty?
  File.write(File.join(root, "router.ts"), "{ path: '/home' }\n")
  File.write(File.join(root, "nav.ts"), "home\n")
  navigation = Dora::PluginAdapter.run!(id: "vue-navigation", root: root, inputs: {"router_path" => "router.ts", "navigation_paths" => ["nav.ts"], "required_surfaces" => ["home"]})
  abort "navigation adapter did not return route findings" unless navigation.fetch("findings").first.fetch("routes") == ["/home"]
end

puts "Dora plugin adapter test passed (portable static-analysis adapters)."
