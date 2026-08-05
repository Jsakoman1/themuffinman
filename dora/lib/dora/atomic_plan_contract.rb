# frozen_string_literal: true

require "yaml"

module Dora
  class AtomicPlanContract
    REQUIRED_FIELDS = %w[id title observable_outcome dependencies evidence_boundary paths required_paths validation].freeze

    def self.validate!(plan_path)
      plan = YAML.load_file(plan_path)
      fail!("atomic plan must be a work plan") unless plan.is_a?(Hash) && plan["kind"] == "work" && plan["version"].to_i == 1
      tasks = Array(plan["tasks"])
      fail!("atomic plan must declare at least one task") if tasks.empty?

      ids = tasks.map { |task| task.is_a?(Hash) ? task["id"] : nil }
      fail!("atomic plan task ids must be unique and non-empty") if ids.any? { |id| id.to_s.strip.empty? } || ids.uniq.length != ids.length

      tasks.each do |task|
        required_fields = REQUIRED_FIELDS.reject { |field| field == "dependencies" ? task.key?(field) : present?(task[field]) }
        fail!("atomic task #{task["id"] || "<unknown>"} is missing #{required_fields.join(", ")}") unless required_fields.empty?
        fail!("atomic task #{task["id"]} dependencies must be a list") unless task["dependencies"].is_a?(Array)
        fail!("atomic task #{task["id"]} evidence_boundary must be a non-empty list") unless task["evidence_boundary"].is_a?(Array) && !task["evidence_boundary"].empty?
        paths = Array(task["paths"])
        required_paths = Array(task["required_paths"])
        fail!("atomic task #{task["id"]} paths must equal required_paths") unless paths == required_paths && !paths.empty?
        fail!("atomic task #{task["id"]} has an unsafe required path") unless required_paths.all? { |path| safe_relative_path?(path) }
        fail!("atomic task #{task["id"]} has recursive validation") if recursive_validation?(task["validation"])
        fail!("atomic task #{task["id"]} requires_external_approval must be boolean") if task.key?("requires_external_approval") && ![true, false].include?(task["requires_external_approval"])
      end

      {"id" => plan["id"], "tasks" => tasks.length}
    rescue Psych::Exception => error
      fail!("atomic plan YAML is invalid: #{error.message}")
    end

    def self.present?(value)
      value.is_a?(Array) ? !value.empty? : !value.to_s.strip.empty?
    end
    private_class_method :present?

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.recursive_validation?(command)
      command.to_s.match?(/\bwork-verify\b/) || command.to_s.match?(%r{scripts/verify-work\.rb})
    end
    private_class_method :recursive_validation?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
