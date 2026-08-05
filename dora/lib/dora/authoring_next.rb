# frozen_string_literal: true

require "yaml"
require_relative "idea_interview_session"
require_relative "starter_compatibility"

module Dora
  class AuthoringNext
    def self.report!(session_path:, readiness_path: nil)
      session = IdeaInterviewSession.load!(session_path)
      question = IdeaInterviewSession.next_question!(session)
      return response("answer_interview_question", "Record a user-confirmed answer for #{question.fetch("id")}: #{question.fetch("question")}", [session_path]) if question
      decision = session.fetch("open_decisions").first
      return response("resolve_open_decision", "Ask the user to resolve open decision #{decision.fetch("id")}: #{decision.fetch("question")}", [session_path]) if decision
      if readiness_path
        readiness = StarterCompatibility.readiness!(readiness_path)
        gap = readiness.fetch("blocking_gaps").first
        return response("resolve_technical_prerequisite", gap, [session_path, readiness_path]) if gap
      end
      response("prepare_confirmed_capability_context", "Convert the completed confirmed interview into a declared capability context before requesting a proposal.", [session_path])
    end

    def self.response(action, reason, citations)
      {"kind" => "dora_authoring_next", "version" => 1, "recommended_next_action" => {"id" => action, "reason" => reason}, "citations" => citations, "completion_boundary" => "This selects one declared next authoring action only; it does not infer product rules, create source code, or start implementation."}.freeze
    end
    private_class_method :response
  end
end
