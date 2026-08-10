# frozen_string_literal: true

require "time"

module Dora
  # Builds the common safe envelope for future greenfield discovery projections.
  # It is intentionally local and value-only: callers retain authority over every
  # source artifact, decision, work item, and external action.
  class DiscoveryProvenance
    VERSION = 1
    AUTHORITY_BOUNDARY = "This advisory discovery output is read-only and non-canonical; it cannot change work, inventory, or execution status, create or amend an owner decision, write code, invoke GitHub, mutate a consumer project, or start a local or remote runner or agent.".freeze

    def self.advisory!(kind:, source_references:, observed_at: Time.now.utc.iso8601, payload:)
      normalized_kind = validate_kind!(kind)
      references = validate_references!(source_references)
      timestamp = validate_observed_at!(observed_at)
      value = clone_value!(payload)

      {
        "kind" => normalized_kind,
        "version" => VERSION,
        "observed_at" => timestamp,
        "source_references" => references,
        "read_only" => true,
        "disposition" => "advisory",
        "payload" => value,
        "authority_boundary" => AUTHORITY_BOUNDARY
      }.freeze
    end

    def self.validate_kind!(kind)
      fail!("discovery advisory kind is invalid") unless kind.is_a?(String) && kind.match?(%r{\Adora_discovery_[a-z][a-z0-9_]*\z})

      kind
    end
    private_class_method :validate_kind!

    def self.validate_references!(references)
      fail!("discovery advisory source_references must be a non-empty list") unless references.is_a?(Array) && !references.empty?

      normalized = references.map do |reference|
        fail!("discovery advisory source reference is invalid") unless reference.is_a?(String) && !reference.strip.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..")

        reference
      end
      fail!("discovery advisory source references must be unique") unless normalized.uniq.length == normalized.length

      normalized.sort.freeze
    end
    private_class_method :validate_references!

    def self.validate_observed_at!(observed_at)
      fail!("discovery advisory observed_at is invalid") unless observed_at.is_a?(String)

      parsed = Time.iso8601(observed_at)
      canonical = parsed.utc.iso8601
      fail!("discovery advisory observed_at must be UTC ISO-8601") unless observed_at == canonical

      canonical
    rescue ArgumentError
      fail!("discovery advisory observed_at is invalid")
    end
    private_class_method :validate_observed_at!

    def self.clone_value!(value)
      case value
      when String
        value.dup.freeze
      when Array
        value.map { |item| clone_value!(item) }.freeze
      when Hash
        fail!("discovery advisory payload keys must be strings") unless value.keys.all? { |key| key.is_a?(String) && !key.empty? }

        value.sort.to_h { |key, item| [key.dup.freeze, clone_value!(item)] }.freeze
      when Numeric, TrueClass, FalseClass, NilClass
        value
      else
        fail!("discovery advisory payload contains an unsupported value")
      end
    end
    private_class_method :clone_value!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
