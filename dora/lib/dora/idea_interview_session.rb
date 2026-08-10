# frozen_string_literal: true

require "yaml"
require_relative "idea_interview"
require_relative "discovery_provenance"

module Dora
  class IdeaInterviewSession
    SCHEMA_PATH = File.expand_path("../../idea-interview-session.schema.yaml", __dir__)
    QUESTIONS = {
      "target_users" => "Who will use this application first?",
      "first_problem" => "What is the first concrete problem it should solve?",
      "first_capability" => "What is the first user-visible capability?",
      "domain_concepts" => "Which domain concepts must the first capability use?",
      "permission_intent" => "Who may do what in the first capability?",
      "workflow_intent" => "Which states or steps does the first workflow need?",
      "forbidden_outcomes" => "What must this application explicitly not do?"
    }.freeze

    def self.new!(project_id:)
      validate!({"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => project_id, "answers" => [], "open_decisions" => [], "completion_boundary" => "A guided session records confirmed answers and explicit open decisions only; it does not infer product rules or prove readiness."})
    end

    def self.load!(path)
      validate!(YAML.load_file(path))
    rescue Psych::Exception => error
      fail!("idea interview session YAML is invalid: #{error.message}")
    end

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("idea interview session schema is invalid") unless schema["kind"] == "dora_idea_interview_session_schema" && schema["version"].to_i == 1
      fail!("idea interview session must be a mapping") unless document.is_a?(Hash)
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) || empty_list_field?(field, document[field]) }
      fail!("idea interview session is missing #{missing.join(", ")}") unless missing.empty?
      fail!("idea interview session kind is invalid") unless document["kind"] == "dora_idea_interview_session" && document["version"].to_i == schema["version"].to_i
      fail!("idea interview session project_id is invalid") unless identifier?(document.fetch("project_id"))
      answers = validate_answers!(document.fetch("answers"), schema)
      decisions = validate_open_decisions!(document.fetch("open_decisions"), schema)
      validate_direction_schema!(schema)
      {"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => document.fetch("project_id"), "answers" => answers, "open_decisions" => decisions, "completion_boundary" => document.fetch("completion_boundary")}.freeze
    rescue Psych::Exception => error
      fail!("idea interview session schema is invalid: #{error.message}")
    end

    def self.next_question!(session)
      validated = validate!(session)
      answered = validated.fetch("answers").map { |answer| answer.fetch("id") }
      id = IdeaInterview::REQUIRED_ANSWERS.find { |candidate| !answered.include?(candidate) }
      return nil unless id

      {"id" => id, "question" => QUESTIONS.fetch(id), "answer_policy" => "Record only a user-confirmed answer. Keep a separate unresolved concern in open_decisions instead of inventing a value."}.freeze
    end

    def self.direction_projection!(session, schema_path: SCHEMA_PATH)
      validated = validate!(session, schema_path: schema_path)
      schema = YAML.load_file(schema_path)
      validate_direction_schema!(schema)
      answered = validated.fetch("answers").map { |answer| answer.fetch("id") }
      core = schema.fetch("direction_core_answer_ids")
      conditional = schema.fetch("direction_conditional_rules").select { |rule| direction_rule_triggered?(rule, validated) }
      question_ids = core + conditional.map { |rule| rule.fetch("answer_id") }
      next_id = question_ids.find { |id| !answered.include?(id) }
      payload = {
        "core_answer_ids" => core,
        "triggered_conditional_answer_ids" => conditional.map { |rule| rule.fetch("answer_id") },
        "next_question" => next_id && direction_question(next_id),
        "complete" => next_id.nil?,
        "completion_boundary" => schema.fetch("direction_completion_boundary")
      }
      DiscoveryProvenance.advisory!(
        kind: "dora_discovery_direction",
        source_references: ["idea-interview-session.schema.yaml#direction", "idea_interview_session#confirmed_answers"],
        payload: payload
      )
    rescue Psych::Exception => error
      fail!("idea interview session schema is invalid: #{error.message}")
    end

    def self.record_answer!(session, id:, value:, source: "user_confirmed")
      validated = validate!(session)
      fail!("idea interview session answer id is invalid") unless IdeaInterview::REQUIRED_ANSWERS.include?(id)
      fail!("idea interview session answer source must be user_confirmed") unless source == "user_confirmed"
      fail!("idea interview session already has an answer for #{id}") if validated.fetch("answers").any? { |answer| answer.fetch("id") == id }
      validate!({"kind" => validated.fetch("kind"), "version" => validated.fetch("version"), "project_id" => validated.fetch("project_id"), "answers" => validated.fetch("answers") + [{"id" => id, "value" => value, "source" => source}], "open_decisions" => validated.fetch("open_decisions"), "completion_boundary" => validated.fetch("completion_boundary")})
    end

    def self.validate_answers!(answers, schema)
      fail!("idea interview session answers must be a list") unless answers.is_a?(Array)
      rows = answers.map do |answer|
        fail!("idea interview session answer must be a mapping") unless answer.is_a?(Hash)
        schema.fetch("answer_required_fields").each { |field| fail!("idea interview session answer is missing #{field}") unless present?(answer[field]) }
        fail!("idea interview session answer id is invalid") unless schema.fetch("required_answer_ids").include?(answer.fetch("id"))
        fail!("idea interview session answer source must be user_confirmed") unless answer.fetch("source") == schema.fetch("allowed_answer_source")
        answer.slice(*schema.fetch("answer_required_fields"))
      end
      ids = rows.map { |answer| answer.fetch("id") }
      fail!("idea interview session answer ids must be unique") unless ids.uniq.length == ids.length
      rows.sort_by { |answer| IdeaInterview::REQUIRED_ANSWERS.index(answer.fetch("id")) }
    end
    private_class_method :validate_answers!

    def self.validate_open_decisions!(decisions, schema)
      fail!("idea interview session open_decisions must be a list") unless decisions.is_a?(Array)
      rows = decisions.map do |decision|
        fail!("idea interview session open decision must be a mapping") unless decision.is_a?(Hash)
        schema.fetch("open_decision_required_fields").each { |field| fail!("idea interview session open decision is missing #{field}") unless present?(decision[field]) }
        fail!("idea interview session open decision id is invalid") unless identifier?(decision.fetch("id"))
        fail!("idea interview session open decision source must be user_confirmed") unless decision.fetch("source") == schema.fetch("allowed_open_decision_source")
        fail!("idea interview session open decision status must be open") unless decision.fetch("status") == "open"
        decision.slice(*schema.fetch("open_decision_required_fields"))
      end
      ids = rows.map { |decision| decision.fetch("id") }
      fail!("idea interview session open decision ids must be unique") unless ids.uniq.length == ids.length
      rows
    end
    private_class_method :validate_open_decisions!

    def self.validate_direction_schema!(schema)
      core = schema["direction_core_answer_ids"]
      fail!("idea interview direction core answer ids are invalid") unless core.is_a?(Array) && !core.empty? && core.uniq.length == core.length && core.all? { |id| schema.fetch("required_answer_ids").include?(id) }
      rules = schema["direction_conditional_rules"]
      fail!("idea interview direction conditional rules are invalid") unless rules.is_a?(Array)
      rule_ids = rules.map do |rule|
        fail!("idea interview direction conditional rule is invalid") unless rule.is_a?(Hash) && identifier?(rule["id"]) && schema.fetch("required_answer_ids").include?(rule["answer_id"]) && rule["source_answer_ids"].is_a?(Array) && !rule["source_answer_ids"].empty? && rule["source_answer_ids"].all? { |id| schema.fetch("required_answer_ids").include?(id) } && rule["trigger_terms"].is_a?(Array) && !rule["trigger_terms"].empty? && rule["trigger_terms"].all? { |term| term.is_a?(String) && !term.empty? }

        rule.fetch("id")
      end
      fail!("idea interview direction conditional rule ids must be unique") unless rule_ids.uniq.length == rule_ids.length
      conditional_answer_ids = rules.map { |rule| rule.fetch("answer_id") }
      fail!("idea interview direction questions must not duplicate core answers") unless (conditional_answer_ids & core).empty?
      fail!("idea interview direction conditional answers must be unique") unless conditional_answer_ids.uniq.length == conditional_answer_ids.length
      fail!("idea interview direction completion boundary is invalid") unless schema["direction_completion_boundary"].is_a?(String) && !schema["direction_completion_boundary"].strip.empty?
    end
    private_class_method :validate_direction_schema!

    def self.direction_rule_triggered?(rule, session)
      answers = session.fetch("answers").to_h { |answer| [answer.fetch("id"), answer.fetch("value").to_s.downcase] }
      source_text = rule.fetch("source_answer_ids").map { |id| answers.fetch(id, "") }.join(" ")
      rule.fetch("trigger_terms").any? { |term| source_text.include?(term.downcase) }
    end
    private_class_method :direction_rule_triggered?

    def self.direction_question(id)
      {"id" => id, "question" => QUESTIONS.fetch(id), "answer_policy" => "Record only a user-confirmed answer. Keep an unresolved concern in open_decisions; this direction question cannot create an owner decision or start delivery.", "scope" => "direction"}.freeze
    end
    private_class_method :direction_question

    def self.empty_list_field?(field, value)
      %w[answers open_decisions].include?(field) && value.is_a?(Array)
    end
    private_class_method :empty_list_field?

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.present?(value)
      !value.nil? && (!value.respond_to?(:empty?) || !value.empty?)
    end
    private_class_method :present?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
