# frozen_string_literal: true

require "yaml"

module Dora
  class BlueprintValidator
    FORBIDDEN_DEFAULTS = /muffinman|vision|openai|api[_-]?key|secret[_-]?key|sk-[a-z0-9]/i

    def self.validate!(path)
      fixture = YAML.load_file(path)
      fail!("blueprint fixture must be a mapping") unless fixture.is_a?(Hash)
      capability = fixture.fetch("capability_blueprint")
      voice = fixture.fetch("voice_blueprint")
      serialized = YAML.dump(fixture)
      fail!("blueprint fixture includes a product-specific name or credential") if serialized.match?(FORBIDDEN_DEFAULTS)
      fail!("capability blueprint must retain service ownership") unless capability.dig("ownership", "service_owner").is_a?(String) && !capability.dig("ownership", "service_owner").empty?
      fail!("voice blueprint must make execution conditional on confirmation") unless voice.dig("execution", "rule").to_s.downcase.include?("after confirmation")
      fail!("voice blueprint must disable raw conversation memory by default") unless voice.dig("retention", "rule").to_s.downcase.include?("raw conversation memory is disabled")
      fail!("voice blueprint must require explicit confirmation") unless voice.dig("confirmation", "rule").to_s.downcase.include?("explicit confirmation")
      {"capability" => capability.fetch("capability"), "voice_kind" => voice.fetch("kind")}.freeze
    rescue KeyError => error
      fail!("blueprint fixture is incomplete: #{error.message}")
    rescue Psych::Exception => error
      fail!("blueprint fixture YAML is invalid: #{error.message}")
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
