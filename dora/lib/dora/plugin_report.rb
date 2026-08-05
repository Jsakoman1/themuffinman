# frozen_string_literal: true

module Dora
  class PluginReport
    COMPLETION_BOUNDARY = "Static plugin output is diagnostic evidence only and does not prove product completion, runtime acceptance, or release readiness."

    def self.build!(plugin_id:, inputs:, findings:, output:, finding_context: {}, execution_boundary: {}, read_boundary: {})
      fail!("plugin report id is invalid") unless plugin_id.is_a?(String) && plugin_id.match?(/\A[a-z][a-z0-9_-]*\z/)
      fail!("plugin report inputs must be a mapping") unless inputs.is_a?(Hash)
      fail!("plugin report findings must be a list") unless findings.is_a?(Array)
      fail!("plugin report output must be a mapping") unless output.is_a?(Hash) && output["kind"].to_s.match?(/\A[a-z][a-z0-9_-]*\z/)
      fail!("plugin report finding context must be a mapping") unless finding_context.is_a?(Hash)
      fail!("plugin report execution boundary must be a mapping") unless execution_boundary.is_a?(Hash)
      fail!("plugin report read boundary must be a mapping") unless read_boundary.is_a?(Hash)
      {"kind" => "dora_plugin_report", "version" => 1, "plugin" => plugin_id, "inputs" => inputs, "findings" => findings.each_with_index.map { |finding, index| standard_finding(plugin_id, finding, index, finding_context) }, "output" => output, "execution_boundary" => execution_boundary, "read_boundary" => read_boundary, "finding_contract" => "dora_finding", "completion_boundary" => COMPLETION_BOUNDARY}
    end

    def self.standard_finding(plugin_id, finding, index, context)
      raw = finding.is_a?(Hash) ? finding : {"value" => finding}
      id = raw["id"].to_s.gsub(/[^a-z0-9-]/, "-").sub(/\A-+/, "").sub(/-+\z/, "")
      id = "finding-#{index + 1}" if id.empty?
      severity = %w[info warning error].include?(raw["severity"]) ? raw["severity"] : "info"
      location = raw["location"].is_a?(Hash) && raw["location"]["path"].is_a?(String) ? raw["location"] : {"path" => Array(context["source_roots"]).first || "declared-plugin-input"}
      explanation = raw["explanation"].is_a?(String) && !raw["explanation"].empty? ? raw["explanation"] : "Declared plugin #{plugin_id} produced diagnostic finding #{id}."
      repair = raw["repair"].is_a?(String) && !raw["repair"].empty? ? raw["repair"] : "Review the declared plugin evidence before changing project work."
      evidence = raw["evidence"].is_a?(Array) && !raw["evidence"].empty? ? raw["evidence"] : ["plugin:#{plugin_id}"]
      raw.merge("kind" => "dora_finding", "version" => 1, "id" => "#{plugin_id}-#{id}", "severity" => severity, "location" => location, "explanation" => explanation, "repair" => repair, "evidence" => evidence, "details" => raw, "diagnostic_boundary" => "A finding is diagnostic evidence only and does not prove product completion, runtime acceptance, or release readiness.").freeze
    end
    private_class_method :standard_finding

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
