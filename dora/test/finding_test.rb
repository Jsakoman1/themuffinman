#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/finding"

finding = Dora::Finding.build!(id: "missing-controller-test", severity: "warning", location: {"path" => "src/controller.rb", "line" => 12}, explanation: "The declared controller has no matching test.", repair: "Add a focused controller test.", evidence: ["static source inspection"])
abort "finding omitted portable location" unless finding.dig("location", "path") == "src/controller.rb"
abort "finding omitted repair guidance" unless finding.fetch("repair") == "Add a focused controller test."
abort "finding crossed its diagnostic boundary" unless finding.fetch("diagnostic_boundary").include?("does not prove")
begin
  Dora::Finding.build!(id: "bad", severity: "critical", location: {"path" => "src/item.rb"}, explanation: "x", repair: "y", evidence: ["z"])
  abort "finding accepted an unsupported severity"
rescue ArgumentError
end

puts "Dora finding test passed (portable severity, location, explanation, repair, and evidence envelope)."
