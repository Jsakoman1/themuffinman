#!/usr/bin/env ruby

require "json"
require "yaml"
require "fileutils"
require "time"

ROOT = File.expand_path("../..", __dir__)
capability_path = File.join(ROOT, "docs/capability-evidence-registry.yaml")
runtime_path = File.join(ROOT, "docs/runtime-acceptance-matrix.yaml")
capability = YAML.load_file(capability_path)
runtime = YAML.load_file(runtime_path)
families = capability.fetch("capability_families")
scenarios = runtime.fetch("scenarios")
passed = scenarios.count { |scenario| scenario["status"] == "passed" }
pending = scenarios.count { |scenario| scenario["status"] == "pending_runtime" }

rows = families.map do |family|
  pending_names = Array(family["known_pending"])
  {
    "capability_family" => family.fetch("id"),
    "edges" => ["capability", "source", "endpoint", "client", "test", "runtime", "evidence"],
    "declared_evidence_layers" => family.fetch("evidence_layers"),
    "runtime_state" => family.fetch("runtime_state"),
    "missing_edges" => pending_names.map { |name| {"edge" => "runtime_or_external", "id" => name, "status" => "gap"} },
    "native_provider_operator_boundary" => family.fetch("id") == "native_mobile_watch" ? "unknown_without_device_trace" : "separate_evidence_required"
  }
end

report = {
  "generated_at" => Time.now.utc.iso8601,
  "advisory_only" => true,
  "runtime_counts" => {"passed" => passed, "pending" => pending},
  "rows" => rows,
  "boundary" => "Missing edges identify navigable evidence gaps. They do not set capability status, runtime status, or production readiness."
}

output_dir = File.join(ROOT, "docs/audit-output")
FileUtils.mkdir_p(output_dir)
File.write(File.join(output_dir, "capability-evidence-coverage.json"), JSON.pretty_generate(report) + "\n")
File.write(File.join(output_dir, "capability-evidence-coverage-summary.md"), <<~MARKDOWN)
  # Capability Evidence Coverage

  - Advisory only: missing edges do not set capability or runtime status.
  - Runtime counts: #{passed} passed / #{pending} pending.
  - Capability families: #{rows.length}.
  - Native, provider, and operator evidence remain separate boundaries.
MARKDOWN

puts "Capability evidence coverage report generated"
puts "  capability families: #{rows.length}"
puts "  runtime counts: #{passed} passed / #{pending} pending"
puts "  missing-edge semantics: explicit and advisory"
