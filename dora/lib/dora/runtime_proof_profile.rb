# frozen_string_literal: true

require "yaml"

module Dora
  class RuntimeProofProfile
    SCHEMA_PATH = File.expand_path("../../runtime-proof-profile.schema.yaml", __dir__)
    FORBIDDEN_KEYS = %w[product capability entity seed_data production_endpoint user_action].freeze

    def self.load!(path)
      validate!(YAML.load_file(path))
    rescue Psych::Exception => error
      fail!("runtime-proof profile YAML is invalid: #{error.message}")
    end

    def self.validate!(profile, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("runtime-proof profile schema is invalid") unless schema["kind"] == "dora_runtime_proof_profile_schema" && schema["version"].to_i == 1
      fail!("runtime-proof profile must be a mapping") unless profile.is_a?(Hash) && profile["kind"] == "dora_runtime_proof_profile" && profile["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(profile[field]) }
      fail!("runtime-proof profile is missing #{missing.join(", ")}") unless missing.empty?
      fail!("runtime-proof profile id is invalid") unless profile["id"].is_a?(String) && profile["id"].match?(/\A[a-z][a-z0-9-]*\z/)
      fail!("runtime-proof profile browser installation must be explicit opt_in") unless profile["browser_installation"] == "explicit_opt_in"
      fail!("runtime-proof profile test command is invalid") unless statement?(profile["test_command"])
      fail!("runtime-proof profile evidence destination is invalid") unless safe_relative?(profile["evidence_destination"])
      fail!("runtime-proof profile non_guarantees must be a non-empty list") unless profile["non_guarantees"].is_a?(Array) && !profile["non_guarantees"].empty? && profile["non_guarantees"].all? { |item| statement?(item) }
      fail!("runtime-proof profile product boundary is invalid") unless profile["product_boundary"] == "technical_health_only"
      fail!("runtime-proof profile contains product behavior") if FORBIDDEN_KEYS.any? { |key| profile.key?(key) }
      profile.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("runtime-proof profile schema is invalid: #{error.message}")
    end

    def self.safe_relative?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
