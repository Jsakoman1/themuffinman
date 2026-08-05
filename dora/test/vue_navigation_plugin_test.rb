#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/vue_navigation"

Dir.mktmpdir("dora-vue-navigation") do |root|
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "src/router.ts"), "{ path: '/home' }\n{ path: '/jobs/:id' }")
  File.write(File.join(root, "src/nav.ts"), "home jobs")
  result = Dora::Plugins::VueNavigation.analyze!(root: root, router_path: "src/router.ts", navigation_paths: ["src/nav.ts"], required_surfaces: %w[home jobs chat])
  abort "route extraction is wrong" unless result.fetch("routes") == ["/home", "/jobs/:id"]
  abort "surface classification is wrong" unless result.fetch("surfaces").map { |row| [row["in_navigation"], row["in_router"]] } == [[true, true], [true, true], [false, false]]
end

puts "Dora Vue navigation plugin test passed (declared router and navigation sources)."
