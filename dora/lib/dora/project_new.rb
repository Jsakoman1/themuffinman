# frozen_string_literal: true

require_relative "project_intake"

module Dora
  class ProjectNew
    FIRST_WORK_FIELDS = %w[id title observable_outcome required_paths validation evidence_boundary].freeze

    def self.validate!(answers)
      fail!("project new answers must be a mapping") unless answers.is_a?(Hash)
      fail!("project new kind is invalid") unless answers["kind"] == "dora_project_new" && answers["version"].to_i == 1
      project_id = answers["project_id"]
      fail!("project new project_id is invalid") unless project_id.is_a?(String) && project_id.match?(/\A[a-z][a-z0-9-]*\z/)
      first_work = answers["first_work"]
      fail!("project new first_work must be a mapping") unless first_work.is_a?(Hash)
      FIRST_WORK_FIELDS.each { |field| fail!("project new first_work is missing #{field}") unless present?(first_work[field]) }
      validate_paths!(first_work.fetch("required_paths"))
      fail!("project new first_work validation is invalid") unless statement?(first_work.fetch("validation"))
      validate_statements!(first_work.fetch("evidence_boundary"), "evidence_boundary")

      {"kind" => "dora_project_new", "version" => 1, "project_id" => project_id, "intake" => ProjectIntake.build!(answers.fetch("intake")), "first_work" => first_work.slice(*FIRST_WORK_FIELDS), "invention" => "none"}.freeze
    end

    def self.validate_paths!(paths)
      fail!("project new first_work required_paths must be a non-empty list") unless paths.is_a?(Array) && !paths.empty?
      fail!("project new first_work required_paths contains an invalid path") unless paths.all? { |path| safe_relative_path?(path) }
    end
    private_class_method :validate_paths!

    def self.validate_statements!(values, label)
      fail!("project new first_work #{label} must be a non-empty list") unless values.is_a?(Array) && !values.empty? && values.all? { |value| statement?(value) }
    end
    private_class_method :validate_statements!

    def self.present?(value)
      value.is_a?(Array) ? !value.empty? : statement?(value)
    end
    private_class_method :present?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.safe_relative_path?(path)
      statement?(path) && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
