#!/usr/bin/env ruby
# frozen_string_literal: true

root = File.expand_path("../..", __dir__)
frontend = File.join(root, "apps/themuffinman/frontend/src")
definitions = File.read(File.join(frontend, "modules/app-shell/shellDefinitions.ts"))
registry = File.read(File.join(frontend, "modules/app-shell/shellRouteRegistry.ts"))
renderer = File.read(File.join(frontend, "modules/app-shell/components/SurfaceContentView.vue"))
router = File.read(File.join(frontend, "router.ts"))
violations = []

%w[home work chat calendar business circles things rides profile].each do |surface|
  violations << "#{surface} is missing from primary navigation" unless registry.match?(/"#{surface}"/)
  violations << "#{surface} has no shell configuration" unless definitions.match?(/id:\s*"#{surface}"/)
end

%w[things rides].each do |surface|
  violations << "#{surface} has no authenticated router entry" unless router.match?(/path:\s*['"]#{surface}['"]/) || router.match?(/path:\s*['"]#{surface}\//)
  violations << "#{surface} create handoff has no query consumer" unless definitions.include?("path: \"/#{surface}\", query: {create: \"1\"}") && File.read(File.join(frontend, "modules/app-shell/views/#{surface == 'rides' ? 'RidesView' : 'ThingsDiscoveryView'}.vue")).include?("route.query.create")
end

violations << "surface action renderer truncates configured actions" if renderer.include?("config.actions.slice")

if violations.empty?
  puts "UI entry-point audit passed (navigation, routes, create handoffs, and action rendering are connected)."
else
  warn "UI entry-point audit failed:"
  violations.each { |violation| warn "- #{violation}" }
  exit 1
end
