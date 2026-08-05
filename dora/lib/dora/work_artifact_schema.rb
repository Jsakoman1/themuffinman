# frozen_string_literal: true

require "yaml"

module Dora
  class WorkArtifactSchema
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
      tasks.each { |task| required_fields!(task, definition.fetch("task_required_fields"), "work task") }
    end
    private_class_method :validate_tasks!

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

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
