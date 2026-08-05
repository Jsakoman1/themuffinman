#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/domain_compiler"

def package(test_status: "recorded", runtime_status: "recorded", unresolved: [])
  {"kind" => "dora_capability_package", "version" => 1, "id" => "record-note", "title" => "Record note", "intent" => {"problem" => "Notes are lost.", "capability" => "Record note.", "source_reference" => "docs/idea-interview.yaml#first-capability"}, "domain" => {"entity_ids" => ["note"], "invariant_ids" => ["note-owner"], "permission_rule_ids" => ["record-own-note"], "workflow_id" => "note-lifecycle"}, "api" => {"service_owner" => "notes", "operations" => [{"id" => "record-note", "purpose" => "Record one note."}]}, "tests" => {"scenarios" => [{"id" => "record-note-test", "action" => "Submit note.", "expected" => "Note is recorded.", "status" => test_status}]}, "runtime" => {"scenarios" => [{"id" => "record-note-runtime", "action" => "Record note in runtime.", "expected" => "Note is visible.", "status" => runtime_status}]}, "work" => {"plan" => "docs/work/record-note.yaml", "task" => "implement-record-note"}, "unresolved" => unresolved}
end

domain = {"entities" => [{"id" => "note"}], "invariants" => [{"id" => "note-owner"}], "permission_rules" => [{"id" => "record-own-note"}], "workflows" => [{"id" => "note-lifecycle"}]}
coherent = Dora::DomainCompiler.compile!(capability: package, domain: domain)
abort "compiler rejected coherent declared domain" unless coherent.fetch("consistent") && coherent.fetch("findings").empty?
unresolved = [{"id" => "record-note-test", "reason" => "Test not run."}, {"id" => "record-note-runtime", "reason" => "Runtime not run."}]
contradictory = Dora::DomainCompiler.compile!(capability: package(test_status: "declared", runtime_status: "unresolved", unresolved: unresolved), domain: {"entities" => [{"id" => "note"}], "invariants" => [], "permission_rules" => [], "workflows" => []})
ids = contradictory.fetch("findings").map { |finding| finding.fetch("id") }
%w[missing-invariant-note-owner missing-permission-rule-record-own-note missing-workflow-note-lifecycle missing-test-evidence-record-note-test missing-runtime-evidence-record-note-runtime].each { |id| abort "compiler omitted #{id}" unless ids.include?(id) }
abort "compiler inferred a repair" unless contradictory.fetch("recommended_next_action").include?("Resolve") && contradictory.fetch("invention") == "none"
puts "Dora domain compiler test passed (declared domain contradictions and missing evidence are reported without inferred repair)."
