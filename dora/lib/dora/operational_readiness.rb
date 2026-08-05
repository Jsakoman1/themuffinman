# frozen_string_literal: true

require "yaml"

module Dora
  class OperationalReadiness
    SCHEMA_PATH = File.expand_path("../../operational-readiness.schema.yaml", __dir__)
    SECRET_VALUE_KEYS = %w[value secret token password api_key].freeze

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("operational readiness schema is invalid") unless schema["kind"] == "dora_operational_readiness_schema" && schema["version"].to_i == 1
      fail!("operational readiness profile must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_operational_readiness" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("operational readiness profile is missing #{missing.join(", ")}") unless missing.empty?
      fail!("environment declarations are invalid") unless declarations?(document["environment"])
      secrets = Array(document["secret_references"])
      fail!("secret references are invalid") unless !secrets.empty? && secrets.all? { |secret| safe_secret_reference?(secret) }
      %w[health logging rate_limits deployment_runbook].each { |field| fail!("#{field} declaration is invalid") unless statement?(document[field]) }
      fail!("dependency declarations are invalid") unless declarations?(document["dependencies"])
      fail!("operational readiness confirmation must be true") unless document["confirmation"] == true
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("operational readiness YAML is invalid: #{error.message}")
    end

    def self.declarations?(value); value.is_a?(Array) && !value.empty? && value.all? { |item| item.is_a?(Hash) && identifier?(item["id"]) && statement?(item["purpose"]) }; end
    private_class_method :declarations?
    def self.safe_secret_reference?(value); value.is_a?(Hash) && identifier?(value["id"]) && statement?(value["reference"]) && (value.keys & SECRET_VALUE_KEYS).empty?; end
    private_class_method :safe_secret_reference?
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
