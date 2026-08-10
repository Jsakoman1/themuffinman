# frozen_string_literal: true

require_relative "discovery_provenance"

module Dora
  # Formats owner-mediated research questions and pasted responses. It deliberately
  # has no transport, credential, remote client, or persistence behavior.
  class DiscoveryResearchPacket
    DISCLOSURE_LEVELS = %w[public_summary_redacted].freeze
    CLAIM_CLASSES = %w[facts inferences speculation].freeze

    def self.render!(framing:, consent:, questions:, observed_at: Time.now.utc.iso8601)
      validated_framing = validate_framing!(framing)
      validated_consent = validate_consent!(consent)
      validated_questions = validate_questions!(questions)
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_research_packet",
        source_references: framing.fetch("source_references"),
        observed_at: observed_at,
        payload: {
          "consent" => validated_consent,
          "questions" => validated_questions,
          "copyable_prompt" => copyable_prompt(validated_framing, validated_consent, validated_questions),
          "completion_boundary" => "This is an owner-mediated copy/paste packet only; it does not contact an external service, persist a transcript, accept research as a decision, or authorize implementation."
        }
      )
    end

    def self.validate_pasted_response!(packet:, response:, observed_at: Time.now.utc.iso8601)
      validate_packet!(packet)
      fail!("research response is invalid") unless response.is_a?(Hash) && response.keys.sort == %w[facts inferences source_references speculation]
      claims = CLAIM_CLASSES.to_h { |key| [key, validate_claims!(response.fetch(key), key)] }
      references = validate_references!(response.fetch("source_references"))
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_research_response",
        source_references: (packet.fetch("source_references") + references).uniq.sort,
        observed_at: observed_at,
        payload: {
          "claims" => claims,
          "completion_boundary" => "A pasted research response remains advisory; it cannot create or amend an owner decision, change status, write code, invoke GitHub, mutate a consumer project, or start an agent."
        }
      )
    end

    def self.validate_framing!(framing)
      fail!("research packet framing is invalid") unless framing.is_a?(Hash) && framing["kind"] == "dora_discovery_owner_framing_gate" && framing["read_only"] == true && framing["disposition"] == "advisory" && framing.dig("payload", "sufficient_for_proposal") == true && framing.fetch("source_references").is_a?(Array)

      framing
    end
    private_class_method :validate_framing!

    def self.validate_consent!(consent)
      fail!("research packet consent is invalid") unless consent.is_a?(Hash) && consent.keys.sort == %w[allowed_sources disclosure_level source topic] && consent["source"] == "user_confirmed" && consent["topic"].is_a?(String) && !consent["topic"].strip.empty? && DISCLOSURE_LEVELS.include?(consent["disclosure_level"])

      {"topic" => consent.fetch("topic").dup.freeze, "disclosure_level" => consent.fetch("disclosure_level"), "allowed_sources" => validate_references!(consent.fetch("allowed_sources")), "source" => "user_confirmed"}.freeze
    end
    private_class_method :validate_consent!

    def self.validate_questions!(questions)
      fail!("research packet questions must be a non-empty list") unless questions.is_a?(Array) && !questions.empty?

      rows = questions.map do |question|
        fail!("research packet question is invalid") unless question.is_a?(Hash) && question.keys.sort == %w[id question] && identifier?(question["id"]) && statement?(question["question"])

        {"id" => question.fetch("id"), "question" => question.fetch("question").dup.freeze}.freeze
      end
      fail!("research packet question ids must be unique") unless rows.map { |row| row.fetch("id") }.uniq.length == rows.length

      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_questions!

    def self.validate_packet!(packet)
      fail!("research response packet is invalid") unless packet.is_a?(Hash) && packet["kind"] == "dora_discovery_research_packet" && packet["read_only"] == true && packet["disposition"] == "advisory" && packet.fetch("source_references").is_a?(Array)
    end
    private_class_method :validate_packet!

    def self.validate_claims!(claims, label)
      fail!("research response #{label} must be a list") unless claims.is_a?(Array)

      claims.map do |claim|
        fail!("research response claim is invalid") unless claim.is_a?(Hash) && claim.keys.sort == %w[source_references statement] && statement?(claim["statement"])

        {"statement" => claim.fetch("statement").dup.freeze, "source_references" => validate_references!(claim.fetch("source_references"))}.freeze
      end.freeze
    end
    private_class_method :validate_claims!

    def self.validate_references!(references)
      fail!("research packet references must be a non-empty list") unless references.is_a?(Array) && !references.empty?

      rows = references.map do |reference|
        fail!("research packet reference is invalid") unless reference.is_a?(String) && !reference.strip.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..")

        reference.dup.freeze
      end
      fail!("research packet references must be unique") unless rows.uniq.length == rows.length

      rows.sort.freeze
    end
    private_class_method :validate_references!

    def self.copyable_prompt(framing, consent, questions)
      [
        "Research topic: #{consent.fetch("topic")}",
        "Disclosure level: #{consent.fetch("disclosure_level")}",
        "Confirmed framing: #{framing.dig("payload", "confirmed_direction_answer_ids").join(", ")}",
        "Questions:",
        *questions.map { |question| "- #{question.fetch("question")}" },
        "Separate facts, inferences, and speculation. Include source references and observation dates. Do not make implementation decisions."
      ].join("\n").freeze
    end
    private_class_method :copyable_prompt

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
