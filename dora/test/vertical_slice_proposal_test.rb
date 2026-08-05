#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/vertical_slice_proposal"

proposal = {
  "kind" => "dora_vertical_slice_proposal", "version" => 1,
  "capability" => {"id" => "record-supply", "title" => "Record a supply", "confirmed" => true},
  "surfaces" => {
    "migration" => ["backend/src/main/resources/db/migration/V__record_supply.sql"],
    "backend" => ["backend/src/main/java/<product-package>/supply/"],
    "api" => ["backend/src/main/java/<product-package>/supply/controller/"],
    "frontend" => ["frontend/src/features/record-supply/"],
    "tests" => ["backend/src/test/java/<product-package>/supply/"],
    "runtime_evidence" => ["docs/runtime-evidence/record-supply.json"],
    "documentation" => ["docs/capabilities/record-supply.md"]
  },
  "atomic_work" => {"plan" => "docs/work/record-supply.yaml", "task" => {"id" => "implement-record-supply", "title" => "Implement record supply", "observable_outcome" => "The confirmed capability has one bounded path to review.", "required_paths" => ["docs/capabilities/record-supply.md"], "validation" => "ruby test/record_supply_test.rb", "evidence_boundary" => ["declared capability fixture"]}},
  "gaps" => []
}

result = Dora::VerticalSliceProposal.validate!(proposal)
abort "proposal lost confirmation" unless result.dig("capability", "confirmed") == true
abort "proposal lacks completion boundary" unless result.fetch("completion_boundary").include?("unproven")

incomplete = Marshal.load(Marshal.dump(proposal)); incomplete["surfaces"].delete("runtime_evidence")
begin; Dora::VerticalSliceProposal.validate!(incomplete); abort "proposal accepted missing runtime evidence"; rescue ArgumentError => error; abort error.message unless error.message.include?("surfaces"); end
source_bearing = Marshal.load(Marshal.dump(proposal)); source_bearing["source_code"] = "class Supply {}"
begin; Dora::VerticalSliceProposal.validate!(source_bearing); abort "proposal accepted source code"; rescue ArgumentError => error; abort error.message unless error.message.include?("source"); end

puts "Dora vertical-slice proposal test passed (one confirmed capability links all review surfaces without generated implementation)."
