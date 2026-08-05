#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/idea_interview_session"

session = Dora::IdeaInterviewSession.new!(project_id: "home-stock")
question = Dora::IdeaInterviewSession.next_question!(session)
abort "session did not expose the first required question" unless question.fetch("id") == "target_users"

confirmed = session.merge(
  "answers" => [{"id" => "target_users", "value" => ["household member"], "source" => "user_confirmed"}],
  "open_decisions" => [{"id" => "retention", "question" => "How long should inventory history be retained?", "source" => "user_confirmed", "status" => "open"}]
)
validated = Dora::IdeaInterviewSession.validate!(confirmed)
abort "confirmed answer was not retained" unless validated.fetch("answers").first.fetch("source") == "user_confirmed"
abort "open decision was not retained" unless validated.fetch("open_decisions").first.fetch("status") == "open"
abort "session did not advance by one question" unless Dora::IdeaInterviewSession.next_question!(validated).fetch("id") == "first_problem"

inferred = Marshal.load(Marshal.dump(confirmed))
inferred.fetch("answers").first["source"] = "agent"
begin
  Dora::IdeaInterviewSession.validate!(inferred)
  abort "inferred answer was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("user_confirmed")
end

duplicate = Marshal.load(Marshal.dump(confirmed))
duplicate.fetch("answers") << duplicate.fetch("answers").first.dup
begin
  Dora::IdeaInterviewSession.validate!(duplicate)
  abort "duplicate answer was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("unique")
end

puts "Dora idea interview session test passed (one next question, confirmed answers, and explicit open decisions remain separate from inference)."
