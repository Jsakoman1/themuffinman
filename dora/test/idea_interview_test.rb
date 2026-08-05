#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/idea_interview"

def answer(id, value)
  {"id" => id, "value" => value, "source" => "user"}
end

interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => "garden-journal", "answers" => [answer("target_users", ["garden member"]), answer("first_problem", "Members lose planting decisions."), answer("first_capability", "Record a planting note."), answer("domain_concepts", ["garden", "planting-note"]), answer("permission_intent", "Members record notes in their garden."), answer("workflow_intent", "A note moves from draft to recorded."), answer("forbidden_outcomes", ["Do not publish notes publicly."])], "unanswered_decisions" => [{"id" => "archive-policy", "question" => "Who may archive a note?", "source" => "user"}]}
result = Dora::IdeaInterview.validate!(interview)
abort "interview lost user provenance" unless result.fetch("answers").all? { |answer| answer.fetch("source") == "user" }
abort "interview changed unresolved decision" unless result.fetch("unanswered_decisions").first.fetch("id") == "archive-policy"
abort "interview made an invention claim" unless result.fetch("invention") == "none"

invalid = Marshal.load(Marshal.dump(interview)); invalid["answers"].pop
begin
  Dora::IdeaInterview.validate!(invalid)
  abort "interview accepted missing required answer"
rescue ArgumentError => error
  abort "wrong interview failure: #{error.message}" unless error.message.include?("missing required answers")
end

puts "Dora idea interview test passed (user provenance and unanswered decisions remain explicit)."
