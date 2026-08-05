# frozen_string_literal: true

module Dora
  class RevisionEvidenceTrace
    STATUSES = %w[recorded pending failed].freeze
    def self.validate!(document)
      fail!("revision evidence trace is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_revision_evidence_trace" && document["version"].to_i == 1
      %w[capability_id revision changed_paths work validation_evidence runtime_evidence unresolved].each { |field| fail!("revision evidence trace is missing #{field}") unless document.key?(field) }
      fail!("revision evidence trace capability_id is invalid") unless identifier?(document["capability_id"])
      fail!("revision evidence trace revision is invalid") unless document["revision"].is_a?(String) && document["revision"].match?(/\A[0-9a-f]{40}\z/i)
      paths = document.fetch("changed_paths"); fail!("revision evidence trace changed_paths are invalid") unless paths.is_a?(Array) && !paths.empty? && paths.all? { |path| safe_path?(path) }
      work = document.fetch("work"); fail!("revision evidence trace work is invalid") unless work.is_a?(Hash) && safe_path?(work["plan"]) && identifier?(work["task"])
      unresolved = validate_unresolved!(document.fetch("unresolved"))
      validation = validate_evidence!(document.fetch("validation_evidence"), unresolved, "validation")
      runtime = validate_evidence!(document.fetch("runtime_evidence"), unresolved, "runtime")
      {"kind" => "dora_revision_evidence_trace", "version" => 1, "capability_id" => document.fetch("capability_id"), "revision" => document.fetch("revision").downcase, "changed_paths" => paths, "work" => work.slice("plan", "task"), "validation_evidence" => validation, "runtime_evidence" => runtime, "unresolved" => unresolved, "trace_state" => unresolved.empty? ? "evidence_recorded" : "unresolved", "completion_boundary" => "A revision evidence trace links declared change and evidence records; it does not prove acceptance or release readiness."}.freeze
    end
    def self.validate_evidence!(entries, unresolved, kind)
      fail!("revision evidence trace #{kind} evidence is invalid") unless entries.is_a?(Array) && !entries.empty?
      entries.map do |entry|
        fail!("revision evidence trace #{kind} evidence is invalid") unless entry.is_a?(Hash) && identifier?(entry["id"]) && safe_path?(entry["path"]) && STATUSES.include?(entry["status"])
        fail!("revision evidence trace validation command is missing") if kind == "validation" && !statement?(entry["command"])
        fail!("revision evidence trace #{kind} evidence #{entry["id"]} must be unresolved") if entry["status"] != "recorded" && !unresolved.any? { |item| item.fetch("id") == entry.fetch("id") }
        entry.slice("id", "path", "status", "command")
      end
    end
    private_class_method :validate_evidence!
    def self.validate_unresolved!(entries); fail!("revision evidence trace unresolved is invalid") unless entries.is_a?(Array); entries.map { |item| fail!("revision evidence trace unresolved item is invalid") unless item.is_a?(Hash) && identifier?(item["id"]) && statement?(item["reason"]); item.slice("id", "reason") }; end
    private_class_method :validate_unresolved!
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.safe_path?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
