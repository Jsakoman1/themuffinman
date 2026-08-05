# frozen_string_literal: true

module Dora
  class CapabilityPackage
    EVIDENCE_STATUSES = %w[declared recorded unresolved].freeze

    def self.validate!(document)
      fail!("capability package must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_capability_package" && document["version"].to_i == 1
      %w[id title intent domain api tests runtime work unresolved].each { |field| fail!("capability package is missing #{field}") unless document.key?(field) }
      fail!("capability package id is invalid") unless identifier?(document["id"])
      fail!("capability package title is invalid") unless statement?(document["title"])
      unresolved = validate_unresolved!(document.fetch("unresolved"))
      result = {"kind" => "dora_capability_package", "version" => 1, "id" => document.fetch("id"), "title" => document.fetch("title"), "intent" => validate_intent!(document.fetch("intent")), "domain" => validate_domain!(document.fetch("domain")), "api" => validate_api!(document.fetch("api")), "tests" => validate_evidence!(document.fetch("tests"), "tests", unresolved), "runtime" => validate_evidence!(document.fetch("runtime"), "runtime", unresolved), "work" => validate_work!(document.fetch("work")), "unresolved" => unresolved, "invention" => "none", "completion_boundary" => "A capability package declares implementation inputs and evidence intent only; it does not prove implementation, runtime acceptance, or release readiness."}
      result.freeze
    end

    def self.validate_intent!(intent)
      fail!("capability package intent must be a mapping") unless intent.is_a?(Hash)
      %w[problem capability source_reference].each { |field| fail!("capability package intent is missing #{field}") unless statement?(intent[field]) }
      intent.slice("problem", "capability", "source_reference")
    end
    private_class_method :validate_intent!

    def self.validate_domain!(domain)
      fail!("capability package domain must be a mapping") unless domain.is_a?(Hash)
      %w[entity_ids invariant_ids permission_rule_ids workflow_id].each { |field| fail!("capability package domain is missing #{field}") unless domain.key?(field) }
      %w[entity_ids invariant_ids permission_rule_ids].each do |field|
        values = domain.fetch(field)
        fail!("capability package domain #{field} must be a non-empty list of identifiers") unless values.is_a?(Array) && !values.empty? && values.all? { |value| identifier?(value) }
      end
      fail!("capability package domain workflow_id is invalid") unless identifier?(domain.fetch("workflow_id"))
      domain.slice("entity_ids", "invariant_ids", "permission_rule_ids", "workflow_id")
    end
    private_class_method :validate_domain!

    def self.validate_api!(api)
      fail!("capability package api must be a mapping") unless api.is_a?(Hash) && statement?(api["service_owner"]) && api["operations"].is_a?(Array) && !api["operations"].empty?
      operations = api.fetch("operations").map do |operation|
        fail!("capability package api operation is invalid") unless operation.is_a?(Hash) && identifier?(operation["id"]) && statement?(operation["purpose"])
        operation.slice("id", "purpose")
      end
      fail!("capability package api operation ids must be unique") unless operations.map { |operation| operation.fetch("id") }.uniq.length == operations.length
      {"service_owner" => api.fetch("service_owner"), "operations" => operations}
    end
    private_class_method :validate_api!

    def self.validate_evidence!(section, label, unresolved)
      fail!("capability package #{label} must be a mapping") unless section.is_a?(Hash) && section["scenarios"].is_a?(Array) && !section["scenarios"].empty?
      scenarios = section.fetch("scenarios").map do |scenario|
        fail!("capability package #{label} scenario is invalid") unless scenario.is_a?(Hash) && identifier?(scenario["id"]) && statement?(scenario["action"]) && statement?(scenario["expected"]) && EVIDENCE_STATUSES.include?(scenario["status"])
        fail!("capability package #{label} scenario #{scenario["id"]} must be unresolved") if scenario["status"] != "recorded" && !unresolved.any? { |item| item.fetch("id") == scenario.fetch("id") }
        scenario.slice("id", "action", "expected", "status")
      end
      fail!("capability package #{label} scenario ids must be unique") unless scenarios.map { |scenario| scenario.fetch("id") }.uniq.length == scenarios.length
      {"scenarios" => scenarios}
    end
    private_class_method :validate_evidence!

    def self.validate_work!(work)
      fail!("capability package work must be a mapping") unless work.is_a?(Hash) && safe_relative_path?(work["plan"]) && identifier?(work["task"])
      work.slice("plan", "task")
    end
    private_class_method :validate_work!

    def self.validate_unresolved!(unresolved)
      fail!("capability package unresolved must be a list") unless unresolved.is_a?(Array)
      rows = unresolved.map do |item|
        fail!("capability package unresolved item is invalid") unless item.is_a?(Hash) && identifier?(item["id"]) && statement?(item["reason"])
        item.slice("id", "reason")
      end
      fail!("capability package unresolved ids must be unique") unless rows.map { |item| item.fetch("id") }.uniq.length == rows.length
      rows
    end
    private_class_method :validate_unresolved!

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.safe_relative_path?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
