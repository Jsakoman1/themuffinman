# frozen_string_literal: true

require "yaml"

module Dora
  class AuthPermissionStarter
    SCHEMA_PATH = File.expand_path("../../auth-permission-starter.schema.yaml", __dir__)

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("auth permission starter schema is invalid") unless schema["kind"] == "dora_auth_permission_starter_schema" && schema["version"].to_i == 1
      fail!("auth permission starter must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_auth_permission_starter" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("auth permission starter is missing #{missing.join(", ")}") unless missing.empty?
      authentication = document.fetch("authentication")
      fail!("authentication declaration is invalid") unless authentication.is_a?(Hash) && identifier?(authentication["mode"]) && authentication["confirmed"] == true
      roles = Array(document["roles"])
      fail!("auth roles must be explicitly confirmed") unless !roles.empty? && roles.all? { |role| role.is_a?(Hash) && identifier?(role["id"]) && statement?(role["description"]) && role["confirmed"] == true }
      ownership = document.fetch("ownership")
      fail!("ownership declaration is invalid") unless ownership.is_a?(Hash) && statement?(ownership["rule"]) && ownership["confirmed"] == true
      events = Array(document["audit_events"])
      fail!("audit event names must be explicit") unless !events.empty? && events.all? { |event| event.is_a?(Hash) && identifier?(event["id"]) && statement?(event["description"]) && event["confirmed"] == true }
      fail!("auth permission starter confirmation must be true") unless document["confirmation"] == true
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("auth permission starter YAML is invalid: #{error.message}")
    end

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
