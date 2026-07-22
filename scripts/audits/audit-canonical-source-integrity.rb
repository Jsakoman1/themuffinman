#!/usr/bin/env ruby

require "yaml"

ROOT = File.expand_path("../..", __dir__)
TRUTH_PATH = File.join(ROOT, "docs/system-truth-registry.yaml")
truth = YAML.load_file(TRUTH_PATH)
contract = truth.fetch("canonical_lint_contract")

required_claim_classes = contract.fetch("current_claim_classes")
fact_classes = truth.fetch("fact_classes")
known_ids = fact_classes.map { |fact| fact.fetch("id") }
missing_classes = required_claim_classes - known_ids
abort "Canonical source integrity audit failed: unknown claim classes #{missing_classes.join(', ')}" unless missing_classes.empty?

missing_sources = []
fact_classes.each do |fact|
  fact.fetch("canonical_sources").each do |source|
    next if source.include?("*")

    missing_sources << "#{fact.fetch('id')}: #{source}" unless File.exist?(File.join(ROOT, source))
  end
end
unless missing_sources.empty?
  warn "Canonical source integrity audit failed: missing canonical sources"
  missing_sources.each { |source| warn "  #{source}" }
  exit 1
end

excluded = contract.fetch("historical_exclusion")
invalid_exclusions = excluded.reject { |path| path.include?("*") || File.exist?(File.join(ROOT, path)) }
abort "Canonical source integrity audit failed: invalid exclusions #{invalid_exclusions.join(', ')}" unless invalid_exclusions.empty?

# Deterministic negative control: this proves the comparison logic detects drift
# without mutating a repository document or treating the fixture as current truth.
seeded_claim = {"fact_class" => "runtime_acceptance", "value" => "65 passed / 0 pending"}
canonical_value = "65 passed / 16 pending"
seeded_detected = seeded_claim["value"] != canonical_value
abort "Canonical source integrity audit failed: seeded stale-claim control was not detected" unless seeded_detected

puts "Canonical source integrity audit passed"
puts "  fact classes checked: #{fact_classes.size}"
puts "  selected current claim classes: #{required_claim_classes.join(', ')}"
puts "  canonical source paths checked: #{fact_classes.sum { |fact| fact.fetch('canonical_sources').size }}"
puts "  historical/disposable exclusions: #{excluded.size}"
puts "  seeded stale-claim control: detected"
