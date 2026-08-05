#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../../dora/lib/dora/plugins/vue_surface_hygiene"

ROOT = File.expand_path("../..", __dir__)
result = Dora::Plugins::VueSurfaceHygiene.scan!(root: ROOT, source_glob: "apps/themuffinman/frontend/src/**/*.vue", required_markers: ["aria-selected", "role=\"dialog\"", "role=\"search\""])
abort "Frontend interaction contract audit failed: missing markers #{result.fetch("missing_markers").join(", ")}" unless result.fetch("missing_markers").empty?
puts "Frontend interaction contract audit passed through Dora static surface hygiene."
