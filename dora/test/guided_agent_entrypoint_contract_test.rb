#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/idea_interview"
require_relative "../lib/dora/idea_interview_session"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "guided-agent-entrypoint.schema.yaml"))
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/guided-agent-entrypoint.yaml"))

abort "guided entrypoint schema kind is invalid" unless schema["kind"] == "dora_guided_agent_entrypoint_schema" && schema["version"] == 1
abort "guided entrypoint schema lacks resolution states" unless schema.fetch("resolution_states") == %w[ask_one_confirmed_question review_create_app_handoff]

%w[incomplete complete].each do |name|
  entrypoint = fixture.fetch(name)
  abort "fixture entrypoint kind is invalid" unless entrypoint["kind"] == "dora_guided_agent_entrypoint" && entrypoint["version"] == 1
  missing = schema.fetch("required_fields").reject { |field| entrypoint.key?(field) }
  abort "fixture entrypoint is missing #{missing.join(", ")}" unless missing.empty?
  handoff_missing = schema.fetch("handoff_required_fields").reject { |field| entrypoint.fetch("handoff").key?(field) }
  abort "fixture handoff is missing #{handoff_missing.join(", ")}" unless handoff_missing.empty?
end

incomplete = Dora::IdeaInterviewSession.validate!(fixture.dig("incomplete", "session"))
question = Dora::IdeaInterviewSession.next_question!(incomplete)
abort "incomplete session must resolve to one first question" unless question && question.fetch("id") == "target_users"

complete = Dora::IdeaInterviewSession.validate!(fixture.dig("complete", "session"))
abort "complete session must not request another required answer" unless Dora::IdeaInterviewSession.next_question!(complete).nil?
abort "explicit open decision was lost" unless complete.fetch("open_decisions").first.fetch("id") == "offline-mode"
abort "complete handoff must remain review-only" unless fixture.dig("complete", "completion_boundary").include?("review-only")

puts "Dora guided agent entrypoint contract test passed (one confirmed question or review-only handoff with explicit decisions preserved)."
