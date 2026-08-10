# frozen_string_literal: true

require "yaml"

module Dora
  class ProductBrief
    def self.load!(path, schema_path: File.expand_path("../../product-brief.schema.yaml", __dir__))
      schema = YAML.load_file(schema_path)
      brief = YAML.load_file(path)
      fail!("product brief schema kind is invalid") unless schema["kind"] == "dora_product_brief_schema" && schema["version"].to_i == 1
      fail!("product brief kind is invalid") unless brief.is_a?(Hash) && brief["kind"] == "dora_product_brief" && brief["version"].to_i == schema["version"].to_i
      empty_allowed = Array(schema["optional_empty_list_fields"])
      missing = schema.fetch("required_fields").reject do |field|
        field == "version" ? brief[field].is_a?(Integer) : empty_allowed.include?(field) ? brief[field].is_a?(Array) : present?(brief[field])
      end
      fail!("product brief is missing #{missing.join(", ")}") unless missing.empty?
      schema.fetch("list_fields").each do |field|
        values = brief[field]
        fail!("product brief #{field} must be a non-empty list of statements") unless values.is_a?(Array) && !values.empty? && values.all? { |value| value.is_a?(String) && !value.strip.empty? }
      end
      empty_allowed.each do |field|
        values = brief[field]
        fail!("product brief #{field} must be a list of statements") unless values.is_a?(Array) && values.all? { |value| value.is_a?(String) && !value.strip.empty? }
      end
      brief.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("product brief YAML is invalid: #{error.message}")
    end

    def self.present?(value)
      value.is_a?(Array) ? !value.empty? : value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :present?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
