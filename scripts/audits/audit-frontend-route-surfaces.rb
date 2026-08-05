#!/usr/bin/env ruby
# frozen_string_literal: true

exec("dora/bin/dora", "plugin-run", ".dora/plugins.yaml", "vue-navigation") unless ENV["DORA_PLUGIN_RUNNER"] == "1"

require "json"
require "fileutils"
require "time"
require_relative "../../dora/lib/dora/plugins/vue_navigation"

ROOT = File.expand_path("../..", __dir__)
report = Dora::Plugins::VueNavigation.analyze!(root: ROOT, router_path: "apps/themuffinman/frontend/src/router.ts", navigation_paths: ["apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts"], required_surfaces: [])
FileUtils.mkdir_p(File.join(ROOT, "docs/audit-output"))
File.write(File.join(ROOT, "docs/audit-output/frontend-route-surface-inventory.json"), JSON.pretty_generate(report.merge("generated_at" => Time.now.utc.iso8601, "advisory_only" => true)) + "\n")
puts "Frontend route surface audit passed (#{report.fetch("routes").length} declared routes; static navigation only)."
