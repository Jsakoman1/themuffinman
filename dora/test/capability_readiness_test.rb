#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/capability_readiness"

def capability(test_status: "recorded", runtime_status: "recorded", unresolved: [])
  {"kind" => "dora_capability_package", "version" => 1, "id" => "record-note", "title" => "Record note", "intent" => {"problem" => "Notes are lost.", "capability" => "Record note.", "source_reference" => "docs/idea-interview.yaml#first-capability"}, "domain" => {"entity_ids" => ["note"], "invariant_ids" => ["note-owner"], "permission_rule_ids" => ["record-own-note"], "workflow_id" => "note-lifecycle"}, "api" => {"service_owner" => "notes", "operations" => [{"id" => "record-note", "purpose" => "Record one note."}]}, "tests" => {"scenarios" => [{"id" => "record-note-test", "action" => "Submit note.", "expected" => "Note is recorded.", "status" => test_status}]}, "runtime" => {"scenarios" => [{"id" => "record-note-runtime", "action" => "Record note in runtime.", "expected" => "Note is visible.", "status" => runtime_status}]}, "work" => {"plan" => "docs/work/record-note.yaml", "task" => "implement-record-note"}, "unresolved" => unresolved}
end

domain = {"entities" => [{"id" => "note"}], "invariants" => [{"id" => "note-owner"}], "permission_rules" => [{"id" => "record-own-note"}], "workflows" => [{"id" => "note-lifecycle"}]}
ready = Dora::CapabilityReadiness.report!(capability: capability, domain: domain)
abort "readiness rejected coherent capability" unless ready.fetch("ready_to_implement") && ready.fetch("blocking_gaps").empty?
abort "readiness did not return declared work action" unless ready.fetch("recommended_next_action").include?("implement-record-note")
unresolved = [{"id" => "record-note-test", "reason" => "Test not run."}, {"id" => "record-note-runtime", "reason" => "Runtime not run."}]
blocked = Dora::CapabilityReadiness.report!(capability: capability(test_status: "declared", runtime_status: "unresolved", unresolved: unresolved), domain: {"entities" => [{"id" => "note"}], "invariants" => [{"id" => "note-owner"}], "permission_rules" => [], "workflows" => [{"id" => "note-lifecycle"}]})
abort "readiness accepted a missing permission rule" if blocked.fetch("ready_to_implement")
abort "readiness omitted exact permission blocker" unless blocked.fetch("blocking_gaps").any? { |gap| gap.include?("permission rule record-own-note") }
abort "readiness made a completion claim" unless blocked.fetch("completion_boundary").include?("not a completion")
puts "Dora capability readiness test passed (ready and blocked capability states retain exact gaps and one declared next action)."
