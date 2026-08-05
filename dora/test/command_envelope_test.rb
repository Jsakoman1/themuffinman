#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/command_envelope"

success = Dora::CommandEnvelope.success(payload: {"action" => "inspect"}, citations: ["docs/project-memory.yaml"])
abort "success envelope is incomplete" unless success.values_at("kind", "outcome", "side_effect") == ["dora_command_envelope", "success", "read_only"]
error = Dora::CommandEnvelope.error(message: "answers are invalid", remediation: "Update dora_source checksum.", citations: ["project-new.yaml"])
abort "error envelope is incomplete" unless error.values_at("outcome", "remediation") == ["error", "Update dora_source checksum."]
begin
  Dora::CommandEnvelope.success(payload: {}, citations: ["../secret"])
  abort "envelope accepted an unsafe citation"
rescue ArgumentError
end
schema = YAML.load_file(File.expand_path("../command-envelope.schema.yaml", __dir__))
abort "envelope schema lacks side-effect invariant" unless schema.fetch("invariants").join(" ").include?("Side-effect")
puts "Dora command envelope test passed (stable success, error, citation, and side-effect contracts)."
