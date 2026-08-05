# frozen_string_literal: true

require_relative "vertical_slice_proposal"

module Dora
  class VerticalSliceReadiness
    DECISION_CATEGORIES = {"data_safety" => "data-safety", "workflow" => "workflow", "permission" => "permission", "technical" => "technical"}.freeze

    def self.evaluate!(proposal)
      validated = VerticalSliceProposal.validate!(proposal)
      blockers = validated.fetch("gaps").map { |gap| blocker_for(gap) }
      ready = blockers.empty?
      {
        "ready_to_plan" => ready,
        "blocking_gaps" => blockers,
        "recommended_next_action" => ready ? "Create one project-owned atomic implementation plan and review it before source changes." : "Confirm the listed decisions with the product owner, then regenerate or review the proposal."
      }
    end

    def self.evaluate_report!(report)
      fail!("vertical slice report is invalid") unless report.is_a?(Hash) && report["kind"] == "dora_vertical_slice_report" && report["version"].to_i == 1
      evaluate!(report.fetch("proposal"))
    end

    def self.blocker_for(gap)
      category = DECISION_CATEGORIES.find { |_id, label| gap.downcase.include?(label) }&.first || "other"
      {"category" => category, "detail" => gap, "source" => "proposal.gaps"}
    end
    private_class_method :blocker_for
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
