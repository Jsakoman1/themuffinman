#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/bounded_failure_diagnosis"
require "yaml"

def evidence(overrides = {})
  {"task" => "validate-control", "result" => "failed", "timedOut" => false, "exitCode" => 1, "timeoutSeconds" => 30, "output" => "TOKEN=owner-secret should never leave evidence"}.merge(overrides)
end

timeout_input = evidence("timedOut" => true, "exitCode" => nil)
before = Marshal.load(Marshal.dump(timeout_input))
timeout = Dora::BoundedFailureDiagnosis.diagnose!(evidence: timeout_input)
abort "timeout diagnosis is not advisory provenance" unless timeout.fetch("read_only") && timeout.fetch("disposition") == "advisory" && timeout.fetch("observed_at").match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && timeout.fetch("source_references") == ["work_evidence:validate-control"]
abort "timeout diagnosis did not classify bounded signature" unless timeout.fetch("failure_class") == "leaf_timeout" && timeout.fetch("cause_confidence") == "signature_matched"
abort "failure diagnosis mutated evidence" unless timeout_input == before
abort "failure diagnosis retained raw output" if timeout.to_s.include?("owner-secret") || timeout.to_s.include?("TOKEN=")
abort "failure diagnosis granted repair authority" unless timeout.fetch("completion_boundary").include?("does not retain or return output") && timeout.fetch("completion_boundary").include?("retry a command")

unavailable = Dora::BoundedFailureDiagnosis.diagnose!(evidence: evidence("exitCode" => 127))
abort "command unavailable was not classified" unless unavailable.fetch("failure_class") == "command_unavailable"
unknown = Dora::BoundedFailureDiagnosis.diagnose!(evidence: evidence("exitCode" => 2))
abort "unknown failure claimed a cause" unless unknown.fetch("failure_class") == "unknown_failure" && unknown.fetch("cause_confidence") == "unknown"

invalid = evidence("result" => "passed")
begin
  Dora::BoundedFailureDiagnosis.diagnose!(evidence: invalid)
  abort "successful evidence was diagnosed as failure"
rescue ArgumentError => error
  abort "wrong invalid-evidence rejection: #{error.message}" unless error.message.include?("invalid")
end

domain_library = YAML.load_file(File.expand_path("../docs/domain-library.yaml", __dir__))
abort "domain library omits bounded failure diagnosis" unless domain_library.fetch("vocabulary").any? { |item| item.fetch("id") == "bounded-failure-diagnosis" && item.fetch("description").include?("WorkExecution") }
abort "domain library omits diagnosis non-repair boundary" unless domain_library.fetch("invariants").any? { |item| item.fetch("id") == "bounded-failure-diagnosis-non-repair" && item.fetch("description").include?("never returns raw output") }

puts "Dora bounded failure diagnosis test passed (allow-listed signatures, unknown fallback, redaction, and no repair)."
