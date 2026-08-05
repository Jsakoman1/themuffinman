# frozen_string_literal: true

module Dora
  class PluginReport
    COMPLETION_BOUNDARY = "Static plugin output is diagnostic evidence only and does not prove product completion, runtime acceptance, or release readiness."

    def self.build!(plugin_id:, inputs:, findings:, output:)
      fail!("plugin report id is invalid") unless plugin_id.is_a?(String) && plugin_id.match?(/\A[a-z][a-z0-9_-]*\z/)
      fail!("plugin report inputs must be a mapping") unless inputs.is_a?(Hash)
      fail!("plugin report findings must be a list") unless findings.is_a?(Array)
      fail!("plugin report output must be a mapping") unless output.is_a?(Hash) && output["kind"].to_s.match?(/\A[a-z][a-z0-9_-]*\z/)
      {"kind" => "dora_plugin_report", "version" => 1, "plugin" => plugin_id, "inputs" => inputs, "findings" => findings, "output" => output, "completion_boundary" => COMPLETION_BOUNDARY}
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
