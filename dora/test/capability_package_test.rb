#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/capability_package"

package = {"kind" => "dora_capability_package", "version" => 1, "id" => "record-note", "title" => "Record note", "intent" => {"problem" => "Notes are lost.", "capability" => "Record note.", "source_reference" => "docs/idea-interview.yaml#first-capability"}, "domain" => {"entity_ids" => ["note"], "invariant_ids" => ["note-owner"], "permission_rule_ids" => ["record-own-note"], "workflow_id" => "note-lifecycle"}, "api" => {"service_owner" => "notes", "operations" => [{"id" => "record-note", "purpose" => "Record one note."}]}, "tests" => {"scenarios" => [{"id" => "record-note-test", "action" => "Submit a valid note.", "expected" => "The note is recorded.", "status" => "declared"}]}, "runtime" => {"scenarios" => [{"id" => "record-note-runtime", "action" => "Record a note in a local runtime.", "expected" => "The recorded note is visible.", "status" => "unresolved"}]}, "work" => {"plan" => "docs/work/record-note.yaml", "task" => "implement-record-note"}, "unresolved" => [{"id" => "record-note-test", "reason" => "Test has not run."}, {"id" => "record-note-runtime", "reason" => "Runtime scenario has not run."}]}

result = Dora::CapabilityPackage.validate!(package)
abort "capability package lost the atomic work link" unless result.dig("work", "task") == "implement-record-note"
abort "capability package inferred evidence" unless result.dig("runtime", "scenarios", 0, "status") == "unresolved"
invalid = Marshal.load(Marshal.dump(package)); invalid["domain"]["permission_rule_ids"] = []
begin
  Dora::CapabilityPackage.validate!(invalid)
  abort "capability package accepted missing permission linkage"
rescue ArgumentError => error
  abort error.message unless error.message.include?("permission_rule_ids")
end
invalid = Marshal.load(Marshal.dump(package)); invalid["tests"]["scenarios"][0]["status"] = "recorded"; invalid["runtime"]["scenarios"][0]["status"] = "declared"; invalid["unresolved"] = []
begin
  Dora::CapabilityPackage.validate!(invalid)
  abort "capability package accepted untracked unresolved evidence"
rescue ArgumentError => error
  abort error.message unless error.message.include?("must be unresolved")
end
puts "Dora capability package test passed (intent, domain, API, evidence, and atomic work remain explicitly linked)."
