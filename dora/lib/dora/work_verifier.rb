# frozen_string_literal: true

require "yaml"
require_relative "adapter"
require_relative "work_artifact_schema"

module Dora
  class WorkVerifier
    WORK_ARTIFACT_SCHEMA_PATH = File.expand_path("../../templates/work-artifact-schema.yaml", __dir__)
    WORK_TASK_FIELDS = %w[id title observable_outcome dependencies evidence_boundary paths required_paths validation].freeze
    COMMON_FIELDS = %w[kind version id status baseline].freeze

    def self.validate_plan!(adapter_path, plan_path, schema_path)
      adapter = Adapter.validate!(adapter_path, schema_path)
      project_root = adapter.fetch("root")
      work_root = File.expand_path(adapter.fetch("paths").fetch("work_plans"), project_root)
      plan_absolute_path = resolve_under!(project_root, plan_path, "work plan")
      fail!("work plan must be inside the adapter work_plans path") unless plan_absolute_path.start_with?("#{work_root}/")
      fail!("work plan does not exist: #{plan_path}") unless File.file?(plan_absolute_path)

      plan = YAML.load_file(plan_absolute_path)
      required_fields!(plan, COMMON_FIELDS, "work plan")
      fail!("work plan version is invalid") unless plan["version"].to_i == 1
      case plan.fetch("kind")
      when "work"
        validate_work_tasks!(plan)
        WorkArtifactSchema.validate!(plan_absolute_path, schema_path: WORK_ARTIFACT_SCHEMA_PATH) if Array(plan["tasks"]).any? { |task| task.is_a?(Hash) && task.key?("implementation_contract") }
      when "master"
        children = Array(plan["children"])
        fail!("master work plan must declare children") if children.empty?
      else
        fail!("work plan kind is invalid")
      end

      {"project" => adapter.fetch("project"), "kind" => plan.fetch("kind"), "id" => plan.fetch("id")}
    end

    def self.resolve_under!(root, path, label)
      fail!("#{label} must be a non-empty relative path") unless path.is_a?(String) && !path.empty? && !path.start_with?("/")
      resolved = File.expand_path(path, root)
      fail!("#{label} resolves outside project.root") unless resolved.start_with?("#{root}/")
      resolved
    end
    private_class_method :resolve_under!

    def self.validate_work_tasks!(plan)
      tasks = plan["tasks"]
      fail!("work plan must declare tasks") unless tasks.is_a?(Array) && !tasks.empty?
      tasks.each do |task|
        required_fields!(task, WORK_TASK_FIELDS, "work task")
        fail!("work task required_paths must be a non-empty list") unless task["required_paths"].is_a?(Array) && !task["required_paths"].empty?
        fail!("work task dependencies must be a list") unless task["dependencies"].is_a?(Array)
        fail!("work task evidence_boundary must be a non-empty list") unless task["evidence_boundary"].is_a?(Array) && !task["evidence_boundary"].empty?
      end
    end
    private_class_method :validate_work_tasks!

    def self.required_fields!(value, fields, label)
      fail!("#{label} must be a mapping") unless value.is_a?(Hash)
      missing = fields.reject { |field| value.key?(field) && !value[field].to_s.empty? }
      fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :required_fields!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
