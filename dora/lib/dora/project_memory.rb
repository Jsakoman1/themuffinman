# frozen_string_literal: true

require "yaml"

module Dora
  class ProjectMemory
    REQUIRED_FIELDS = %w[project_intent canonical_knowledge open_decisions capability_intent current_work].freeze
    CURRENT_WORK_STATES = %w[planned active blocked].freeze

    def self.load!(path)
      fail!("project memory file is missing") unless File.file?(path)

      validate!(YAML.load_file(path))
    end

    def self.validate!(document)
      fail!("project memory must be a mapping") unless document.is_a?(Hash)
      fail!("project memory kind is invalid") unless document["kind"] == "dora_project_memory" && document["version"].to_i == 1
      missing = REQUIRED_FIELDS.reject { |field| document.key?(field) }
      fail!("project memory is missing #{missing.join(", ")}") unless missing.empty?

      project_intent = validate_project_intent!(document.fetch("project_intent"))
      knowledge = validate_knowledge!(document.fetch("canonical_knowledge"))
      decisions = validate_decisions!(document.fetch("open_decisions"))
      capabilities = validate_capabilities!(document.fetch("capability_intent"), knowledge)
      current_work = validate_current_work!(document.fetch("current_work"))

      {
        "kind" => "dora_project_memory",
        "version" => 1,
        "project_intent" => project_intent,
        "canonical_knowledge" => knowledge,
        "open_decisions" => decisions,
        "capability_intent" => capabilities,
        "current_work" => current_work,
        "completion_boundary" => "Project memory is declared navigation context only and does not prove implementation, runtime acceptance, or release readiness."
      }.freeze
    end

    def self.validate_project_intent!(intent)
      fail!("project memory project_intent must be a mapping") unless intent.is_a?(Hash)
      %w[product_brief domain_library].each do |field|
        fail!("project memory project_intent #{field} is invalid") unless safe_relative_path?(intent[field])
      end

      intent.slice("product_brief", "domain_library")
    end
    private_class_method :validate_project_intent!

    def self.validate_knowledge!(entries)
      fail!("project memory canonical_knowledge must be a non-empty list") unless entries.is_a?(Array) && !entries.empty?

      entries.map do |entry|
        fail!("project memory knowledge entry must be a mapping") unless entry.is_a?(Hash)
        fail!("project memory knowledge id is invalid") unless identifier?(entry["id"])
        fail!("project memory knowledge path is invalid") unless safe_relative_path?(entry["path"])
        fail!("project memory knowledge purpose is invalid") unless statement?(entry["purpose"])

        entry.slice("id", "path", "purpose")
      end
    end
    private_class_method :validate_knowledge!

    def self.validate_decisions!(entries)
      fail!("project memory open_decisions must be a list") unless entries.is_a?(Array)

      entries.map do |entry|
        fail!("project memory open decision must be a mapping") unless entry.is_a?(Hash)
        fail!("project memory open decision id is invalid") unless identifier?(entry["id"])
        fail!("project memory open decision statement is invalid") unless statement?(entry["statement"])
        fail!("project memory open decision source is invalid") unless safe_relative_path?(entry["source"])

        entry.slice("id", "statement", "source")
      end
    end
    private_class_method :validate_decisions!

    def self.validate_capabilities!(entries, knowledge)
      fail!("project memory capability_intent must be a list") unless entries.is_a?(Array)
      knowledge_ids = knowledge.map { |entry| entry.fetch("id") }

      entries.map do |entry|
        fail!("project memory capability intent must be a mapping") unless entry.is_a?(Hash)
        fail!("project memory capability id is invalid") unless identifier?(entry["id"])
        fail!("project memory capability intended_outcome is invalid") unless statement?(entry["intended_outcome"])
        references = entry["knowledge_references"]
        fail!("project memory capability knowledge_references are invalid") unless references.is_a?(Array) && !references.empty? && references.all? { |reference| knowledge_ids.include?(reference) }

        entry.slice("id", "intended_outcome").merge("knowledge_references" => references)
      end
    end
    private_class_method :validate_capabilities!

    def self.validate_current_work!(work)
      fail!("project memory current_work must be a mapping") unless work.is_a?(Hash)
      fail!("project memory current_work plan is invalid") unless safe_relative_path?(work["plan"])
      fail!("project memory current_work task is invalid") unless identifier?(work["task"])
      fail!("project memory current_work state is invalid") unless CURRENT_WORK_STATES.include?(work["state"])

      work.slice("plan", "task", "state")
    end
    private_class_method :validate_current_work!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.safe_relative_path?(path)
      statement?(path) && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
