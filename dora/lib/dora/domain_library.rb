# frozen_string_literal: true

require "yaml"

module Dora
  class DomainLibrary
    def self.load!(path, schema_path: File.expand_path("../../domain-library.schema.yaml", __dir__))
      schema = YAML.load_file(schema_path)
      library = YAML.load_file(path)
      fail!("domain library schema kind is invalid") unless schema["kind"] == "dora_domain_library_schema" && schema["version"].to_i == 1
      fail!("domain library kind is invalid") unless library.is_a?(Hash) && library["kind"] == "dora_domain_library" && library["version"].to_i == schema["version"].to_i
      missing = schema.fetch("required_fields").reject { |field| field == "version" ? library[field].is_a?(Integer) : nonempty?(library[field]) }
      fail!("domain library is missing #{missing.join(", ")}") unless missing.empty?
      schema.fetch("named_list_fields").each { |field| validate_named_list!(field, library.fetch(field)) }
      Array(library.fetch("permission_rules")).each { |rule| %w[actor action boundary].each { |field| fail!("permission rule #{rule["id"]} is missing #{field}") unless present?(rule[field]) } }
      Array(library.fetch("workflows")).each do |workflow|
        fail!("workflow #{workflow["id"]} is missing initial_state") unless present?(workflow["initial_state"])
        transitions = workflow["transitions"]
        fail!("workflow #{workflow["id"]} must declare transitions") unless transitions.is_a?(Array) && !transitions.empty?
        transitions.each { |transition| %w[from to action].each { |field| fail!("workflow #{workflow["id"]} transition is missing #{field}") unless present?(transition[field]) } }
      end
      Array(library.fetch("acceptance_scenarios")).each { |scenario| %w[given when then].each { |field| fail!("acceptance scenario #{scenario["id"]} is missing #{field}") unless present?(scenario[field]) } }
      library.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("domain library YAML is invalid: #{error.message}")
    end

    def self.validate_named_list!(field, values)
      fail!("domain library #{field} must be a non-empty list") unless values.is_a?(Array) && !values.empty?
      ids = values.map { |value| value.is_a?(Hash) && value["id"] }
      fail!("domain library #{field} has invalid or duplicate ids") unless ids.all? { |id| present?(id) } && ids.uniq.length == ids.length
      values.each { |value| fail!("domain library #{field} item #{value["id"]} is missing description") unless field == "workflows" || present?(value["description"]) || field == "permission_rules" || field == "acceptance_scenarios" }
    end
    private_class_method :validate_named_list!

    def self.present?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :present?

    def self.nonempty?(value)
      value.is_a?(Array) ? !value.empty? : present?(value)
    end
    private_class_method :nonempty?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
