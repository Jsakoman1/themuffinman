#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "capability-proof-matrix.schema.yaml"))
template = YAML.load_file(File.join(ROOT, "templates/capability-proof-matrix.yaml"))

abort "proof matrix schema is invalid" unless schema["kind"] == "dora_capability_proof_matrix_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| template.key?(field) }
abort "proof matrix template is missing #{missing.join(", ")}" unless missing.empty?
assertion_ids = template.fetch("assertions").map do |assertion|
  missing_assertion = schema.fetch("assertion_required_fields").reject { |field| assertion.key?(field) }
  abort "proof matrix assertion is incomplete" unless missing_assertion.empty?
  assertion.fetch("id")
end
template.fetch("obligations").each do |obligation|
  missing_obligation = schema.fetch("obligation_required_fields").reject { |field| obligation.key?(field) }
  abort "proof matrix obligation is incomplete" unless missing_obligation.empty?
  abort "proof matrix obligation has unknown assertion" unless assertion_ids.include?(obligation.fetch("assertion_id"))
  abort "proof matrix evidence class is invalid" unless schema.fetch("evidence_classes").include?(obligation.fetch("evidence_class"))
end
abort "proof matrix template must preserve unresolved runtime proof" unless template.fetch("obligations").any? { |obligation| obligation.fetch("evidence_class") == "browser_runtime" && obligation.fetch("status") == "unresolved" }

puts "Dora capability proof matrix test passed (confirmed assertions and unresolved evidence obligations remain explicit)."
