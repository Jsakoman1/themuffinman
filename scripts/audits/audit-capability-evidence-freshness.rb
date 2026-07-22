#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

matrix = YAML.load_file("docs/runtime-acceptance-matrix.yaml")
registry = YAML.load_file("docs/capability-evidence-registry.yaml")
scenarios = matrix.fetch("scenarios")
passed = scenarios.select { |scenario| scenario["status"] == "passed" }
pending = scenarios.select { |scenario| scenario["status"] == "pending_runtime" }
abort "runtime matrix has no passed scenarios" if passed.empty?

missing = passed.flat_map { |scenario| Array(scenario["evidence"]) }.reject { |path| File.file?(path) }
abort "passed runtime scenarios reference missing evidence: #{missing.uniq.join(', ')}" unless missing.empty?
abort "capability evidence registry must state status/evidence separation" unless Array(registry["rules"]).include?("implemented_and_verified_are_distinct_inventory_states")
abort "capability evidence registry must state runtime evidence boundary" unless Array(registry["rules"]).include?("static_audit_and_source_trace_do_not_replace_runtime_evidence")

puts "Capability evidence freshness audit passed"
puts "  passed runtime scenarios: #{passed.length}"
puts "  pending runtime scenarios: #{pending.length}"
puts "  passed evidence paths checked: #{passed.flat_map { |scenario| Array(scenario['evidence']) }.uniq.length}"
