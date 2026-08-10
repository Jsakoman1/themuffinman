#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/intent_plan_alignment"

def proposal(overrides = {})
  {
    "intent_plan_id" => "mp-01-intent",
    "intended_outcome" => "Align a planning proposal with Dora state.",
    "in_scope_work" => ["Evaluate one bounded proposal."],
    "non_goals" => ["Do not create Dora work."],
    "fixed_owner_decisions" => [],
    "candidate_slices" => [
      {"id" => "first-slice", "outcome" => "Prepare the first Dora slice.", "depends_on" => [], "gates" => ["no_owner_decision_pending"]},
      {"id" => "later-slice", "outcome" => "Wait for Dora verification.", "depends_on" => ["first-slice"], "gates" => ["no_owner_decision_pending", "prior_slice_verification"]}
    ],
    "required_owner_readback" => %w[phase alignment_result first_safe_next_action actionable_blocker_or_decision]
  }.merge(overrides)
end

def state(overrides = {})
  {"state" => "HEALTHY", "active_delivery" => nil, "latest_verified_delivery" => nil, "open_decisions" => [], "accepted_decisions" => []}.merge(overrides)
end

accepted = Dora::IntentPlanAlignment.evaluate(proposal: proposal, canonical_state: state)
abort "no-active-work proposal was not accepted" unless accepted.fetch("alignment_result") == "ACCEPTED" && accepted.fetch("first_eligible_slice") == {"id" => "first-slice", "status" => "ELIGIBLE"}
abort "later slice was released early" unless accepted.fetch("later_slices") == [{"id" => "later-slice", "status" => "BLOCKED_PENDING_DORA_VERIFICATION"}]
accepted_before_packet = Marshal.load(Marshal.dump(accepted))
packet = Dora::IntentPlanAlignment.option_packet!(alignment: accepted, owner_question: "Which bounded option should the owner choose?")
abort "option packet is not explicitly owner-mediated" unless packet.fetch("read_only") && packet.fetch("non_canonical") && packet.dig("alignment_facts", "alignment_result") == "ACCEPTED" && packet.dig("copyable_response_template", "required_sections") == %w[alternatives benefits risks assumptions recommendation]
abort "option packet provenance is incomplete" unless packet.fetch("observed_at").match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && packet.fetch("source_references") == ["intent_plan_alignment"]
abort "option packet mutated alignment facts" unless accepted == accepted_before_packet
abort "option packet granted authority" unless packet.fetch("authority_boundary").include?("does not contact ChatGPT") && packet.fetch("authority_boundary").include?("does not contact")

reconciled = Dora::IntentPlanAlignment.evaluate(proposal: proposal, canonical_state: state("latest_verified_delivery" => {"id" => "prior-delivery"}))
abort "verified baseline was not reconciled" unless reconciled.fetch("alignment_result") == "RECONCILED"

active = Dora::IntentPlanAlignment.evaluate(proposal: proposal, canonical_state: state("active_delivery" => {"id" => "active-delivery"}))
abort "active work was not held for owner decision" unless active.fetch("alignment_result") == "OWNER_DECISION_NEEDED" && active.fetch("first_eligible_slice").nil?

open_decision = Dora::IntentPlanAlignment.evaluate(proposal: proposal, canonical_state: state("open_decisions" => [{"id" => "owner-choice"}]))
abort "open decision was not held" unless open_decision.fetch("alignment_result") == "OWNER_DECISION_NEEDED"

invalid_state = Dora::IntentPlanAlignment.evaluate(proposal: proposal, canonical_state: state("state" => "INVALID"))
abort "invalid canonical state was not held" unless invalid_state.fetch("alignment_result") == "OWNER_DECISION_NEEDED"

canonical_decision = {"id" => "read-only-boundary", "statement" => "Keep the bridge read-only."}
decision_proposal = proposal("fixed_owner_decisions" => [canonical_decision])
decision_accepted = Dora::IntentPlanAlignment.evaluate(proposal: decision_proposal, canonical_state: state("accepted_decisions" => [canonical_decision]))
abort "accepted owner decision was not honored" unless decision_accepted.fetch("alignment_result") == "ACCEPTED"
decision_conflict = Dora::IntentPlanAlignment.evaluate(proposal: decision_proposal, canonical_state: state("accepted_decisions" => [canonical_decision.merge("statement" => "Changed")]))
abort "decision conflict was not held" unless decision_conflict.fetch("alignment_result") == "OWNER_DECISION_NEEDED"

invalid = Dora::IntentPlanAlignment.evaluate(proposal: proposal.reject { |key, _| key == "non_goals" }, canonical_state: state)
abort "incomplete proposal was accepted" unless invalid.fetch("alignment_result") == "OWNER_DECISION_NEEDED"

secret = "/private/owner-secret"
redacted = Dora::IntentPlanAlignment.evaluate(proposal: proposal("intended_outcome" => secret), canonical_state: state)
abort "owner readback echoed proposal content" if redacted.to_s.include?(secret)

puts "Dora Intent Plan alignment test passed (accepted, reconciled, conflict, invalid, no-active-work, and redaction)."
