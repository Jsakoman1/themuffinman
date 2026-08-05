#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/vue_surface_hygiene"

Dir.mktmpdir("dora-vue-hygiene") do |root|
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "src/A.vue"), "<button aria-label=\"Open\">Open</button>")
  result = Dora::Plugins::VueSurfaceHygiene.scan!(root: root, source_glob: "src/**/*.vue", required_markers: ["aria-label", "missing"])
  abort "surface hygiene scan is wrong" unless result == {"files" => ["src/A.vue"], "missing_markers" => ["missing"]}
end

puts "Dora Vue surface hygiene plugin test passed (declared sources and markers)."
