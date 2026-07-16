#!/usr/bin/env ruby
require "yaml"

matrix = YAML.load_file("docs/runtime-acceptance-matrix.yaml")
scenarios = matrix.fetch("scenarios")
raise "Runtime acceptance matrix must contain scenarios" if scenarios.empty?

seen = {}
scenarios.each do |scenario|
  id = scenario.fetch("id")
  raise "Duplicate runtime scenario: #{id}" if seen[id]
  seen[id] = true
  %w[capability_ids target actions required_evidence status].each do |key|
    raise "#{id} is missing #{key}" unless scenario[key]
  end
  raise "#{id} must remain pending until runtime evidence exists" unless scenario["status"] == "pending_runtime"
end

router = File.read("apps/themuffinman/frontend/src/router.ts")
missing_targets = scenarios.each_with_object([]) do |scenario, missing|
  target = scenario.fetch("target").sub(/:\w+/, "").sub(%r{^/}, "")
  missing << scenario.fetch("id") unless router.include?(target)
end
raise "Runtime targets missing from router: #{missing_targets.join(', ')}" unless missing_targets.empty?

puts "Runtime acceptance audit passed (#{scenarios.length} pending runtime scenarios)."
