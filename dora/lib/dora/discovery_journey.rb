# frozen_string_literal: true

require_relative "discovery_provenance"

module Dora
  # Declares human-facing journey expectations from a cited skeleton. It does not
  # generate UI, execute scenarios, or retain test/evidence state.
  class DiscoveryJourney
    SCENARIO_STATES = %w[happy_path empty permission recovery].freeze

    def self.build!(skeleton:, journeys:, observed_at: Time.now.utc.iso8601)
      validated_skeleton = validate_skeleton!(skeleton)
      rows = validate_journeys!(journeys)
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_journey_coverage",
        source_references: (validated_skeleton.fetch("source_references") + rows.flat_map { |row| row.fetch("source_references") }).uniq.sort,
        observed_at: observed_at,
        payload: {
          "journeys" => rows,
          "foundation_choice_coverage" => validated_skeleton.dig("payload", "foundation_choice_coverage"),
          "completion_boundary" => "Journey coverage declares advisory user expectations only; it does not generate UI, execute a scenario, create a quality profile, record evidence, or change canonical state."
        }
      )
    end

    def self.validate_skeleton!(skeleton)
      fail!("journey skeleton is invalid") unless skeleton.is_a?(Hash) && skeleton["kind"] == "dora_discovery_skeleton" && skeleton["read_only"] == true && skeleton["disposition"] == "advisory" && skeleton.fetch("source_references").is_a?(Array) && skeleton.dig("payload", "foundation_choice_coverage").is_a?(Array)

      skeleton
    end
    private_class_method :validate_skeleton!

    def self.validate_journeys!(journeys)
      fail!("discovery journeys must be a non-empty list") unless journeys.is_a?(Array) && !journeys.empty?

      rows = journeys.map do |journey|
        fail!("discovery journey is invalid") unless journey.is_a?(Hash) && journey.keys.sort == %w[id outcome role scenario_states source_references trigger] && identifier?(journey["id"]) && statement?(journey["role"]) && statement?(journey["trigger"]) && statement?(journey["outcome"])
        states = journey.fetch("scenario_states")
        fail!("discovery journey scenario states are invalid") unless states.is_a?(Array) && !states.empty? && states.uniq.length == states.length && states.all? { |state| SCENARIO_STATES.include?(state) }

        {"id" => journey.fetch("id"), "role" => journey.fetch("role").dup.freeze, "trigger" => journey.fetch("trigger").dup.freeze, "outcome" => journey.fetch("outcome").dup.freeze, "scenario_states" => states.sort.freeze, "source_references" => validate_references!(journey.fetch("source_references"))}.freeze
      end
      fail!("discovery journey ids must be unique") unless rows.map { |row| row.fetch("id") }.uniq.length == rows.length

      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_journeys!

    def self.validate_references!(references)
      fail!("discovery journey references must be a non-empty list") unless references.is_a?(Array) && !references.empty?
      rows = references.map do |reference|
        fail!("discovery journey reference is invalid") unless reference.is_a?(String) && !reference.strip.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..")

        reference.dup.freeze
      end
      fail!("discovery journey references must be unique") unless rows.uniq.length == rows.length

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
