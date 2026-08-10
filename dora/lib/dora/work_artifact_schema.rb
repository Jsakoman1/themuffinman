# frozen_string_literal: true

require "yaml"

module Dora
  class WorkArtifactSchema
    IMPLEMENTATION_CONTRACT_PATH_FIELDS = %w[read_paths write_paths schema_paths api_paths ui_paths test_paths documentation_paths].freeze
    def self.validate!(artifact_path, schema_path:)
      schema = YAML.load_file(schema_path)
      fail!("work artifact schema is invalid") unless schema["kind"] == "dora_work_artifact_schema" && schema["version"].to_i == 1
      artifact = YAML.load_file(artifact_path)
      fail!("work artifact must be a mapping") unless artifact.is_a?(Hash)
      definition = schema.fetch("artifacts").values.find { |candidate| candidate.fetch("kind") == artifact["kind"] }
      fail!("unsupported work artifact kind: #{artifact["kind"]}") unless definition
      required_fields!(artifact, definition.fetch("required_fields"), artifact.fetch("kind"))
      validate_tasks!(artifact, definition) if artifact["kind"] == "work"
      validate_inventory_items!(artifact, definition) if artifact["kind"] == "execution_inventory"
      artifact
    end

    def self.validate_tasks!(artifact, definition)
      tasks = artifact.fetch("tasks")
      fail!("work tasks must be a non-empty list") unless tasks.is_a?(Array) && !tasks.empty?
      tasks.each do |task|
        required_fields!(task, definition.fetch("task_required_fields"), "work task")
        validate_implementation_contract!(task["implementation_contract"]) if task.key?("implementation_contract")
      end
    end
    private_class_method :validate_tasks!

    def self.validate_implementation_contract!(contract)
      fail!("task implementation_contract must be a mapping") unless contract.is_a?(Hash)
      required = IMPLEMENTATION_CONTRACT_PATH_FIELDS + ["permission_owner", "runtime_evidence"]
      required_fields!(contract, required, "task implementation_contract")
      IMPLEMENTATION_CONTRACT_PATH_FIELDS.each do |field|
        paths = contract.fetch(field)
        fail!("task implementation_contract #{field} must be a list of project-relative paths") unless paths.is_a?(Array) && paths.all? { |path| safe_relative_path?(path) }
      end
      fail!("task implementation_contract permission_owner must be a non-empty string") unless contract.fetch("permission_owner").is_a?(String) && !contract.fetch("permission_owner").empty?
      runtime = contract.fetch("runtime_evidence")
      fail!("task implementation_contract runtime_evidence must be a mapping") unless runtime.is_a?(Hash) && [true, false].include?(runtime["required"]) && runtime["paths"].is_a?(Array) && runtime["paths"].all? { |path| safe_relative_path?(path) }
      fail!("task implementation_contract runtime_evidence paths are required when runtime evidence is required") if runtime.fetch("required") && runtime.fetch("paths").empty?
    end
    private_class_method :validate_implementation_contract!

    def self.validate_inventory_items!(artifact, definition)
      items = artifact.fetch("items")
      fail!("execution inventory items must be a non-empty list") unless items.is_a?(Array) && !items.empty?
      items.each { |item| required_fields!(item, definition.fetch("item_required_fields"), "execution inventory item") }
    end
    private_class_method :validate_inventory_items!

    def self.required_fields!(value, fields, label)
      fail!("#{label} must be a mapping") unless value.is_a?(Hash)
      missing = fields.reject { |field| value.key?(field) && !value[field].nil? && value[field] != "" }
      fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :required_fields!

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
