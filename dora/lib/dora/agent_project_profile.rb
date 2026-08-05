# frozen_string_literal: true

require "yaml"

module Dora
  class AgentProjectProfile
    PATH_FIELDS = %w[entrypoints canonical_knowledge].freeze

    def self.load!(path, schema_path: File.expand_path("../../agent-project-profile.schema.yaml", __dir__))
      schema = YAML.load_file(schema_path)
      profile = YAML.load_file(path)
      fail!("agent profile schema kind is invalid") unless schema["kind"] == "dora_agent_project_profile_schema" && schema["version"].to_i == 1
      fail!("agent profile kind is invalid") unless profile.is_a?(Hash) && profile["kind"] == "dora_agent_project_profile" && profile["version"].to_i == schema["version"].to_i
      missing = schema.fetch("required_fields").reject { |field| field == "version" ? profile[field].is_a?(Integer) : present?(profile[field]) }
      fail!("agent profile is missing #{missing.join(", ")}") unless missing.empty?
      schema.fetch("named_list_fields").each { |field| validate_named_list!(field, profile.fetch(field)) }
      PATH_FIELDS.each do |field|
        Array(profile.fetch(field)).each do |item|
          fail!("agent profile #{field} item #{item["id"]} has an invalid path") unless safe_relative_path?(item["path"])
          fail!("agent profile #{field} item #{item["id"]} is missing purpose") unless string?(item["purpose"])
        end
      end
      Array(profile.fetch("stack_commands")).each { |item| %w[command purpose].each { |field| fail!("agent profile command #{item["id"]} is missing #{field}") unless string?(item[field]) } }
      %w[authority_limits evidence_requirements implementation_order].each { |section| Array(profile.fetch(section)).each { |item| fail!("agent profile #{section} item #{item["id"]} is missing rule") unless string?(item["rule"]) } }
      profile.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("agent profile YAML is invalid: #{error.message}")
    end

    def self.validate_named_list!(field, values)
      fail!("agent profile #{field} must be a non-empty list") unless values.is_a?(Array) && !values.empty?
      ids = values.map { |item| item.is_a?(Hash) && item["id"] }
      fail!("agent profile #{field} has invalid or duplicate ids") unless ids.all? { |id| string?(id) } && ids.uniq.length == ids.length
    end
    private_class_method :validate_named_list!

    def self.present?(value)
      value.is_a?(Array) ? !value.empty? : string?(value)
    end
    private_class_method :present?

    def self.string?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :string?

    def self.safe_relative_path?(path)
      string?(path) && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
