# frozen_string_literal: true

module Dora
  class FindingExport
    LEVELS = {"error" => "error", "warning" => "warning", "info" => "notice"}.freeze
    DIAGNOSTIC_BOUNDARY = "Exported annotations are diagnostic evidence only and do not prove product completion, runtime acceptance, or release readiness."

    def self.export!(findings)
      fail!("findings must be a non-empty list") unless findings.is_a?(Array) && !findings.empty?
      annotations = findings.map { |finding| annotation!(finding) }
      {"kind" => "dora_finding_export", "version" => 1, "read_only" => true, "annotations" => annotations, "diagnostic_boundary" => DIAGNOSTIC_BOUNDARY}.freeze
    end

    def self.annotation!(finding)
      required = %w[id severity location explanation repair evidence]
      fail!("finding is incomplete for export") unless finding.is_a?(Hash) && required.all? { |field| finding.key?(field) }
      level = LEVELS[finding["severity"]]
      fail!("finding severity is invalid for export") unless level
      location = finding.fetch("location")
      fail!("finding export location is invalid") unless location.is_a?(Hash) && location["path"].is_a?(String) && !location["path"].empty?
      {"level" => level, "location" => location.slice("path", "line"), "message" => finding.fetch("explanation"), "repair" => finding.fetch("repair"), "properties" => {"id" => finding.fetch("id"), "evidence" => finding.fetch("evidence")}}
    end
    private_class_method :annotation!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
