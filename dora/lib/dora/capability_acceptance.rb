# frozen_string_literal: true

require "yaml"

module Dora
  class CapabilityAcceptance
    SCHEMA_PATH = File.expand_path("../../capability-acceptance.schema.yaml", __dir__)

    def self.declare!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("capability acceptance schema is invalid") unless schema["kind"] == "dora_capability_acceptance_schema" && schema["version"].to_i == 1
      fail!("capability acceptance must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_capability_acceptance" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("capability acceptance is missing #{missing.join(", ")}") unless missing.empty?
      fail!("capability acceptance capability is invalid") unless identifier?(document["capability"])
      %w[acceptance_statements static_checks test_scenarios runtime_scenarios].each { |field| validate_confirmed_items!(field, document.fetch(field)) }
      fail!("capability acceptance confirmation must be true") unless document["confirmation"] == true
      obligations = %w[static_checks test_scenarios runtime_scenarios].flat_map do |field|
        document.fetch(field).map { |item| {"kind" => field, "id" => item.fetch("id"), "status" => "unresolved", "expected" => item.fetch("expected")} }
      end
      {"kind" => "dora_capability_acceptance_obligations", "version" => 1, "capability" => document.fetch("capability"), "acceptance_statements" => document.fetch("acceptance_statements"), "obligations" => obligations, "acceptance_status" => "unresolved", "completion_boundary" => "These are declared obligations only. No check, test, or runtime scenario has passed evidence yet."}.freeze
    rescue Psych::Exception => error
      fail!("capability acceptance YAML is invalid: #{error.message}")
    end

    def self.validate_confirmed_items!(name, items)
      valid = items.is_a?(Array) && !items.empty? && items.all? { |item| item.is_a?(Hash) && identifier?(item["id"]) && statement?(item["expected"]) && item["confirmed"] == true }
      fail!("#{name} must contain explicit confirmed items") unless valid
    end
    private_class_method :validate_confirmed_items!
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
