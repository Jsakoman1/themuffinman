# frozen_string_literal: true

require "time"

module Dora
  # Produces a deliberately small explanation from already-recorded leaf evidence.
  # It never reads, classifies, or returns terminal output, and cannot retry work.
  class BoundedFailureDiagnosis
    KNOWN_FAILURES = {
      "leaf_timeout" => "Review the declared leaf-command timeout and its local prerequisite before an owner chooses a follow-up task.",
      "command_unavailable" => "Review the declared command availability and project prerequisite before an owner chooses a follow-up task.",
      "unknown_failure" => "Inspect the existing redacted evidence through the normal owner workflow; Dora does not infer a cause or repair."
    }.freeze

    def self.diagnose!(evidence:)
      validate_evidence!(evidence)
      failure_class = if evidence.fetch("timedOut")
                        "leaf_timeout"
                      elsif evidence.fetch("exitCode") == 127
                        "command_unavailable"
                      else
                        "unknown_failure"
                      end
      {
        "kind" => "dora_bounded_failure_diagnosis",
        "version" => 1,
        "observed_at" => Time.now.utc.iso8601,
        "source_references" => ["work_evidence:#{evidence.fetch("task")}"],
        "read_only" => true,
        "disposition" => "advisory",
        "failure_class" => failure_class,
        "cause_confidence" => failure_class == "unknown_failure" ? "unknown" : "signature_matched",
        "next_safe_step" => KNOWN_FAILURES.fetch(failure_class),
        "completion_boundary" => "This diagnosis classifies allow-listed evidence fields only. It does not retain or return output, retry a command, invoke a shell, repair a project, mutate evidence, work status, or decisions, invoke GitHub, mutate a consumer project, or start a runner or remote agent."
      }.freeze
    end

    def self.validate_evidence!(evidence)
      fail!("failure evidence is invalid") unless evidence.is_a?(Hash) && evidence["result"] == "failed"
      fail!("failure evidence task is invalid") unless evidence["task"].is_a?(String) && evidence["task"].match?(/\A[a-z][a-z0-9-]*\z/)
      fail!("failure evidence timedOut is invalid") unless [true, false].include?(evidence["timedOut"])
      fail!("failure evidence exitCode is invalid") unless evidence["exitCode"].nil? || evidence["exitCode"].is_a?(Integer)
      fail!("failure evidence timeoutSeconds is invalid") unless evidence["timeoutSeconds"].is_a?(Integer) && evidence["timeoutSeconds"].positive?
    end
    private_class_method :validate_evidence!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
