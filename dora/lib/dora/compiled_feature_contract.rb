# frozen_string_literal: true

require "yaml"

module Dora
  class CompiledFeatureContract
    SCHEMA_PATH = File.expand_path("../../compiled-feature.schema.yaml", __dir__)
    TARGET_STACK = "spring-vue-postgres-buildable"
    TYPE_MAP_PATH = File.expand_path("../../compiled-feature-type-map.yaml", __dir__)
    HTTP_METHODS = %w[GET POST PUT PATCH DELETE].freeze

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("compiled feature schema is invalid") unless schema["kind"] == "dora_compiled_feature_schema" && schema["version"].to_i == 1
      fail!("compiled feature must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_compiled_feature" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("compiled feature is missing #{missing.join(", ")}") unless missing.empty?
      fail!("compiled feature capability is invalid") unless identifier?(document["capability"])
      validate_stack!(document.fetch("stack"))
      validate_entity!(document.fetch("entity"))
      validate_permission!(document.fetch("permission"))
      validate_workflow!(document.fetch("workflow"))
      validate_api!(document.fetch("api"))
      validate_ui!(document.fetch("ui"))
      fail!("compiled feature confirmation must be true") unless document["confirmation"] == true
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("compiled feature YAML is invalid: #{error.message}")
    end

    def self.validate_type_mappings!(document, type_map_path: TYPE_MAP_PATH)
      feature = validate!(document)
      type_map = YAML.load_file(type_map_path)
      fail!("compiled feature type map is invalid") unless type_map["kind"] == "dora_compiled_feature_type_map" && type_map["version"].to_i == 1 && type_map["mappings"].is_a?(Array)
      mappings = type_map.fetch("mappings").to_h { |mapping| [mapping.fetch("sql_type"), mapping] }
      feature.dig("entity", "fields").each do |field|
        database = field.fetch("database")
        mapping = mappings[database.fetch("sql_type")]
        fail!("compiled feature SQL type is unsupported: #{database.fetch("sql_type")}") unless mapping && mapping["java_type"] == field.fetch("java_type")
        validate_database_controls!(database)
      end
      feature
    rescue Psych::Exception => error
      fail!("compiled feature type map YAML is invalid: #{error.message}")
    end

    def self.confirmed_foreign_key!(document, field_id:)
      feature = validate_type_mappings!(document)
      field = feature.dig("entity", "fields").find { |candidate| candidate.fetch("id") == field_id }
      foreign_key = field && field.dig("database", "foreign_key")
      fail!("compiled feature field #{field_id} lacks a confirmed foreign key") unless foreign_key && foreign_key.fetch("confirmed") == true
      foreign_key.freeze
    end

    def self.validate_database_controls!(database)
      allowed = %w[column sql_type nullable confirmed default default_confirmed unique unique_confirmed index index_confirmed foreign_key foreign_key_confirmed]
      unknown = database.keys - allowed
      fail!("compiled feature database mapping has unsupported controls: #{unknown.join(", ")}") unless unknown.empty?
      %w[default unique index foreign_key].each do |control|
        fail!("compiled feature database #{control} must be explicitly declared") unless database.key?(control) && database["#{control}_confirmed"] == true
      end
      fail!("compiled feature database default is invalid") unless database["default"].nil? || statement?(database["default"])
      fail!("compiled feature database unique is invalid") unless [true, false].include?(database["unique"])
      fail!("compiled feature database index is invalid") unless [true, false].include?(database["index"])
      foreign_key = database["foreign_key"]
      fail!("compiled feature database foreign key is invalid") unless foreign_key.nil? || (foreign_key.is_a?(Hash) && identifier?(foreign_key["table"]) && identifier?(foreign_key["column"]) && foreign_key["confirmed"] == true)
    end
    private_class_method :validate_database_controls!

    def self.validate_stack!(stack)
      valid = stack.is_a?(Hash) && stack["id"] == TARGET_STACK && dotted_identifier?(stack["package"]) && safe_relative?(stack["backend_root"]) && safe_relative?(stack["frontend_root"]) && safe_relative?(stack["migration_directory"]) && stack["confirmed"] == true
      fail!("compiled feature stack is invalid") unless valid
    end
    private_class_method :validate_stack!

    def self.validate_entity!(entity)
      valid = entity.is_a?(Hash) && identifier?(entity["id"]) && identifier?(entity["table"]) && entity["confirmed"] == true
      fail!("compiled feature entity is invalid") unless valid
      fields = Array(entity["fields"])
      fail!("compiled feature fields must be explicit") unless !fields.empty? && fields.all? { |field| valid_field?(field) }
    end
    private_class_method :validate_entity!

    def self.valid_field?(field)
      mapping = field.is_a?(Hash) && field["database"]
      field.is_a?(Hash) && identifier?(field["id"]) && statement?(field["java_type"]) && field["confirmed"] == true && mapping.is_a?(Hash) && identifier?(mapping["column"]) && statement?(mapping["sql_type"]) && [true, false].include?(mapping["nullable"]) && mapping["confirmed"] == true
    end
    private_class_method :valid_field?

    def self.validate_permission!(permission)
      valid = permission.is_a?(Hash) && identifier?(permission["id"]) && %w[service controller].include?(permission["enforcement"]) && statement?(permission["rule"]) && permission["confirmed"] == true
      fail!("compiled feature permission is invalid") unless valid
    end
    private_class_method :validate_permission!

    def self.validate_workflow!(workflow)
      valid = workflow.is_a?(Hash) && identifier?(workflow["id"]) && identifier?(workflow["initial_state"]) && workflow["confirmed"] == true
      fail!("compiled feature workflow is invalid") unless valid
      states = Array(workflow["states"])
      transitions = Array(workflow["transitions"])
      fail!("compiled feature workflow states must be explicit") unless !states.empty? && states.all? { |state| identifier?(state) } && states.include?(workflow["initial_state"])
      fail!("compiled feature workflow transitions must be explicit") unless !transitions.empty? && transitions.all? { |transition| transition.is_a?(Hash) && identifier?(transition["from"]) && identifier?(transition["to"]) && identifier?(transition["action"]) && transition["confirmed"] == true && states.include?(transition["from"]) && states.include?(transition["to"]) }
    end
    private_class_method :validate_workflow!

    def self.validate_api!(api)
      fail!("compiled feature API is invalid") unless api.is_a?(Hash) && safe_api_path?(api["base_path"]) && api["confirmed"] == true
      operations = Array(api["operations"])
      fail!("compiled feature API operations must be explicit") unless !operations.empty? && operations.all? { |operation| valid_operation?(operation) }
    end
    private_class_method :validate_api!

    def self.valid_operation?(operation)
      operation.is_a?(Hash) && identifier?(operation["id"]) && HTTP_METHODS.include?(operation["method"]) && safe_api_path?(operation["path"]) && identifiers?(operation["request_fields"]) && identifiers?(operation["response_fields"]) && operation["confirmed"] == true
    end
    private_class_method :valid_operation?

    def self.validate_ui!(ui)
      valid = ui.is_a?(Hash) && identifier?(ui["blueprint"]) && identifiers?(ui["states"]) && ui["confirmed"] == true
      fail!("compiled feature UI is invalid") unless valid
    end
    private_class_method :validate_ui!

    def self.identifiers?(value); value.is_a?(Array) && !value.empty? && value.all? { |item| identifier?(item) }; end
    private_class_method :identifiers?
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.dotted_identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)*\z/); end
    private_class_method :dotted_identifier?
    def self.safe_relative?(value); value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative?
    def self.safe_api_path?(value); value.is_a?(String) && value.start_with?("/") && !value.include?("..") && !value.include?("//"); end
    private_class_method :safe_api_path?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
