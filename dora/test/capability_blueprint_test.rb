#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "capability-blueprint.schema.yaml"))
blueprint = YAML.load_file(File.join(ROOT, "templates/capability-blueprint.yaml"))
abort "capability blueprint schema is invalid" unless schema["kind"] == "dora_capability_blueprint_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| blueprint.key?(field) && !blueprint[field].nil? }
abort "capability blueprint template is missing #{missing.join(', ')}" unless missing.empty?
abort "capability blueprint does not assign service ownership" unless blueprint.dig("ownership", "service_owner").is_a?(String)
abort "capability blueprint lacks evidence separation" unless %w[static tests runtime].all? { |kind| blueprint.dig("evidence", kind).is_a?(Array) }
guide = File.read(File.join(ROOT, "docs/architecture-capability-blueprint.md"))
abort "capability blueprint guide does not protect backend ownership" unless guide.include?("business rules") && guide.include?("Clients")

puts "Dora capability blueprint test passed (ownership, API, state, permissions, validation, evidence, and client boundaries)."
