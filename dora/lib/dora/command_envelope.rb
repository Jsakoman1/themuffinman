# frozen_string_literal: true

module Dora
  class CommandEnvelope
    SIDE_EFFECTS = %w[read_only local_mutation external_approval_required].freeze

    def self.success(payload:, citations: [], side_effect: "read_only")
      validate!(citations, side_effect)
      {"kind" => "dora_command_envelope", "version" => 1, "outcome" => "success", "payload" => payload, "citations" => citations, "side_effect" => side_effect, "completion_boundary" => "A successful command response is guidance or local operation evidence only; it does not prove product completion or release readiness."}.freeze
    end

    def self.error(message:, remediation:, citations: [], side_effect: "read_only")
      validate!(citations, side_effect)
      fail!("error message is required") unless statement?(message)
      fail!("error remediation is required") unless statement?(remediation)
      {"kind" => "dora_command_envelope", "version" => 1, "outcome" => "error", "error" => message, "remediation" => remediation, "citations" => citations, "side_effect" => side_effect, "completion_boundary" => "An error response is guidance only; it does not infer a repair or product completion."}.freeze
    end

    def self.validate!(citations, side_effect)
      fail!("command citations must be a list of non-empty paths") unless citations.is_a?(Array) && citations.all? { |path| statement?(path) && !path.start_with?("/") && !path.split("/").include?("..") }
      fail!("command side_effect is invalid") unless SIDE_EFFECTS.include?(side_effect)
    end
    private_class_method :validate!

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
