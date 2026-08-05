# frozen_string_literal: true

module Dora
  class Finding
    DIAGNOSTIC_BOUNDARY = "A finding is diagnostic evidence only and does not prove product completion, runtime acceptance, or release readiness."
    SEVERITIES = %w[info warning error].freeze

    def self.build!(id:, severity:, location:, explanation:, repair:, evidence:)
      fail!("finding id is invalid") unless string?(id)
      fail!("finding severity is invalid") unless SEVERITIES.include?(severity)
      fail!("finding location must be a mapping") unless location.is_a?(Hash) && string?(location["path"])
      fail!("finding explanation is invalid") unless string?(explanation)
      fail!("finding repair is invalid") unless string?(repair)
      fail!("finding evidence must be a non-empty list") unless evidence.is_a?(Array) && !evidence.empty? && evidence.all? { |item| string?(item) }

      {"kind" => "dora_finding", "version" => 1, "id" => id, "severity" => severity, "location" => location, "explanation" => explanation, "repair" => repair, "evidence" => evidence, "diagnostic_boundary" => DIAGNOSTIC_BOUNDARY}.freeze
    end

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
