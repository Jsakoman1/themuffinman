#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "voice-capability-blueprint.schema.yaml"))
blueprint = YAML.load_file(File.join(ROOT, "templates/voice-capability-blueprint.yaml"))
abort "voice blueprint schema is invalid" unless schema["kind"] == "dora_voice_capability_blueprint_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| blueprint.key?(field) && !blueprint[field].nil? }
abort "voice blueprint template is missing #{missing.join(', ')}" unless missing.empty?
abort "voice blueprint does not require explicit confirmation" unless blueprint.dig("confirmation", "rule").downcase.include?("explicit confirmation")
abort "voice blueprint does not keep validation deterministic" unless blueprint.dig("deterministic_validation", "rule").downcase.include?("outside the language model")
guide = File.read(File.join(ROOT, "docs/voice-capability-blueprint.md"))
abort "voice blueprint guide allows unconfirmed execution" unless guide.include?("no execution")
abort "voice blueprint guide does not isolate project authority" unless guide.include?("consuming application owns")

puts "Dora voice capability blueprint test passed (interpretation is separated from validation, confirmation, execution, consent, retention, and evaluation)."
