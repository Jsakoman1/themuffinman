#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/create_app"

def answer(id, value); {"id" => id, "value" => value, "source" => "user"}; end
source = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "../reviewed-dora", "ref" => "a" * 40, "checksum" => "b" * 64}, "review" => {"id" => "review", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}
interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => "starter-test", "answers" => [answer("target_users", ["member"]), answer("first_problem", "Notes are lost."), answer("first_capability", "Record note."), answer("domain_concepts", ["note"]), answer("permission_intent", "Member records note."), answer("workflow_intent", "Draft becomes recorded."), answer("forbidden_outcomes", ["No public notes."])], "unanswered_decisions" => []}
bundle = {"kind" => "dora_create_app", "version" => 1, "interview" => interview, "dora_source" => source, "first_capability" => {"id" => "record-note", "title" => "Record note", "interview_answer" => "first_capability"}, "first_work" => {"id" => "document-note", "title" => "Document note", "observable_outcome" => "A note boundary is declared.", "required_paths" => ["docs/note.md"], "validation" => "ruby test/note_test.rb", "evidence_boundary" => ["fixture"]}, "starter" => "blank"}
result = Dora::CreateApp.validate!(bundle)
abort "starter selection omitted declared pack" unless result.dig("starter_selection", "starter_pack") == "starters/blank.yaml"
invalid = Marshal.load(Marshal.dump(bundle)); invalid["starter"] = "unknown"
begin; Dora::CreateApp.validate!(invalid); abort "unknown starter accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("unknown neutral stack"); end
puts "Dora create-app starter selection test passed (one declared neutral starter is selected and unknown choices are rejected)."
