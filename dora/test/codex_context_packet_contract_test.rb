#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "codex-context-packet.schema.yaml"))
packet = YAML.load_file(File.join(ROOT, "test/fixtures/codex-context-packet.yaml"))

abort "context packet schema is invalid" unless schema["kind"] == "dora_codex_context_packet_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| packet.key?(field) }
abort "context packet fixture is missing #{missing.join(", ")}" unless missing.empty?
missing_confirmed = schema.fetch("confirmed_sections").reject { |section| packet.fetch("confirmed").key?(section) }
abort "context packet fixture lacks confirmed sections #{missing_confirmed.join(", ")}" unless missing_confirmed.empty?
missing_task = schema.fetch("task_required_fields").reject { |field| packet.fetch("task").key?(field) }
abort "context packet fixture task is incomplete" unless missing_task.empty?
packet.fetch("proof_obligations").each do |obligation|
  missing_obligation = schema.fetch("proof_obligation_required_fields").reject { |field| obligation.key?(field) }
  abort "context packet proof obligation is incomplete" unless missing_obligation.empty?
  abort "context packet evidence class is invalid" unless schema.fetch("evidence_classes").include?(obligation.fetch("evidence_class"))
end
abort "context packet fixture must state omissions" unless packet.fetch("omitted").include?("product-inference")
abort "context packet fixture must state a non-completion boundary" unless packet.fetch("completion_boundary").include?("does not")

puts "Dora Codex context packet contract test passed (cited confirmed context, allowed paths, proof obligations, and omissions are explicit)."
