# frozen_string_literal: true

require_relative "capability_package"

module Dora
  class DomainCompiler
    def self.compile!(capability:, domain:)
      package = CapabilityPackage.validate!(capability)
      fail!("domain compiler domain must be a mapping") unless domain.is_a?(Hash)
      findings = []
      add_missing_reference_findings!(findings, package.fetch("domain").fetch("entity_ids"), identifiers(domain["entities"]), "entity")
      add_missing_reference_findings!(findings, package.fetch("domain").fetch("invariant_ids"), identifiers(domain["invariants"]), "invariant")
      add_missing_reference_findings!(findings, package.fetch("domain").fetch("permission_rule_ids"), identifiers(domain["permission_rules"]), "permission_rule")
      add_missing_reference_findings!(findings, [package.fetch("domain").fetch("workflow_id")], identifiers(domain["workflows"]), "workflow")
      add_evidence_findings!(findings, package.fetch("tests").fetch("scenarios"), "test")
      add_evidence_findings!(findings, package.fetch("runtime").fetch("scenarios"), "runtime")
      findings.sort_by! { |finding| [finding.fetch("category"), finding.fetch("id")] }
      {"kind" => "dora_domain_compilation", "version" => 1, "capability_id" => package.fetch("id"), "consistent" => findings.empty?, "findings" => findings, "recommended_next_action" => findings.empty? ? "Define or run the next declared atomic work item." : "Resolve the first declared finding explicitly before implementation.", "invention" => "none", "completion_boundary" => "Domain compilation compares declared references only; it does not repair rules, infer permission, prove evidence, or prove implementation readiness."}.freeze
    end

    def self.add_missing_reference_findings!(findings, declared, available, category)
      declared.each do |id|
        next if available.include?(id)

        findings << {"id" => "missing-#{category.tr('_', '-')}-#{id}", "category" => category, "severity" => "blocking", "message" => "Declared #{category.tr('_', ' ')} #{id} is missing from the domain library.", "declared_reference" => id}
      end
    end
    private_class_method :add_missing_reference_findings!

    def self.add_evidence_findings!(findings, scenarios, category)
      scenarios.each do |scenario|
        next if scenario.fetch("status") == "recorded"

        findings << {"id" => "missing-#{category}-evidence-#{scenario.fetch("id")}", "category" => "#{category}_evidence", "severity" => "blocking", "message" => "Declared #{category} scenario #{scenario.fetch("id")} has status #{scenario.fetch("status")} and no recorded evidence.", "declared_reference" => scenario.fetch("id")}
      end
    end
    private_class_method :add_evidence_findings!

    def self.identifiers(rows)
      Array(rows).map { |row| row.is_a?(Hash) && row["id"].is_a?(String) ? row.fetch("id") : nil }.compact
    end
    private_class_method :identifiers

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
