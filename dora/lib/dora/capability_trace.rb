# frozen_string_literal: true

require "yaml"

module Dora
  class CapabilityTrace
    EVIDENCE_STATUSES = %w[recorded pending failed].freeze

    def self.load!(trace_path, project_root: File.dirname(File.expand_path(trace_path)))
      fail!("capability trace is missing") unless File.file?(trace_path)

      validate!(YAML.load_file(trace_path), project_root: project_root)
    rescue Psych::Exception => error
      fail!("capability trace YAML is invalid: #{error.message}")
    end

    def self.validate!(document, project_root:)
      fail!("capability trace must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_capability_trace" && document["version"].to_i == 1
      %w[capability work implementation_references validation_evidence runtime_evidence unresolved].each { |field| fail!("capability trace is missing #{field}") unless document.key?(field) }
      root = File.expand_path(project_root)
      capability = validate_capability!(document.fetch("capability"), root)
      work = validate_work!(document.fetch("work"), root)
      implementation = validate_references!(document.fetch("implementation_references"), root, "implementation_references")
      validation = validate_evidence!(document.fetch("validation_evidence"), root, kind: "validation", unresolved: document.fetch("unresolved"))
      runtime = validate_evidence!(document.fetch("runtime_evidence"), root, kind: "runtime", unresolved: document.fetch("unresolved"))
      unresolved = validate_unresolved!(document.fetch("unresolved"), validation + runtime)

      {
        "kind" => "dora_capability_trace", "version" => 1, "capability" => capability, "work" => work,
        "implementation_references" => implementation, "validation_evidence" => validation, "runtime_evidence" => runtime,
        "unresolved" => unresolved, "trace_state" => unresolved.empty? ? "evidence_recorded" : "unresolved",
        "completion_boundary" => "Capability trace links declared intent and evidence only; it does not prove implementation completion, runtime acceptance, or release readiness."
      }.freeze
    end

    def self.validate_capability!(capability, root)
      fail!("capability trace capability must be a mapping") unless capability.is_a?(Hash)
      fail!("capability trace capability id is invalid") unless identifier?(capability["id"])
      {
        "id" => capability.fetch("id"),
        "product_references" => validate_references!(capability["product_references"], root, "product_references"),
        "domain_references" => validate_references!(capability["domain_references"], root, "domain_references")
      }
    end
    private_class_method :validate_capability!

    def self.validate_work!(work, root)
      fail!("capability trace work must be a mapping") unless work.is_a?(Hash)
      fail!("capability trace work task is invalid") unless identifier?(work["task"])
      plan = work["plan"]
      validate_reference!(plan, root, "work plan")
      {"plan" => plan, "task" => work.fetch("task")}
    end
    private_class_method :validate_work!

    def self.validate_evidence!(entries, root, kind:, unresolved:)
      fail!("capability trace #{kind}_evidence must be a non-empty list") unless entries.is_a?(Array) && !entries.empty?
      unresolved_ids = Array(unresolved).map { |entry| entry["id"] if entry.is_a?(Hash) }.compact
      entries.map do |entry|
        fail!("capability trace #{kind} evidence must be a mapping") unless entry.is_a?(Hash)
        %w[id path status].each { |field| fail!("capability trace #{kind} evidence is missing #{field}") unless entry.key?(field) }
        fail!("capability trace #{kind} evidence id is invalid") unless identifier?(entry["id"])
        fail!("capability trace #{kind} evidence status is invalid") unless EVIDENCE_STATUSES.include?(entry["status"])
        fail!("capability trace validation evidence is missing command") if kind == "validation" && !statement?(entry["command"])
        validate_reference!(entry.fetch("path"), root, "#{kind} evidence") if entry["status"] == "recorded"
        fail!("capability trace #{kind} evidence #{entry["id"]} must be unresolved when #{entry["status"]}") if entry["status"] != "recorded" && !unresolved_ids.include?(entry["id"])
        entry.slice("id", "path", "status", "command")
      end
    end
    private_class_method :validate_evidence!

    def self.validate_unresolved!(entries, evidence)
      fail!("capability trace unresolved must be a list") unless entries.is_a?(Array)
      ids = evidence.reject { |entry| entry["status"] == "recorded" }.map { |entry| entry.fetch("id") }
      rows = entries.map do |entry|
        fail!("capability trace unresolved entry must be a mapping") unless entry.is_a?(Hash) && identifier?(entry["id"]) && statement?(entry["reason"])
        entry.slice("id", "reason")
      end
      fail!("capability trace unresolved evidence is missing") unless (ids - rows.map { |entry| entry.fetch("id") }).empty?
      rows
    end
    private_class_method :validate_unresolved!

    def self.validate_references!(references, root, label)
      fail!("capability trace #{label} must be a non-empty list") unless references.is_a?(Array) && !references.empty?
      references.each { |reference| validate_reference!(reference, root, label) }
      references
    end
    private_class_method :validate_references!

    def self.validate_reference!(reference, root, label)
      path = reference.to_s.split("#", 2).first
      fail!("capability trace #{label} reference is invalid") unless safe_relative_path?(path)
      absolute = File.expand_path(path, root)
      fail!("capability trace #{label} reference escapes project root") unless absolute.start_with?("#{root}/")
      fail!("capability trace #{label} reference is missing: #{reference}") unless File.file?(absolute)
    end
    private_class_method :validate_reference!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.safe_relative_path?(value)
      statement?(value) && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
