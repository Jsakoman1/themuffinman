# frozen_string_literal: true

require_relative "discovery_assumption_ledger"
require_relative "idea_interview_session"

module Dora
  # Checks whether an owner has provided enough direction to receive a proposed
  # skeleton. It reports gaps only; owner confirmation and DecisionLog mutation
  # remain separate deliberate actions.
  class OwnerFramingGate
    REQUIRED_DIRECTION_ANSWER_IDS = %w[target_users first_problem first_capability forbidden_outcomes].freeze
    COLLABORATION_SCOPES = %w[individual shared organizational public].freeze

    def self.evaluate!(session:, ledger:, collaboration_scope: nil, observed_at: Time.now.utc.iso8601)
      validated_session = IdeaInterviewSession.validate!(session)
      validated_ledger = validate_ledger!(ledger)
      scope = validate_collaboration_scope!(collaboration_scope)
      answered = validated_session.fetch("answers").map { |answer| answer.fetch("id") }
      blockers = REQUIRED_DIRECTION_ANSWER_IDS.reject { |id| answered.include?(id) }.map do |id|
        {"id" => "confirm-#{id.tr("_", "-")}", "question" => IdeaInterviewSession::QUESTIONS.fetch(id)}
      end
      blockers << {"id" => "confirm-collaboration-scope", "question" => "Is the product individual, shared, organizational, or public?"} if scope.nil?
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_owner_framing_gate",
        source_references: source_references(validated_ledger),
        observed_at: observed_at,
        payload: {
          "sufficient_for_proposal" => blockers.empty?,
          "confirmed_direction_answer_ids" => REQUIRED_DIRECTION_ANSWER_IDS & answered,
          "collaboration_scope" => scope && scope.fetch("value"),
          "blocking_questions" => blockers,
          "completion_boundary" => "A sufficient framing result is not owner confirmation and cannot create a DecisionLog entry, authorize research, create-app, work, implementation, or verification."
        }
      )
    end

    def self.validate_ledger!(ledger)
      fail!("owner framing ledger is invalid") unless ledger.is_a?(Hash) && ledger["kind"] == "dora_discovery_assumption_ledger" && ledger["read_only"] == true && ledger["disposition"] == "advisory" && ledger["source_references"].is_a?(Array)

      ledger
    end
    private_class_method :validate_ledger!

    def self.validate_collaboration_scope!(scope)
      return nil if scope.nil?

      fail!("owner framing collaboration scope is invalid") unless scope.is_a?(Hash) && scope.keys.sort == %w[source value] && scope["source"] == "user_confirmed" && COLLABORATION_SCOPES.include?(scope["value"])

      scope.slice("value", "source").freeze
    end
    private_class_method :validate_collaboration_scope!

    def self.source_references(ledger)
      (ledger.fetch("source_references") + ["idea_interview_session#confirmed_answers"]).uniq.sort
    end
    private_class_method :source_references

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
