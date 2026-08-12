# frozen_string_literal: true

require "digest"
require "time"
require "yaml"

require_relative "adapter"
require_relative "decision_log"
require_relative "project_read_model"

module Dora
  class IdcEnvelope
    SAFE_PROJECT_FIELDS = %w[project state health integrity delivery current_goal next_task open_decisions references].freeze
    DECISION_STATUSES = %w[accepted proposed].freeze
    SELECTION_KIND = "dora_idc_envelope_selection"

    def self.export!(adapter_path:, selection:, observed_at: Time.now.utc.iso8601)
      context = Adapter.load_context!(adapter_path, ProjectReadModel::SCHEMA_PATH)
      timestamp = utc_time!(observed_at)
      summary = ProjectReadModel.load!(adapter_path: adapter_path).summary
      normalized = validate_selection!(selection, summary: summary, root: context.root)
      decisions = selected_decisions!(root: context.root, decision_ids: normalized.fetch("decision_ids"))
      artifacts = selected_artifacts!(root: context.root, references: normalized.fetch("artifact_references"))
      projection = summary.slice(*normalized.fetch("project_fields"))
      envelope = {
        "kind" => "dora_idc_read_envelope",
        "version" => 1,
        "observed_at" => timestamp,
        "read_only" => true,
        "disposition" => "advisory",
        "project" => summary.fetch("project").slice("id", "name"),
        "selection" => normalized,
        "project_read_model" => projection,
        "selected_decisions" => decisions,
        "artifact_references" => artifacts,
        "sources" => source_provenance(projection: projection, decisions: decisions, artifacts: artifacts, observed_at: timestamp),
        "completion_boundary" => "This is a sanitized, owner-selected, read-only Dora snapshot. It does not expose raw source, ProjectMemory contents, secrets, absolute paths, filesystem search, or write authority; it cannot create or amend decisions, plans, evidence, verified status, Git state, or Codex execution."
      }
      envelope.freeze
    end

    def self.load_selection!(path)
      fail!("IDC envelope selection path is invalid") unless path.is_a?(String) && !path.strip.empty? && File.file?(path)

      document = YAML.load_file(path)
      fail!("IDC envelope selection must be a YAML mapping") unless document.is_a?(Hash)

      document
    rescue Psych::Exception
      fail!("IDC envelope selection YAML is invalid")
    end

    def self.validate_selection!(selection, summary:, root:)
      fail!("IDC envelope selection is invalid") unless selection.is_a?(Hash) && selection["kind"] == SELECTION_KIND && selection["version"] == 1
      fields = selection["project_fields"]
      fail!("IDC envelope project_fields must be a non-empty unique allow-list") unless fields.is_a?(Array) && !fields.empty? && fields.uniq.length == fields.length && (fields - SAFE_PROJECT_FIELDS).empty?
      decision_ids = Array(selection["decision_ids"])
      fail!("IDC envelope decision_ids must be unique identifiers") unless decision_ids.uniq.length == decision_ids.length && decision_ids.all? { |id| identifier?(id) }
      allowed_artifacts = allowed_artifacts(summary)
      artifact_references = Array(selection["artifact_references"])
      fail!("IDC envelope artifact_references must be unique allow-listed references") unless artifact_references.uniq.length == artifact_references.length && (artifact_references - allowed_artifacts).empty?
      fail!("IDC envelope selector contains an authority field") if selection.keys.any? { |key| %w[write path command shell git codex].include?(key) }

      {"project_fields" => fields.sort, "decision_ids" => decision_ids.sort, "artifact_references" => artifact_references.sort}.freeze
    end
    private_class_method :validate_selection!

    def self.selected_decisions!(root:, decision_ids:)
      return [].freeze if decision_ids.empty?

      path = File.join(root, "docs/decision-log.yaml")
      fail!("IDC envelope decision log is unavailable") unless File.file?(path)
      entries = DecisionLog.load!(path).fetch("entries")
      selected = decision_ids.map do |id|
        entry = entries.find { |candidate| candidate["id"] == id }
        fail!("IDC envelope decision is not allow-listed: #{id}") unless entry && DECISION_STATUSES.include?(entry["status"])

        {"id" => entry.fetch("id"), "statement" => entry.fetch("decision"), "status" => entry.fetch("status"), "reference" => "docs/decision-log.yaml"}.freeze
      end
      selected.sort_by { |entry| entry.fetch("id") }.freeze
    rescue ArgumentError
      fail!("IDC envelope decision log is invalid")
    end
    private_class_method :selected_decisions!

    def self.selected_artifacts!(root:, references:)
      references.map do |reference|
        path = resolve_reference!(root, reference)
        {"reference" => reference, "revision_or_digest" => "sha256:#{Digest::SHA256.file(path).hexdigest}"}.freeze
      end.freeze
    end
    private_class_method :selected_artifacts!

    def self.source_provenance(projection:, decisions:, artifacts:, observed_at:)
      sources = [{"id" => "dora-read-model", "allowed_kind" => "dora_read_envelope", "locator" => "project_read_model", "revision_or_digest" => "sha256:#{Digest::SHA256.hexdigest(Marshal.dump(projection))}", "observed_at" => observed_at}]
      decisions.each { |decision| sources << {"id" => "decision-#{decision.fetch("id")}", "allowed_kind" => "dora_read_envelope", "locator" => "#{decision.fetch("reference")}##{decision.fetch("id")}", "revision_or_digest" => artifact_digest(artifacts, decision.fetch("reference")), "observed_at" => observed_at} }
      artifacts.each_with_index { |artifact, index| sources << {"id" => "artifact-#{index + 1}", "allowed_kind" => "dora_read_envelope", "locator" => artifact.fetch("reference"), "revision_or_digest" => artifact.fetch("revision_or_digest"), "observed_at" => observed_at} }
      sources.sort_by { |source| source.fetch("id") }.freeze
    end
    private_class_method :source_provenance

    def self.artifact_digest(artifacts, reference)
      artifact = artifacts.find { |candidate| candidate.fetch("reference") == reference }
      artifact ? artifact.fetch("revision_or_digest") : "sha256:unselected"
    end
    private_class_method :artifact_digest

    def self.allowed_artifacts(summary)
      references = Array(summary["references"]).dup
      references << "docs/decision-log.yaml"
      references.select { |reference| safe_reference?(reference) }.uniq.sort
    end
    private_class_method :allowed_artifacts

    def self.resolve_reference!(root, reference)
      fail!("IDC envelope reference is invalid") unless safe_reference?(reference)
      path = File.expand_path(reference, root)
      fail!("IDC envelope reference resolves outside project root") unless path.start_with?("#{File.expand_path(root)}/")
      fail!("IDC envelope reference is unavailable") unless File.file?(path)
      resolved = File.realpath(path)
      fail!("IDC envelope reference resolves outside project root") unless resolved.start_with?("#{File.realpath(root)}/")

      resolved
    end
    private_class_method :resolve_reference!

    def self.safe_reference?(reference)
      reference.is_a?(String) && !reference.empty? && !reference.start_with?("/") && !reference.split("/").include?("..")
    end
    private_class_method :safe_reference?

    def self.identifier?(value)
      value.is_a?(String) && value.match?(%r{\A[a-z][a-z0-9-]*\z})
    end
    private_class_method :identifier?

    def self.utc_time!(value)
      fail!("IDC envelope observed_at is invalid") unless value.is_a?(String) && Time.iso8601(value).utc.iso8601 == value

      value
    rescue ArgumentError
      fail!("IDC envelope observed_at is invalid")
    end
    private_class_method :utc_time!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
