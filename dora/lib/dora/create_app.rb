# frozen_string_literal: true

require_relative "idea_interview"
require_relative "project_new"
require_relative "stack_catalog"

module Dora
  class CreateApp
    FIRST_WORK_FIELDS = ProjectNew::FIRST_WORK_FIELDS

    def self.validate!(bundle)
      fail!("create-app bundle must be a mapping") unless bundle.is_a?(Hash) && bundle["kind"] == "dora_create_app" && bundle["version"].to_i == 1
      %w[interview dora_source first_capability first_work].each { |field| fail!("create-app bundle is missing #{field}") unless bundle.key?(field) }
      interview = IdeaInterview.validate!(bundle.fetch("interview"))
      capability = validate_capability!(bundle.fetch("first_capability"), interview)
      work = validate_work!(bundle.fetch("first_work"))
      source = ProjectNew.send(:validate_source!, bundle.fetch("dora_source"))
      starter = bundle["starter"]
      fail!("create-app starter is invalid") unless starter.nil? || identifier?(starter)
      starter_selection = starter ? StackCatalog.find!(starter) : nil
      integration = bundle.fetch("codex_integration", false)
      fail!("create-app codex_integration must be boolean") unless integration == true || integration == false
      {"kind" => "dora_create_app", "version" => 1, "project_id" => interview.fetch("project_id"), "interview" => interview, "dora_source" => source, "first_capability" => capability, "first_work" => work, "starter" => starter, "starter_selection" => starter_selection, "codex_integration" => integration, "invention" => "none", "completion_boundary" => "Create-app preparation creates declared starting context only; it does not implement a capability, resolve decisions, or prove readiness."}.compact.freeze
    end

    def self.validate_capability!(capability, interview)
      fail!("create-app first_capability must be a mapping") unless capability.is_a?(Hash)
      %w[id title interview_answer].each { |field| fail!("create-app first_capability is missing #{field}") unless capability.key?(field) }
      fail!("create-app first_capability id is invalid") unless identifier?(capability["id"])
      fail!("create-app first_capability title is invalid") unless statement?(capability["title"])
      answer = interview.fetch("answers").find { |row| row.fetch("id") == capability.fetch("interview_answer") }
      fail!("create-app first_capability must cite an interview answer") unless answer
      capability.slice("id", "title", "interview_answer").merge("provenance" => answer.slice("id", "source"))
    end
    private_class_method :validate_capability!

    def self.validate_work!(work)
      fail!("create-app first_work must be a mapping") unless work.is_a?(Hash)
      FIRST_WORK_FIELDS.each { |field| fail!("create-app first_work is missing #{field}") unless present?(work[field]) }
      fail!("create-app first_work id is invalid") unless identifier?(work["id"])
      fail!("create-app first_work required_paths are invalid") unless work["required_paths"].is_a?(Array) && work["required_paths"].all? { |path| safe_relative_path?(path) }
      fail!("create-app first_work evidence_boundary is invalid") unless work["evidence_boundary"].is_a?(Array) && work["evidence_boundary"].all? { |item| statement?(item) }
      work.slice(*FIRST_WORK_FIELDS)
    end
    private_class_method :validate_work!

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); value.is_a?(Array) ? !value.empty? : statement?(value); end
    private_class_method :present?
    def self.safe_relative_path?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
