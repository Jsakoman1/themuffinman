#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/discovery_assumption_ledger"

statements = [
  {"id" => "shared-membership", "classification" => "confirmed", "statement" => "Household members share one stock view.", "source_references" => ["idea-interview.yaml#target_users"]},
  {"id" => "offline-mode", "classification" => "open_question", "statement" => "Offline behavior is undecided.", "source_references" => ["idea-interview.yaml#open-decisions"]},
  {"id" => "market-pattern", "classification" => "external_research", "statement" => "Comparable products separate routine stock from administration.", "source_references" => ["research/market.md#observed-pattern"]},
  {"id" => "delivery-order", "classification" => "assumption", "statement" => "The first delivery should prioritize routine stock updates.", "source_references" => ["idea-interview.yaml#first-capability"]},
  {"id" => "export-later", "classification" => "deferred_without_commitment", "statement" => "Export is not selected for the first delivery.", "source_references" => ["idea-interview.yaml#forbidden-outcomes"]}
]
before = Marshal.dump(statements)
result = Dora::DiscoveryAssumptionLedger.build!(statements: statements, observed_at: "2026-08-10T12:40:00Z")

abort "ledger is not advisory provenance" unless result.slice("kind", "read_only", "disposition", "observed_at") == {"kind" => "dora_discovery_assumption_ledger", "read_only" => true, "disposition" => "advisory", "observed_at" => "2026-08-10T12:40:00Z"}
rows = result.dig("payload", "statements")
abort "ledger lost classifications" unless rows.map { |row| row.fetch("classification") }.sort == Dora::DiscoveryAssumptionLedger::CLASSIFICATIONS.sort
abort "ledger did not sort rows deterministically" unless rows.map { |row| row.fetch("id") } == rows.map { |row| row.fetch("id") }.sort
abort "ledger mutated its input" unless Marshal.dump(statements) == before
abort "ledger boundary lacks non-authority wording" unless result.dig("payload", "completion_boundary").include?("does not accept a decision")
abort "ledger must not expose lifecycle state" if result.key?("status") || result.dig("payload", "status")

invalid = statements.first.merge("classification" => "accepted")
begin
  Dora::DiscoveryAssumptionLedger.build!(statements: [invalid])
  abort "ledger accepted an authority-like classification"
rescue ArgumentError
  # Expected.
end

invalid_reference = statements.first.merge("source_references" => ["../private.yaml"])
begin
  Dora::DiscoveryAssumptionLedger.build!(statements: [invalid_reference])
  abort "ledger accepted an unsafe source reference"
rescue ArgumentError
  # Expected.
end

source = File.read(File.expand_path("../lib/dora/discovery_assumption_ledger.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open DecisionLog WorkExecution HandoffRunner].each do |forbidden_surface|
  abort "ledger exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end

puts "Dora discovery assumption ledger test passed (sourced advisory classifications and no durable authority)."
