# frozen_string_literal: true

require "yaml"
require_relative "idea_interview_session"

module Dora
  class GuidedAgentEntrypoint
    SCHEMA_PATH = File.expand_path("../../guided-agent-entrypoint.schema.yaml", __dir__)

    def self.resolve!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("guided entrypoint schema is invalid") unless schema["kind"] == "dora_guided_agent_entrypoint_schema" && schema["version"].to_i == 1
      fail!("guided entrypoint must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_guided_agent_entrypoint" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| document.key?(field) }
      fail!("guided entrypoint is missing #{missing.join(", ")}") unless missing.empty?

      session = IdeaInterviewSession.validate!(document.fetch("session"))
      handoff = validate_handoff!(document.fetch("handoff"), schema)
      question = IdeaInterviewSession.next_question!(session)
      payload = if question
                  {"resolution" => "ask_one_confirmed_question", "question" => question}
                else
                  {"resolution" => "review_create_app_handoff", "handoff" => handoff}
                end
      {"kind" => "dora_guided_agent_entrypoint_result", "version" => 1, "project_id" => session.fetch("project_id"), "open_decisions" => session.fetch("open_decisions"), "citations" => [{"id" => "session", "reason" => "confirmed answers and explicit open decisions"}, {"id" => "handoff", "reason" => "declared review-only project handoff"}], "completion_boundary" => "The guided entrypoint selects one declared next interaction or review-only handoff; it does not infer product rules, create source code, start work, or prove readiness."}.merge(payload).freeze
    rescue Psych::Exception => error
      fail!("guided entrypoint YAML is invalid: #{error.message}")
    end

    def self.validate_handoff!(handoff, schema)
      fail!("guided entrypoint handoff must be a mapping") unless handoff.is_a?(Hash)
      missing = schema.fetch("handoff_required_fields").reject { |field| handoff.key?(field) }
      fail!("guided entrypoint handoff is missing #{missing.join(", ")}") unless missing.empty?
      %w[dora_source first_work starter].each { |field| fail!("guided entrypoint handoff #{field} is invalid") unless statement?(handoff[field]) }
      fail!("guided entrypoint handoff codex_integration must be boolean") unless [true, false].include?(handoff["codex_integration"])
      handoff.slice(*schema.fetch("handoff_required_fields")).freeze
    end
    private_class_method :validate_handoff!

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
