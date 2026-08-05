#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/create_app"

def answer(id, value); {"id" => id, "value" => value, "source" => "user"}; end
source = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "../reviewed-dora", "ref" => "a" * 40, "checksum" => "b" * 64}, "review" => {"id" => "review-1", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}
interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => "garden-journal", "answers" => [answer("target_users", ["member"]), answer("first_problem", "Notes are lost."), answer("first_capability", "Record note."), answer("domain_concepts", ["note"]), answer("permission_intent", "Member records own group note."), answer("workflow_intent", "Draft becomes recorded."), answer("forbidden_outcomes", ["No public notes."])], "unanswered_decisions" => []}
bundle = {"kind" => "dora_create_app", "version" => 1, "interview" => interview, "dora_source" => source, "first_capability" => {"id" => "record-note", "title" => "Record a note", "interview_answer" => "first_capability"}, "first_work" => {"id" => "document-note", "title" => "Document note", "observable_outcome" => "A note boundary is declared.", "required_paths" => ["docs/note.md"], "validation" => "ruby test/note_test.rb", "evidence_boundary" => ["declared work fixture"]}, "starter" => "spring-vue-buildable", "codex_integration" => true}
result = Dora::CreateApp.validate!(bundle)
abort "bundle lost capability provenance" unless result.dig("first_capability", "provenance", "source") == "user"
abort "bundle inferred completion" unless result.fetch("invention") == "none"
invalid = Marshal.load(Marshal.dump(bundle)); invalid["first_capability"]["interview_answer"] = "missing"
begin; Dora::CreateApp.validate!(invalid); abort "bundle accepted uncited capability"; rescue ArgumentError => error; abort error.message unless error.message.include?("cite"); end
puts "Dora create-app contract test passed (explicit bundle preserves interview provenance and opt-ins)."
