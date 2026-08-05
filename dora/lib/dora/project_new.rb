# frozen_string_literal: true

require_relative "project_intake"

module Dora
  class ProjectNew
    FIRST_WORK_FIELDS = %w[id title observable_outcome required_paths validation evidence_boundary].freeze
    SOURCE_FIELDS = %w[path ref checksum].freeze
    SOURCE_REVIEW_FIELDS = %w[id reviewed_by reviewed_at].freeze

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
      source = validate_source!(answers["dora_source"])

      {"kind" => "dora_project_new", "version" => 1, "project_id" => project_id, "intake" => ProjectIntake.build!(answers.fetch("intake")), "dora_source" => source, "first_work" => first_work.slice(*FIRST_WORK_FIELDS), "invention" => "none"}.freeze
    end

    def self.validate_source!(source)
      fail!("project new dora_source must be a mapping") unless source.is_a?(Hash)
      fail!("project new dora_source kind is invalid") unless source["kind"] == "dora_bootstrap_source" && source["version"].to_i == 1

      descriptor = source["source"]
      fail!("project new dora_source source must be a mapping") unless descriptor.is_a?(Hash)
      missing_source = SOURCE_FIELDS.reject { |field| statement?(descriptor[field]) }
      fail!("project new dora_source source is missing #{missing_source.join(", ")}") unless missing_source.empty?
      fail!("project new dora_source path must be local") if descriptor.fetch("path").match?(%r{\A[a-z]+://}i)
      fail!("project new dora_source ref is invalid") unless descriptor.fetch("ref").match?(/\A[0-9a-f]{40}\z/i)
      fail!("project new dora_source checksum is invalid") unless descriptor.fetch("checksum").match?(/\A[0-9a-f]{64}\z/i)

      review = source["review"]
      fail!("project new dora_source review must be a mapping") unless review.is_a?(Hash)
      missing_review = SOURCE_REVIEW_FIELDS.reject { |field| statement?(review[field]) }
      fail!("project new dora_source review is missing #{missing_review.join(", ")}") unless missing_review.empty?

      {"kind" => "dora_bootstrap_source", "version" => 1, "source" => descriptor.slice(*SOURCE_FIELDS), "review" => review.slice(*SOURCE_REVIEW_FIELDS)}.freeze
    end
    private_class_method :validate_source!

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
