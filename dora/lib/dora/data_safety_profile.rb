# frozen_string_literal: true

require "yaml"

module Dora
  class DataSafetyProfile
    SCHEMA_PATH = File.expand_path("../../data-safety-profile.schema.yaml", __dir__)

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("data safety profile schema is invalid") unless schema["kind"] == "dora_data_safety_profile_schema" && schema["version"].to_i == 1
      fail!("data safety profile must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_data_safety_profile" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("data safety profile is missing #{missing.join(", ")}") unless missing.empty?
      %w[backup restore_test export retention audit].each { |field| validate_declared_control!(field, document.fetch(field)) }
      demo = document.fetch("demo_data")
      fail!("demo data isolation is invalid") unless demo.is_a?(Hash) && statement?(demo["isolation"]) && demo["real_data_excluded"] == true && demo["confirmed"] == true
      fail!("data safety profile confirmation must be true") unless document["confirmation"] == true
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("data safety profile YAML is invalid: #{error.message}")
    end

    def self.validate_declared_control!(name, value)
      fail!("#{name} declaration is invalid") unless value.is_a?(Hash) && statement?(value["strategy"]) && value["external_approval_required"] == true && value["confirmed"] == true
    end
    private_class_method :validate_declared_control!
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
