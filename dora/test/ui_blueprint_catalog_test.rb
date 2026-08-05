#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "ui-blueprint-catalog.schema.yaml"))
catalog = YAML.load_file(File.join(ROOT, "templates/ui-blueprints.yaml"))
abort "UI catalog schema is invalid" unless schema["kind"] == "dora_ui_blueprint_catalog_schema" && schema["version"] == 1
abort "UI catalog is invalid" unless catalog["kind"] == "dora_ui_blueprint_catalog" && catalog["version"] == 1
ids = catalog.fetch("blueprints").map { |blueprint| blueprint.fetch("id") }
abort "required UI blueprints are missing" unless %w[list-detail filterable-list status-badge dashboard-summary].all? { |id| ids.include?(id) }
abort "catalog is missing accessibility baseline" unless catalog.fetch("accessibility_baseline").length >= 4
content = File.read(File.join(ROOT, "templates/ui-blueprints.yaml")) + File.read(File.join(ROOT, "docs/ui-accessibility-blueprints.md"))
abort "catalog includes product copy" if content.match?(/doomsday|muffinman|supply|reservation/i)
puts "Dora UI blueprint catalog test passed (neutral list/detail, filters, status, states, mobile, and accessibility guidance is present)."
