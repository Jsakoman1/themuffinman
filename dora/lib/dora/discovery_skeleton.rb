# frozen_string_literal: true

require_relative "idea_interview_session"
require_relative "owner_framing_gate"

module Dora
  # Projects a compact target outline from confirmed facts. It cannot add product
  # rules: each area is supplied by the owner-facing framing flow with citations.
  class DiscoverySkeleton
    def self.build!(session:, framing:, product_areas:, observed_at: Time.now.utc.iso8601)
      validated_session = IdeaInterviewSession.validate!(session)
      validated_framing = validate_framing!(framing)
      areas = validate_areas!(product_areas)
      answers = validated_session.fetch("answers").to_h { |answer| [answer.fetch("id"), answer.fetch("value")] }
      required = OwnerFramingGate::REQUIRED_DIRECTION_ANSWER_IDS
      fail!("discovery skeleton framing is missing confirmed direction answers") unless required.all? { |id| answers.key?(id) }
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_skeleton",
        source_references: (validated_framing.fetch("source_references") + areas.flat_map { |area| area.fetch("source_references") } + ["idea_interview_session#confirmed_answers"]).uniq.sort,
        observed_at: observed_at,
        payload: {
          "primary_users" => answers.fetch("target_users"),
          "problem" => answers.fetch("first_problem"),
          "first_delivery" => {"capability" => answers.fetch("first_capability"), "intentional_exclusions" => answers.fetch("forbidden_outcomes")},
          "product_areas" => areas,
          "foundation_choice_coverage" => [{"id" => "collaboration-scope", "state" => "confirmed", "value" => validated_framing.dig("payload", "collaboration_scope"), "source_references" => validated_framing.fetch("source_references")}],
          "unresolved_questions" => validated_session.fetch("open_decisions").map { |decision| decision.slice("id", "question") },
          "completion_boundary" => "This proposed skeleton is advisory and regenerable; it does not write ProductBrief, DomainLibrary, DecisionLog, a backlog, a work plan, a project, or product code."
        }
      )
    end

    def self.validate_framing!(framing)
      fail!("discovery skeleton framing is invalid") unless framing.is_a?(Hash) && framing["kind"] == "dora_discovery_owner_framing_gate" && framing["read_only"] == true && framing["disposition"] == "advisory" && framing.dig("payload", "sufficient_for_proposal") == true && OwnerFramingGate::COLLABORATION_SCOPES.include?(framing.dig("payload", "collaboration_scope")) && framing.fetch("source_references").is_a?(Array)

      framing
    end
    private_class_method :validate_framing!

    def self.validate_areas!(areas)
      fail!("discovery skeleton product areas must be a non-empty list") unless areas.is_a?(Array) && !areas.empty?

      rows = areas.map do |area|
        fail!("discovery skeleton product area is invalid") unless area.is_a?(Hash) && area.keys.sort == %w[id source_references title] && identifier?(area["id"]) && statement?(area["title"])

        {"id" => area.fetch("id"), "title" => area.fetch("title").dup.freeze, "source_references" => validate_references!(area.fetch("source_references"))}.freeze
      end
      fail!("discovery skeleton product area ids must be unique") unless rows.map { |row| row.fetch("id") }.uniq.length == rows.length

      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_areas!

    def self.validate_references!(references)
      fail!("discovery skeleton references must be a non-empty list") unless references.is_a?(Array) && !references.empty?
      rows = references.map do |reference|
        fail!("discovery skeleton reference is invalid") unless reference.is_a?(String) && !reference.strip.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..")

        reference.dup.freeze
      end
      fail!("discovery skeleton references must be unique") unless rows.uniq.length == rows.length

      rows.sort.freeze
    end
    private_class_method :validate_references!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(%r{\A[a-z][a-z0-9-]*\z})
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty? && !value.match?(/[\r\n]/)
    end
    private_class_method :statement?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
