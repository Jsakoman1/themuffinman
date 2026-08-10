# frozen_string_literal: true

require_relative "discovery_provenance"

module Dora
  # Projects a reviewable first delivery from accepted discovery artifacts. It is
  # not a WorkArtifact and cannot start, verify, or generate implementation work.
  class DiscoveryDeliveryHandoff
    def self.build!(skeleton:, journeys:, selection:, observed_at: Time.now.utc.iso8601)
      validate_source!(skeleton, "dora_discovery_skeleton")
      validate_source!(journeys, "dora_discovery_journey_coverage")
      selected = validate_selection!(selection)
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_delivery_handoff",
        source_references: (skeleton.fetch("source_references") + journeys.fetch("source_references") + selected.fetch("source_references")).uniq.sort,
        observed_at: observed_at,
        payload: {
          "first_capability" => selected.fetch("first_capability"),
          "cross_layer_obligations" => selected.fetch("cross_layer_obligations"),
          "scenario_states" => journeys.dig("payload", "journeys").flat_map { |journey| journey.fetch("scenario_states") }.uniq.sort,
          "next_atomic_candidate" => selected.fetch("next_atomic_candidate"),
          "completion_boundary" => "This handoff is read-only and advisory; it is not a work artifact and cannot activate or verify work, write a decision, create a project or code, invoke GitHub, mutate a consumer, or start an agent."
        }
      )
    end

    def self.validate_source!(source, kind)
      fail!("delivery handoff source is invalid") unless source.is_a?(Hash) && source["kind"] == kind && source["read_only"] == true && source["disposition"] == "advisory" && source.fetch("source_references").is_a?(Array)
    end
    private_class_method :validate_source!

    def self.validate_selection!(selection)
      fail!("delivery handoff selection is invalid") unless selection.is_a?(Hash) && selection.keys.sort == %w[cross_layer_obligations first_capability next_atomic_candidate source source_references] && selection["source"] == "user_confirmed" && statement?(selection["first_capability"]) && statement?(selection["next_atomic_candidate"]) && selection["cross_layer_obligations"].is_a?(Array) && !selection.fetch("cross_layer_obligations").empty? && selection.fetch("cross_layer_obligations").all? { |item| statement?(item) }
      references = selection.fetch("source_references")
      fail!("delivery handoff selection references are invalid") unless references.is_a?(Array) && !references.empty? && references.uniq.length == references.length && references.all? { |item| item.is_a?(String) && !item.empty? && !item.start_with?("/") && !item.split("#", 2).first.split("/").include?("..") }
      {"first_capability" => selection.fetch("first_capability").dup.freeze, "cross_layer_obligations" => selection.fetch("cross_layer_obligations").map { |item| item.dup.freeze }.sort.freeze, "next_atomic_candidate" => selection.fetch("next_atomic_candidate").dup.freeze, "source_references" => references.sort.freeze}.freeze
    end
    private_class_method :validate_selection!

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
