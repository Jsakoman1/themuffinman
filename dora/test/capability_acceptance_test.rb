#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/capability_acceptance"

item = {"id" => "declared-check", "expected" => "The confirmed behavior can be reviewed.", "confirmed" => true}
input = {"kind" => "dora_capability_acceptance", "version" => 1, "capability" => "record-supply", "acceptance_statements" => [item.dup], "static_checks" => [item.dup], "test_scenarios" => [item.dup], "runtime_scenarios" => [item.dup], "confirmation" => true}
report = Dora::CapabilityAcceptance.declare!(input)
abort "acceptance was fabricated" unless report.fetch("acceptance_status") == "unresolved" && report.fetch("obligations").all? { |item| item.fetch("status") == "unresolved" }
invalid = Marshal.load(Marshal.dump(input)); invalid.fetch("runtime_scenarios").first["confirmed"] = false
begin
  Dora::CapabilityAcceptance.declare!(invalid)
  abort "unconfirmed runtime scenario was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("runtime")
end
puts "Dora capability acceptance test passed (confirmed obligations remain unresolved until independent evidence exists)."
