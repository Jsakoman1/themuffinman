#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../../dora/lib/dora/plugins/vue_navigation"

ROOT = File.expand_path("../..", __dir__)
surfaces = %w[home work chat calendar business circles things rides profile]
result = Dora::Plugins::VueNavigation.analyze!(root: ROOT, router_path: "apps/themuffinman/frontend/src/router.ts", navigation_paths: ["apps/themuffinman/frontend/src/modules/app-shell/shellDefinitions.ts", "apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts"], required_surfaces: surfaces)
missing = result.fetch("surfaces").select { |surface| !surface["in_navigation"] }.map { |surface| surface["id"] }
abort "UI entry-point audit failed: missing navigation surfaces #{missing.join(", ")}" unless missing.empty?
puts "UI entry-point audit passed (declared navigation surfaces are configured; static analysis only)."
