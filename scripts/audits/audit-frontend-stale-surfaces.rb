#!/usr/bin/env ruby
# frozen_string_literal: true

exec("dora/bin/dora", "plugin-run", ".dora/plugins.yaml", "vue-stale-surface-hygiene") unless ENV["DORA_PLUGIN_RUNNER"] == "1"

require "json"
require "fileutils"
require_relative "../../dora/lib/dora/plugins/vue_surface_hygiene"

ROOT = File.expand_path("../..", __dir__)
result = Dora::Plugins::VueSurfaceHygiene.scan!(root: ROOT, source_glob: "apps/themuffinman/frontend/src/**/*.{ts,vue,css}")
FileUtils.mkdir_p(File.join(ROOT, "docs/audit-output"))
File.write(File.join(ROOT, "docs/audit-output/frontend-stale-surface-audit.json"), JSON.pretty_generate(result.merge("advisory_only" => true)) + "\n")
puts "Frontend stale surface audit passed (#{result.fetch("files").length} declared static files for review)."
