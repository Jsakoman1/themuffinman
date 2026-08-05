# frozen_string_literal: true

module Dora
  class VoiceEvaluation
    STAGES = %w[transcription semantic_interpretation deterministic_validation review confirmation execution].freeze
    OUTCOMES = %w[passed rejected blocked skipped].freeze
    FORBIDDEN_FIELDS = %w[api_key token secret credential execution_adapter provider].freeze

    def self.record!(fixture)
      fail!("voice evaluation fixture must be a mapping") unless fixture.is_a?(Hash)
      fail!("voice evaluation fixture kind is invalid") unless fixture["kind"] == "dora_voice_evaluation" && fixture["version"].to_i == 1
      fail!("voice evaluation fixture id is invalid") unless string?(fixture["fixture_id"])
      fail!("voice evaluation fixture contains forbidden configuration") if forbidden?(fixture)
      stages = Array(fixture["stages"])
      ids = stages.map { |stage| validate_stage!(stage); stage.fetch("id") }
      fail!("voice evaluation stages must match the safety pipeline") unless ids.sort == STAGES.sort
      {"kind" => "dora_voice_evaluation_record", "version" => 1, "fixture_id" => fixture.fetch("fixture_id"), "stages" => stages.sort_by { |stage| STAGES.index(stage.fetch("id")) }, "execution" => "not invoked", "completion_boundary" => "Voice evaluation records non-production fixture observations only; it does not prove provider behavior, product acceptance, or release readiness."}.freeze
    end

    def self.validate_stage!(stage)
      fail!("voice evaluation stage is invalid") unless stage.is_a?(Hash) && STAGES.include?(stage["id"]) && OUTCOMES.include?(stage["outcome"]) && string?(stage["observation"])
    end
    private_class_method :validate_stage!

    def self.forbidden?(value)
      case value
      when Hash then value.any? { |key, item| FORBIDDEN_FIELDS.include?(key.to_s) || forbidden?(item) }
      when Array then value.any? { |item| forbidden?(item) }
      else false
      end
    end
    private_class_method :forbidden?

    def self.string?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :string?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
