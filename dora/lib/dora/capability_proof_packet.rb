# frozen_string_literal: true

require "yaml"

module Dora
  class CapabilityProofPacket
    SCHEMA_PATH = File.expand_path("../../capability-proof-matrix.schema.yaml", __dir__)

    def self.build!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("capability proof matrix schema is invalid") unless schema["kind"] == "dora_capability_proof_matrix_schema" && schema["version"].to_i == 1
      fail!("capability proof matrix is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_capability_proof_matrix" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| document.key?(field) }
      fail!("capability proof matrix is missing #{missing.join(", ")}") unless missing.empty?
      assertions = Array(document.fetch("assertions"))
      fail!("capability proof matrix assertions are invalid") unless assertions.any? && assertions.all? { |row| row.is_a?(Hash) && schema.fetch("assertion_required_fields").all? { |field| row.key?(field) } && identifier?(row["id"]) && statement?(row["statement"]) && row["confirmed"] == true }
      ids = assertions.map { |row| row.fetch("id") }; fail!("capability proof assertion ids must be unique") unless ids.uniq.length == ids.length
      obligations = Array(document.fetch("obligations")).map do |row|
        fail!("capability proof obligation is invalid") unless row.is_a?(Hash) && schema.fetch("obligation_required_fields").all? { |field| row.key?(field) }
        fail!("capability proof obligation assertion is unknown") unless ids.include?(row.fetch("assertion_id"))
        fail!("capability proof obligation evidence class is invalid") unless schema.fetch("evidence_classes").include?(row.fetch("evidence_class"))
        fail!("capability proof obligation status is invalid") unless schema.fetch("statuses").include?(row.fetch("status"))
        fail!("capability proof obligation boundary is invalid") unless identifier?(row.fetch("id")) && statement?(row.fetch("boundary"))
        row.slice(*schema.fetch("obligation_required_fields"))
      end
      fail!("capability proof matrix obligations are invalid") if obligations.empty?
      unresolved = obligations.select { |row| row.fetch("status") == "unresolved" }
      {"kind" => "dora_capability_proof_packet", "version" => 1, "capability" => document.fetch("capability"), "assertions" => assertions.map { |row| row.slice(*schema.fetch("assertion_required_fields")) }, "obligations" => obligations.sort_by { |row| row.fetch("id") }, "approval_gates" => unresolved.select { |row| %w[browser_runtime accessibility security acceptance].include?(row.fetch("evidence_class")) }.map { |row| {"obligation_id" => row.fetch("id"), "reason" => row.fetch("boundary")} }, "completion_boundary" => "A proof packet declares project-owned evidence obligations; it does not execute a command, approve an external action, or prove a capability complete."}.freeze
    rescue Psych::Exception => error
      fail!("capability proof matrix YAML is invalid: #{error.message}")
    end

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
