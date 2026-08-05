# frozen_string_literal: true

module Dora
  class ApiWorkflowSafety
    REQUIRED_REFERENCES = %w[dto_id api_id client_id permission_id workflow_id].freeze

    def self.review!(document)
      fail!("API workflow safety input is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_api_workflow_safety_input" && document["version"].to_i == 1
      operations = Array(document["operations"])
      fail!("API workflow operations must be declared") unless !operations.empty? && operations.all? { |operation| operation.is_a?(Hash) && identifier?(operation["id"]) }
      findings = operations.flat_map { |operation| findings_for(operation) }
      {"kind" => "dora_api_workflow_safety_report", "version" => 1, "findings" => findings, "safe_to_implement" => findings.empty?, "completion_boundary" => "This static report does not generate clients, change API contracts, modify permissions, or prove runtime behavior."}.freeze
    end

    def self.findings_for(operation)
      id = operation.fetch("id")
      findings = REQUIRED_REFERENCES.each_with_object([]) do |reference, result|
        result << finding("missing_#{reference}", id, "#{reference} is not explicitly declared.") unless identifier?(operation[reference])
      end
      findings << finding("permission_confirmation_missing", id, "Permission confirmation is not explicit.") unless operation["permission_confirmed"] == true
      api_statuses = Array(operation["api_statuses"])
      workflow_statuses = Array(operation["workflow_statuses"])
      invalid_statuses = api_statuses - workflow_statuses
      findings << finding("api_workflow_status_mismatch", id, "API statuses are not declared by the workflow: #{invalid_statuses.join(", ")}.") unless invalid_statuses.empty?
      findings
    end
    private_class_method :findings_for

    def self.finding(id, operation_id, message); {"id" => id, "operation_id" => operation_id, "message" => message}; end
    private_class_method :finding
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
