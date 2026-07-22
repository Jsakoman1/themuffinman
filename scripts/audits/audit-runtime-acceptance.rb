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

closeout = YAML.load_file("docs/runtime-evidence/runtime-capability-closeout-2026-07-22.yaml")
closeout_audit = closeout.fetch("audit")
unless closeout_audit.fetch("passed") == passed && closeout_audit.fetch("pending_runtime") == pending
  raise "Runtime closeout artifact is stale: matrix=#{passed}/#{pending}, closeout=#{closeout_audit.fetch("passed")}/#{closeout_audit.fetch("pending_runtime")}"
end

closeout_registry = YAML.load_file("docs/system-map-runtime-closeout-registry.yaml").fetch("baseline")
unless closeout_registry.fetch("runtime_passed") == passed && closeout_registry.fetch("runtime_pending") == pending
  raise "System-map runtime closeout registry is stale: matrix=#{passed}/#{pending}, registry=#{closeout_registry.fetch("runtime_passed")}/#{closeout_registry.fetch("runtime_pending")}"
end

closeout_row = scenarios.find { |scenario| scenario["id"] == "runtime-capability-closeout" }
note = closeout_row&.fetch("note", "")
unless note.include?("#{passed} passed") && note.include?("#{pending} explicitly classified pending")
  raise "Runtime capability closeout matrix note is stale"
end

capability_evidence = YAML.load_file("docs/capability-evidence-registry.yaml")
runtime_summary = capability_evidence.fetch("runtime_matrix")
unless runtime_summary.fetch("passed_scenarios") == passed && runtime_summary.fetch("pending_runtime_scenarios") == pending
  raise "Capability evidence registry runtime summary is stale: matrix=#{passed}/#{pending}, registry=#{runtime_summary.fetch("passed_scenarios")}/#{runtime_summary.fetch("pending_runtime_scenarios")}"
end

truth_registry = YAML.load_file("docs/system-truth-registry.yaml")
raise "Truth registry is missing optimization baseline link" unless truth_registry.fetch("optimization_baseline") == "docs/system-map-optimization-baseline-2026-07-22.yaml"
raise "Runtime matrix is missing status authority metadata" unless matrix.fetch("status_authority") == "runtime_acceptance_matrix"

puts "Runtime acceptance audit passed (#{passed} passed, #{pending} pending runtime scenarios)."
