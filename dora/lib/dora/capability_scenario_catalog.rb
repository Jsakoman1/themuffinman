# frozen_string_literal: true

require "time"
require "yaml"

module Dora
  # Validates project-declared scenario obligations. It intentionally does not
  # execute a scenario or retain evidence state; those facts belong to the
  # existing capability proof and trace artifacts.
  class CapabilityScenarioCatalog
    SCHEMA_PATH = File.expand_path("../../capability-proof-matrix.schema.yaml", __dir__)
    SCENARIO_CLASSES = %w[happy_path permission recovery regression].freeze

    def self.build!(document, proof_schema_path: SCHEMA_PATH)
      proof_schema = YAML.load_file(proof_schema_path)
      fail!("capability proof matrix schema is invalid") unless proof_schema["kind"] == "dora_capability_proof_matrix_schema" && proof_schema["version"].to_i == 1
      fail!("capability scenario catalog is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_capability_scenario_catalog" && document["version"].to_i == 1
      fail!("capability scenario catalog is missing required fields") unless document.keys.sort == %w[capability kind scenarios source_references version]
      fail!("capability scenario catalog capability is invalid") unless identifier?(document["capability"])

      sources = validate_references!(document.fetch("source_references"), "source_references")
      scenarios = validate_scenarios!(document.fetch("scenarios"), proof_schema.fetch("evidence_classes"))
      missing_classes = SCENARIO_CLASSES - scenarios.map { |scenario| scenario.fetch("class") }.uniq
      fail!("capability scenario catalog is missing classes #{missing_classes.join(", ")}") unless missing_classes.empty?

      {
        "kind" => "dora_capability_scenario_catalog_result",
        "version" => 1,
        "observed_at" => Time.now.utc.iso8601,
        "source_references" => sources,
        "read_only" => true,
        "disposition" => "advisory",
        "capability" => document.fetch("capability"),
        "scenarios" => scenarios,
        "completion_boundary" => "This catalog declares reusable scenario obligations only; it cannot execute a scenario, record evidence, change a capability or work status, create a decision, invoke GitHub, mutate a consumer project, or start a runner or remote agent."
      }.freeze
    rescue Psych::Exception => error
      fail!("capability scenario catalog YAML is invalid: #{error.message}")
    end

    def self.validate_scenarios!(scenarios, evidence_classes)
      fail!("capability scenario catalog scenarios must be a non-empty list") unless scenarios.is_a?(Array) && !scenarios.empty?

      normalized = scenarios.map do |scenario|
        fail!("capability scenario is invalid") unless scenario.is_a?(Hash) && scenario.keys.sort == %w[class evidence_class expected id source_references]
        fail!("capability scenario id is invalid") unless identifier?(scenario["id"])
        fail!("capability scenario class is invalid") unless SCENARIO_CLASSES.include?(scenario["class"])
        fail!("capability scenario evidence class is invalid") unless evidence_classes.include?(scenario["evidence_class"])
        fail!("capability scenario expected result is invalid") unless statement?(scenario["expected"])

        scenario.slice("id", "class", "evidence_class", "expected").merge("source_references" => validate_references!(scenario.fetch("source_references"), "scenario source_references"))
      end
      fail!("capability scenario ids must be unique") unless normalized.map { |scenario| scenario.fetch("id") }.uniq.length == normalized.length

      normalized.sort_by { |scenario| scenario.fetch("id") }
    end
    private_class_method :validate_scenarios!

    def self.validate_references!(references, label)
      fail!("capability scenario catalog #{label} must be a non-empty list") unless references.is_a?(Array) && !references.empty? && references.all? { |reference| safe_reference?(reference) }

      references.uniq.sort
    end
    private_class_method :validate_references!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty? && !value.match?(/[\r\n]/)
    end
    private_class_method :statement?

    def self.safe_reference?(value)
      statement?(value) && !value.start_with?("/") && !value.split("#", 2).first.split("/").include?("..")
    end
    private_class_method :safe_reference?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
