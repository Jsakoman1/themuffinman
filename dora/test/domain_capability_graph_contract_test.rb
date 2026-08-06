#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "domain-capability-graph.schema.yaml"))
graph = YAML.load_file(File.join(ROOT, "test/fixtures/domain-capability-graph.yaml"))

abort "capability graph schema is invalid" unless schema["kind"] == "dora_domain_capability_graph_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| graph.key?(field) }
abort "capability graph fixture is missing #{missing.join(", ")}" unless missing.empty?
ids = graph.fetch("capabilities").map do |capability|
  fields = schema.fetch("capability_required_fields").reject { |field| capability.key?(field) }
  abort "capability fixture is incomplete" unless fields.empty?
  capability.fetch("id")
end
abort "capability ids must be unique" unless ids.uniq.length == ids.length
decisions = graph.fetch("decisions").map do |decision|
  fields = schema.fetch("decision_required_fields").reject { |field| decision.key?(field) }
  abort "decision fixture is incomplete" unless fields.empty?
  abort "decision status is invalid" unless schema.fetch("decision_statuses").include?(decision.fetch("status"))
  decision.fetch("id")
end
abort "decision ids must be unique" unless decisions.uniq.length == decisions.length
abort "fixture must retain an open decision" unless graph.fetch("decisions").any? { |decision| decision.fetch("status") == "open" }

puts "Dora domain capability graph contract test passed (confirmed capabilities, dependencies, and open decisions remain explicit)."
