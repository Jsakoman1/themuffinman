# frozen_string_literal: true

require "yaml"
require "fileutils"

module Dora
  class ArchitectureDecision
    SCHEMA_PATH = File.expand_path("../../architecture-decision.schema.yaml", __dir__)
    STATUSES = %w[proposed accepted unresolved superseded].freeze
    OFFLINE_SYNC_STATES = %w[not-needed required undecided].freeze

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("architecture decision schema is invalid") unless schema["kind"] == "dora_architecture_decision_schema" && schema["version"].to_i == 1
      fail!("architecture decision must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_architecture_decision" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("architecture decision is missing #{missing.join(", ")}") unless missing.empty?
      fail!("architecture decision id is invalid") unless identifier?(document["id"])
      fail!("architecture decision status is invalid") unless STATUSES.include?(document["status"])
      fail!("architecture decision alternatives are invalid") unless statements?(document["alternatives"])
      fail!("architecture decision rationale is invalid") unless statement?(document["rationale"])
      fail!("architecture decision consequences are invalid") unless statements?(document["consequences"])
      fail!("architecture decision citations are invalid") unless Array(document["citations"]).any? && Array(document["citations"]).all? { |path| safe_reference?(path) }
      fail!("architecture decision offline/sync state is invalid") unless OFFLINE_SYNC_STATES.include?(document["offline_sync"])
      fail!("architecture decision confirmation must be true") unless document["confirmation"] == true
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("architecture decision YAML is invalid: #{error.message}")
    end

    def self.append!(path:, entry:)
      decision = validate!(entry)
      existing = if File.exist?(path)
                   document = YAML.load_file(path)
                   fail!("architecture decision log is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_architecture_decision_log" && document["version"].to_i == 1 && document["entries"].is_a?(Array)
                   document.fetch("entries")
                 else
                   []
                 end
      fail!("architecture decision id already exists: #{decision.fetch("id")}") if existing.any? { |candidate| candidate.fetch("id") == decision.fetch("id") }
      FileUtils.mkdir_p(File.dirname(File.expand_path(path)))
      File.write(path, YAML.dump({"kind" => "dora_architecture_decision_log", "version" => 1, "entries" => existing + [decision]}).sub(/\A---\n/, ""))
      {"kind" => "dora_architecture_decision_append", "version" => 1, "path" => path, "decision" => decision, "preserved_decision_ids" => existing.map { |candidate| candidate.fetch("id") }, "completion_boundary" => "Appending one cited decision preserves prior records but does not configure a system, implement a feature, or prove acceptance."}.freeze
    rescue Psych::Exception => error
      fail!("architecture decision log YAML is invalid: #{error.message}")
    end

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statements?(value); value.is_a?(Array) && !value.empty? && value.all? { |item| statement?(item) }; end
    private_class_method :statements?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.safe_reference?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_reference?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
