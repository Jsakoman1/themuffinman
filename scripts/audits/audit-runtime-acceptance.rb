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
  status = scenario.fetch("status")
  unless %w[pending_runtime passed].include?(status)
    raise "#{id} has unsupported runtime status: #{status}"
  end
  if status == "passed"
    evidence = Array(scenario["evidence"])
    raise "#{id} passed without evidence references" if evidence.empty?
    missing_evidence = evidence.reject { |path| File.file?(path.to_s) }
    raise "#{id} references missing evidence: #{missing_evidence.join(', ')}" unless missing_evidence.empty?
  end
end

router = File.read("apps/themuffinman/frontend/src/router.ts")
missing_targets = scenarios.each_with_object([]) do |scenario, missing|
  target = scenario.fetch("target").sub(/:\w+/, "").sub(%r{^/}, "")
  missing << scenario.fetch("id") unless router.include?(target)
end
raise "Runtime targets missing from router: #{missing_targets.join(', ')}" unless missing_targets.empty?

pending = scenarios.count { |scenario| scenario["status"] == "pending_runtime" }
passed = scenarios.count { |scenario| scenario["status"] == "passed" }
puts "Runtime acceptance audit passed (#{passed} passed, #{pending} pending runtime scenarios)."
