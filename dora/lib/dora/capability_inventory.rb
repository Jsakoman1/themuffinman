# frozen_string_literal: true

require "yaml"

module Dora
  class CapabilityInventory
    def self.validate!(document, schema_path: File.expand_path("../../capability-inventory.schema.yaml", __dir__))
      schema = YAML.load_file(schema_path)
      fail!("capability inventory schema is invalid") unless schema["kind"] == "dora_capability_inventory_schema" && schema["version"].to_i == 1
      fail!("capability inventory must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_capability_inventory" && document["version"].to_i == 1
      required!(document, schema.fetch("required_fields"), "capability inventory")
      component = document.fetch("component")
      fail!("capability inventory component is invalid") unless component.is_a?(Hash) && identifier?(component["id"]) && statement?(component["owner"])
      capabilities = document.fetch("capabilities")
      fail!("capability inventory capabilities must be a list") unless capabilities.is_a?(Array)
      ids = capabilities.map { |capability| validate_capability!(capability, schema) }
      fail!("capability inventory capability ids are duplicated") unless ids.uniq.length == ids.length
      document
    end

    def self.validate_file!(path, **options)
      validate!(YAML.load_file(path), **options)
    rescue Psych::Exception => error
      fail!("capability inventory YAML is invalid: #{error.message}")
    end

    def self.validate_capability!(capability, schema)
      fail!("capability inventory capability must be a mapping") unless capability.is_a?(Hash)
      required!(capability, schema.fetch("capability_required_fields"), "capability inventory capability")
      fail!("capability inventory capability id is invalid") unless identifier?(capability.fetch("id"))
      fail!("capability inventory capability title is invalid") unless statement?(capability.fetch("title"))
      fail!("capability inventory capability owner is invalid") unless statement?(capability.fetch("owner"))
      fail!("capability inventory capability status is invalid") unless schema.fetch("statuses").include?(capability.fetch("status"))
      %w[documentation_references evidence_references gaps].each { |field| fail!("capability inventory capability #{field} must be a list") unless capability.fetch(field).is_a?(Array) }
      fail!("verified capability requires evidence references") if capability.fetch("status") == "verified" && capability.fetch("evidence_references").empty?
      capability.fetch("id")
    end
    private_class_method :validate_capability!

    def self.required!(value, fields, label)
      missing = fields.reject { |field| value.key?(field) && !value[field].nil? }
      fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :required!
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]{0,63}\z/); end
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :identifier?, :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
