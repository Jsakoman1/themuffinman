# frozen_string_literal: true

require_relative "discovery_provenance"

module Dora
  # Renders a cited classification of discovery statements. It intentionally has
  # no persistence or promotion path; owners still decide what belongs in Dora's
  # existing canonical product, domain, decision, and work artifacts.
  class DiscoveryAssumptionLedger
    CLASSIFICATIONS = %w[confirmed assumption external_research open_question deferred_without_commitment].freeze

    def self.build!(statements:, observed_at: Time.now.utc.iso8601)
      rows = validate_statements!(statements)
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_assumption_ledger",
        source_references: rows.flat_map { |row| row.fetch("source_references") }.uniq.sort,
        observed_at: observed_at,
        payload: {
          "statements" => rows,
          "completion_boundary" => "This ledger classifies cited discovery statements only; it does not accept a decision, create a backlog item, declare a capability, create work, or record evidence."
        }
      )
    end

    def self.validate_statements!(statements)
      fail!("discovery assumption statements must be a non-empty list") unless statements.is_a?(Array) && !statements.empty?

      rows = statements.map do |statement|
        fail!("discovery assumption statement is invalid") unless statement.is_a?(Hash) && statement.keys.sort == %w[classification id source_references statement]
        id = statement.fetch("id")
        fail!("discovery assumption statement id is invalid") unless identifier?(id)
        classification = statement.fetch("classification")
        fail!("discovery assumption statement classification is invalid") unless CLASSIFICATIONS.include?(classification)
        text = statement.fetch("statement")
        fail!("discovery assumption statement text is invalid") unless text.is_a?(String) && !text.strip.empty? && !text.match?(/[\r\n]/)
        references = validate_references!(statement.fetch("source_references"))

        {"id" => id, "classification" => classification, "statement" => text.dup.freeze, "source_references" => references}.freeze
      end
      fail!("discovery assumption statement ids must be unique") unless rows.map { |row| row.fetch("id") }.uniq.length == rows.length

      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_statements!

    def self.validate_references!(references)
      fail!("discovery assumption source references must be a non-empty list") unless references.is_a?(Array) && !references.empty?

      normalized = references.map do |reference|
        fail!("discovery assumption source reference is invalid") unless reference.is_a?(String) && !reference.strip.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..")

        reference.dup.freeze
      end
      fail!("discovery assumption source references must be unique") unless normalized.uniq.length == normalized.length

      normalized.sort.freeze
    end
    private_class_method :validate_references!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(%r{\A[a-z][a-z0-9-]*\z})
    end
    private_class_method :identifier?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
