# frozen_string_literal: true

require_relative "idea_interview_session"
require_relative "decision_log"
require_relative "vertical_slice_readiness"

module Dora
  class AuthoringTrace
    def self.build!(session_path:, proposal:, decision_log_path: nil)
      session = IdeaInterviewSession.load!(session_path)
      readiness = VerticalSliceReadiness.evaluate!(proposal)
      decisions = decision_log_path ? DecisionLog.load!(decision_log_path).fetch("entries") : []
      {"kind" => "dora_authoring_trace", "version" => 1, "confirmed_interview_inputs" => session.fetch("answers").map { |answer| answer.slice("id", "source") }, "open_decisions" => session.fetch("open_decisions").map { |decision| decision.slice("id", "question") }, "proposal" => {"capability_id" => proposal.dig("capability", "id"), "citation" => "declared proposal input"}, "readiness_gaps" => readiness.fetch("blocking_gaps"), "decision_records" => decisions.map { |entry| entry.slice("id", "status") }, "citations" => [session_path, decision_log_path].compact, "completion_boundary" => "An authoring trace cites declared inputs and gaps only; it does not rewrite product meaning, approve implementation, or prove release readiness."}.freeze
    end
  end
end
