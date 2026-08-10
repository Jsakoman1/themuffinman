# frozen_string_literal: true

require "time"

module Dora
  # Evaluates one owner-authored proposal without making it Dora state. The caller
  # supplies only a sanitized canonical-state snapshot; the result intentionally
  # never echoes proposal prose or canonical decision content.
  class IntentPlanAlignment
    IDENTIFIER = /\A[a-z][a-z0-9-]{0,63}\z/.freeze
    READBACK_FIELDS = %w[phase alignment_result first_safe_next_action actionable_blocker_or_decision].freeze
    FIRST_GATES = %w[no_owner_decision_pending].freeze
    LATER_GATES = %w[no_owner_decision_pending prior_slice_verification].freeze
    MAX_TEXT = 500
    MAX_LIST = 12

    def self.evaluate(proposal:, canonical_state:)
      new(canonical_state).evaluate(proposal)
    end

    def self.option_packet!(alignment:, owner_question:)
      fail!("option packet owner question is invalid") unless safe_text?(owner_question)
      fail!("option packet alignment is invalid") unless alignment.is_a?(Hash) && alignment["phase"] == "ALIGNMENT" && %w[ACCEPTED RECONCILED OWNER_DECISION_NEEDED].include?(alignment["alignment_result"])

      facts = alignment.slice("phase", "alignment_result", "first_safe_next_action", "first_eligible_slice", "later_slices", "actionable_blocker_or_decision")
      {"kind" => "dora_owner_option_packet", "version" => 1, "observed_at" => Time.now.utc.iso8601, "source_references" => ["intent_plan_alignment"], "read_only" => true, "non_canonical" => true, "alignment_facts" => facts, "owner_question" => owner_question, "copyable_response_template" => {"required_sections" => %w[alternatives benefits risks assumptions recommendation], "decision_instruction" => "An owner must explicitly record any selected decision through the existing DecisionLog workflow."}, "authority_boundary" => "This packet does not contact ChatGPT, persist a transcript, accept an inbound response, create a decision, change work status, invoke GitHub, or start a remote agent."}.freeze
    end

    def self.safe_text?(value)
      value.is_a?(String) && !value.strip.empty? && value.length <= MAX_TEXT && !value.match?(/[\r\n]/)
    end
    private_class_method :safe_text?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!

    def initialize(canonical_state)
      @canonical_state = canonical_state.is_a?(Hash) ? canonical_state : {}
    end

    def evaluate(proposal)
      normalized = normalize(proposal)
      return owner_decision_needed("Resolve the incomplete Intent Plan proposal before creating Dora work.", "The Intent Plan proposal is incomplete or invalid.") unless normalized
      return owner_decision_needed("Resolve Dora project health before proposing a slice.", "Dora canonical state is not ready for alignment.") if @canonical_state["state"] == "INVALID"
      return owner_decision_needed("Finish or explicitly reconcile the active Dora delivery before proposing a new first slice.", "A Dora delivery is already active.") if @canonical_state["active_delivery"]
      return owner_decision_needed("Resolve the declared owner decision before releasing a slice.", "A Dora owner decision is pending.") if Array(@canonical_state["open_decisions"]).any?
      return owner_decision_needed("Resolve the proposal conflict with Dora's accepted decisions.", "The Intent Plan conflicts with a canonical owner decision.") unless fixed_decisions_match?(normalized.fetch("fixed_owner_decisions"))

      alignment_result = @canonical_state["latest_verified_delivery"] ? "RECONCILED" : "ACCEPTED"
      first = normalized.fetch("candidate_slices").first
      {
        "phase" => "ALIGNMENT",
        "alignment_result" => alignment_result,
        "first_safe_next_action" => "Create one Dora Master Plan for the eligible first slice.",
        "first_eligible_slice" => {"id" => first.fetch("id"), "status" => "ELIGIBLE"},
        "later_slices" => normalized.fetch("candidate_slices").drop(1).map { |slice| {"id" => slice.fetch("id"), "status" => "BLOCKED_PENDING_DORA_VERIFICATION"} },
        "actionable_blocker_or_decision" => nil
      }.freeze
    end

    private

    def normalize(proposal)
      return nil unless proposal.is_a?(Hash) && proposal.keys.sort == %w[candidate_slices fixed_owner_decisions in_scope_work intended_outcome intent_plan_id non_goals required_owner_readback]
      return nil unless identifier?(proposal["intent_plan_id"]) && text?(proposal["intended_outcome"])
      return nil unless text_list?(proposal["in_scope_work"]) && text_list?(proposal["non_goals"])
      return nil unless Array(proposal["required_owner_readback"]).sort == READBACK_FIELDS.sort && Array(proposal["required_owner_readback"]).uniq.length == READBACK_FIELDS.length

      decisions = normalize_decisions(proposal["fixed_owner_decisions"])
      slices = normalize_slices(proposal["candidate_slices"])
      return nil unless decisions && slices

      {"fixed_owner_decisions" => decisions, "candidate_slices" => slices}.freeze
    end

    def normalize_decisions(decisions)
      return nil unless decisions.is_a?(Array) && decisions.length <= MAX_LIST

      normalized = decisions.map do |decision|
        return nil unless decision.is_a?(Hash) && decision.keys.sort == %w[id statement] && identifier?(decision["id"]) && text?(decision["statement"])

        decision.slice("id", "statement")
      end
      normalized.map { |decision| decision.fetch("id") }.uniq.length == normalized.length ? normalized.freeze : nil
    end

    def normalize_slices(slices)
      return nil unless slices.is_a?(Array) && slices.length.between?(1, MAX_LIST)

      normalized = slices.each_with_index.map do |slice, index|
        return nil unless slice.is_a?(Hash) && slice.keys.sort == %w[depends_on gates id outcome]
        return nil unless identifier?(slice["id"]) && text?(slice["outcome"])
        expected_dependencies = index.zero? ? [] : [slices[index - 1]["id"]]
        expected_gates = index.zero? ? FIRST_GATES : LATER_GATES
        return nil unless slice["depends_on"] == expected_dependencies && Array(slice["gates"]).sort == expected_gates.sort && Array(slice["gates"]).uniq.length == expected_gates.length

        slice.slice("id", "depends_on", "gates")
      end
      normalized.map { |slice| slice.fetch("id") }.uniq.length == normalized.length ? normalized.freeze : nil
    end

    def fixed_decisions_match?(decisions)
      canonical = Array(@canonical_state["accepted_decisions"]).each_with_object({}) do |decision, entries|
        entries[decision["id"]] = decision["statement"] if decision.is_a?(Hash) && identifier?(decision["id"]) && text?(decision["statement"])
      end
      decisions.all? { |decision| canonical[decision.fetch("id")] == decision.fetch("statement") }
    end

    def owner_decision_needed(next_action, blocker)
      {
        "phase" => "ALIGNMENT",
        "alignment_result" => "OWNER_DECISION_NEEDED",
        "first_safe_next_action" => next_action,
        "first_eligible_slice" => nil,
        "later_slices" => [],
        "actionable_blocker_or_decision" => blocker
      }.freeze
    end

    def identifier?(value)
      value.is_a?(String) && value.match?(IDENTIFIER)
    end

    def text?(value)
      value.is_a?(String) && !value.strip.empty? && value.length <= MAX_TEXT && !value.match?(/[\r\n]/)
    end

    def text_list?(value)
      value.is_a?(Array) && value.length.between?(1, MAX_LIST) && value.all? { |item| text?(item) }
    end
  end
end
