#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/idea_interview_session"
require "yaml"

def answer(id, value)
  {"id" => id, "value" => value, "source" => "user_confirmed"}
end

shared = Dora::IdeaInterviewSession.new!(project_id: "home-stock")
before = Marshal.dump(shared)
projection = Dora::IdeaInterviewSession.direction_projection!(shared)
abort "direction output is not advisory provenance" unless projection.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_direction", "read_only" => true, "disposition" => "advisory"}
abort "direction core is wrong" unless projection.dig("payload", "core_answer_ids") == %w[target_users first_problem first_capability forbidden_outcomes]
abort "direction did not start with target users" unless projection.dig("payload", "next_question", "id") == "target_users"
abort "direction projection mutated the session" unless Marshal.dump(shared) == before

shared = shared.merge("answers" => [
  answer("target_users", "A household member"),
  answer("first_problem", "People need to track supplies"),
  answer("first_capability", "Record food stock"),
  answer("forbidden_outcomes", "Do not auto-order goods")
])
projection = Dora::IdeaInterviewSession.direction_projection!(shared)
abort "shared direction did not request permission framing" unless projection.dig("payload", "triggered_conditional_answer_ids") == %w[permission_intent workflow_intent]
abort "shared direction did not ask the first relevant conditional question" unless projection.dig("payload", "next_question", "id") == "permission_intent"

individual = Dora::IdeaInterviewSession.new!(project_id: "note-pad").merge("answers" => [
  answer("target_users", "One person"),
  answer("first_problem", "A note is lost"),
  answer("first_capability", "Save note"),
  answer("forbidden_outcomes", "Do not share notes")
])
projection = Dora::IdeaInterviewSession.direction_projection!(individual)
abort "individual direction invented a conditional question" unless projection.dig("payload", "triggered_conditional_answer_ids") == []
abort "individual direction should be complete" unless projection.dig("payload", "complete") && projection.dig("payload", "next_question").nil?

source = File.read(File.expand_path("../lib/dora/idea_interview_session.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open DecisionLog WorkExecution HandoffRunner].each do |forbidden_surface|
  abort "adaptive direction interview exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end
abort "direction result exposed a status field" if projection.key?("status") || projection.dig("payload", "status")
abort "direction boundary did not forbid delivery authority" unless projection.dig("payload", "completion_boundary").include?("does not create a product decision")
schema_boundary = YAML.load_file(File.expand_path("../idea-interview-session.schema.yaml", __dir__)).fetch("direction_completion_boundary")
abort "direction boundary did not come from the validated schema" unless projection.dig("payload", "completion_boundary") == schema_boundary

puts "Dora adaptive direction interview test passed (short core, conditional questions, provenance, and no authority mutation)."
