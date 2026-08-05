# frozen_string_literal: true

require "yaml"
require_relative "idea_interview_session"
require_relative "idea_interview"

module Dora
  class SessionCreateApp
    SCHEMA_PATH = File.expand_path("../../session-create-app.schema.yaml", __dir__)

    def self.convert!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("session create-app schema is invalid") unless schema["kind"] == "dora_session_create_app_schema" && schema["version"].to_i == 1
      fail!("session create-app input must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_session_create_app" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| document.key?(field) }
      fail!("session create-app input is missing #{missing.join(", ")}") unless missing.empty?
      session = IdeaInterviewSession.validate!(document.fetch("session"))
      fail!("session create-app requires a complete session") if IdeaInterviewSession.next_question!(session)
      answers = session.fetch("answers").to_h { |answer| [answer.fetch("id"), answer.fetch("value")] }
      interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => session.fetch("project_id"), "answers" => session.fetch("answers").map { |answer| answer.merge("source" => "user_confirmed") }, "unanswered_decisions" => session.fetch("open_decisions").map { |decision| {"id" => decision.fetch("id"), "question" => decision.fetch("question"), "source" => "user_confirmed"} }}
      IdeaInterview.validate!(interview)
      first_work = document.fetch("first_work")
      fail!("session create-app first_work must be declared") unless first_work.is_a?(Hash)
      {"kind" => "dora_create_app", "version" => 1, "interview" => interview, "dora_source" => document.fetch("dora_source"), "first_capability" => {"id" => "first-capability", "title" => answers.fetch("first_capability").to_s, "interview_answer" => "first_capability"}, "first_work" => first_work, "starter" => document["starter"], "codex_integration" => document.fetch("codex_integration", false), "conversion_boundary" => "The session conversion preserves confirmed answers and open decisions only; it does not infer implementation, entities, permissions, or workflows."}.compact.freeze
    rescue Psych::Exception => error
      fail!("session create-app YAML is invalid: #{error.message}")
    end

    def self.preview!(document)
      bundle = convert!(document)
      {"project_id" => bundle.dig("interview", "project_id"), "first_capability" => bundle.fetch("first_capability"), "open_decisions" => bundle.dig("interview", "unanswered_decisions")}.freeze
    end
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
