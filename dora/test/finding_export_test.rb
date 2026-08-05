#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/finding"
require_relative "../lib/dora/finding_export"

finding = Dora::Finding.build!(id: "missing-test", severity: "warning", location: {"path" => "src/item.rb", "line" => 8}, explanation: "The source has no declared test.", repair: "Add a focused test.", evidence: ["static inspection"])
export = Dora::FindingExport.export!([finding])
snapshot = {"read_only" => export.fetch("read_only"), "annotation" => export.fetch("annotations").first}
expected = {"read_only" => true, "annotation" => {"level" => "warning", "location" => {"path" => "src/item.rb", "line" => 8}, "message" => "The source has no declared test.", "repair" => "Add a focused test.", "properties" => {"id" => "missing-test", "evidence" => ["static inspection"]}}}
abort "finding export snapshot changed" unless snapshot == expected
abort "finding export is not diagnostic only" unless export.fetch("diagnostic_boundary").include?("do not prove")

puts "Dora finding export test passed (read-only portable annotation snapshot)."
