# frozen_string_literal: true

module Dora
  class IdeaInterview
    REQUIRED_ANSWERS = %w[target_users first_problem first_capability domain_concepts permission_intent workflow_intent forbidden_outcomes].freeze
    SOURCES = %w[user user_confirmed].freeze

    def self.validate!(document)
      fail!("idea interview must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_idea_interview" && document["version"].to_i == 1
      %w[project_id answers unanswered_decisions].each { |field| fail!("idea interview is missing #{field}") unless document.key?(field) }
      fail!("idea interview project_id is invalid") unless identifier?(document["project_id"])
      answers = validate_answers!(document["answers"])
      decisions = validate_decisions!(document["unanswered_decisions"])
      {"kind" => "dora_idea_interview", "version" => 1, "project_id" => document.fetch("project_id"), "answers" => answers, "unanswered_decisions" => decisions, "invention" => "none", "completion_boundary" => "An interview records user-provided intent only; it does not prove product design, implementation, runtime acceptance, or release readiness."}.freeze
    end

    def self.validate_answers!(answers)
      fail!("idea interview answers must be a list") unless answers.is_a?(Array)
      rows = answers.map do |answer|
        fail!("idea interview answer must be a mapping") unless answer.is_a?(Hash)
        %w[id value source].each { |field| fail!("idea interview answer is missing #{field}") unless answer.key?(field) }
        fail!("idea interview answer id is invalid") unless REQUIRED_ANSWERS.include?(answer["id"])
        fail!("idea interview answer value is invalid") unless present?(answer["value"])
        fail!("idea interview answer source is invalid") unless SOURCES.include?(answer["source"])
        answer.slice("id", "value", "source")
      end
      ids = rows.map { |answer| answer.fetch("id") }
      fail!("idea interview answer ids must be unique") unless ids.uniq.length == ids.length
      missing = REQUIRED_ANSWERS - ids
      fail!("idea interview is missing required answers: #{missing.join(", ")}") unless missing.empty?
      rows.sort_by { |answer| REQUIRED_ANSWERS.index(answer.fetch("id")) }
    end
    private_class_method :validate_answers!

    def self.validate_decisions!(decisions)
      fail!("idea interview unanswered_decisions must be a list") unless decisions.is_a?(Array)
      rows = decisions.map do |decision|
        fail!("idea interview unanswered decision must be a mapping") unless decision.is_a?(Hash) && identifier?(decision["id"]) && present?(decision["question"]) && SOURCES.include?(decision["source"])
        decision.slice("id", "question", "source")
      end
      ids = rows.map { |decision| decision.fetch("id") }
      fail!("idea interview unanswered decision ids must be unique") unless ids.uniq.length == ids.length
      rows
    end
    private_class_method :validate_decisions!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.present?(value)
      value.is_a?(Array) ? !value.empty? : value.is_a?(Hash) ? !value.empty? : value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :present?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
