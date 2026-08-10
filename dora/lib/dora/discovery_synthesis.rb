# frozen_string_literal: true

require_relative "discovery_provenance"

module Dora
  # Deterministically surfaces agreement and disagreement in already-advisory
  # discovery inputs. It never chooses a position or schedules other agents.
  class DiscoverySynthesis
    CLAIM_CLASSES = %w[confirmed assumption external_research open_question].freeze

    def self.build!(sources:, claims:, observed_at: Time.now.utc.iso8601)
      inputs = validate_sources!(sources)
      rows = validate_claims!(claims)
      grouped = rows.group_by { |row| row.fetch("topic") }
      conflicts = grouped.each_with_object([]) do |(topic, topic_claims), results|
        positions = topic_claims.map { |claim| claim.fetch("statement") }.uniq.sort
        next unless positions.length > 1

        results << {"topic" => topic, "positions" => positions, "source_references" => topic_claims.flat_map { |claim| claim.fetch("source_references") }.uniq.sort}.freeze
      end.sort_by { |conflict| conflict.fetch("topic") }
      agreement = grouped.each_with_object([]) do |(topic, topic_claims), results|
        next unless topic_claims.map { |claim| claim.fetch("statement") }.uniq.length == 1

        results << {"topic" => topic, "statement" => topic_claims.first.fetch("statement"), "source_references" => topic_claims.flat_map { |claim| claim.fetch("source_references") }.uniq.sort}.freeze
      end.sort_by { |item| item.fetch("topic") }
      uncertainty = rows.select { |claim| claim.fetch("classification") != "confirmed" }.map { |claim| claim.slice("id", "topic", "classification", "statement", "source_references") }
      owner_questions = conflicts.map { |conflict| {"id" => "resolve-#{conflict.fetch("topic")}", "question" => "Which position should Dora use for #{conflict.fetch("topic").tr("-", " ")}?", "source_references" => conflict.fetch("source_references") } }
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_synthesis",
        source_references: inputs.flat_map { |input| input.fetch("source_references") }.concat(rows.flat_map { |row| row.fetch("source_references") }).uniq.sort,
        observed_at: observed_at,
        payload: {
          "agreement" => agreement,
          "conflicts" => conflicts,
          "uncertainty" => uncertainty,
          "owner_questions" => owner_questions,
          "completion_boundary" => "Synthesis only surfaces cited agreement, conflict, and uncertainty; it cannot resolve a conflict, create a decision, change status, persist a queue, or invoke an external, repository, consumer, GitHub, runner, or agent operation."
        }
      )
    end

    def self.validate_sources!(sources)
      fail!("discovery synthesis sources must be a non-empty list") unless sources.is_a?(Array) && !sources.empty?
      sources.each do |source|
        fail!("discovery synthesis source is invalid") unless source.is_a?(Hash) && source["read_only"] == true && source["disposition"] == "advisory" && source["source_references"].is_a?(Array) && !source.fetch("source_references").empty?
      end
      sources
    end
    private_class_method :validate_sources!

    def self.validate_claims!(claims)
      fail!("discovery synthesis claims must be a non-empty list") unless claims.is_a?(Array) && !claims.empty?
      rows = claims.map do |claim|
        fail!("discovery synthesis claim is invalid") unless claim.is_a?(Hash) && claim.keys.sort == %w[classification id source_references statement topic] && identifier?(claim["id"]) && identifier?(claim["topic"]) && CLAIM_CLASSES.include?(claim["classification"]) && statement?(claim["statement"])
        {"id" => claim.fetch("id"), "topic" => claim.fetch("topic"), "classification" => claim.fetch("classification"), "statement" => claim.fetch("statement").dup.freeze, "source_references" => references!(claim.fetch("source_references"))}.freeze
      end
      fail!("discovery synthesis claim ids must be unique") unless rows.map { |row| row.fetch("id") }.uniq.length == rows.length
      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_claims!

    def self.references!(references)
      fail!("discovery synthesis references are invalid") unless references.is_a?(Array) && !references.empty? && references.uniq.length == references.length && references.all? { |reference| reference.is_a?(String) && !reference.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..") }
      references.sort.freeze
    end
    private_class_method :references!

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
