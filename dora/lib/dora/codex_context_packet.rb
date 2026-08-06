# frozen_string_literal: true

require "yaml"

module Dora
  class CodexContextPacket
    SCHEMA_PATH = File.expand_path("../../codex-context-packet.schema.yaml", __dir__)

    def self.build!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("Codex context packet schema is invalid") unless schema["kind"] == "dora_codex_context_packet_schema" && schema["version"].to_i == 1
      fail!("Codex context packet must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_codex_context_packet" && document["version"].to_i == 1
      required!(document, schema.fetch("required_fields"), "Codex context packet")
      task = validate_task!(document.fetch("task"), schema)
      paths = validate_paths!(document.fetch("allowed_paths"))
      confirmed = validate_confirmed!(document.fetch("confirmed"), schema)
      dependencies = validate_identifiers!(document.fetch("dependencies"), "dependencies")
      validation = document.fetch("validation")
      fail!("Codex context packet validation is invalid") unless statement?(validation)
      obligations = validate_obligations!(document.fetch("proof_obligations"), schema)
      citations = validate_citations!(document.fetch("citations"))
      omitted = validate_identifiers!(document.fetch("omitted"), "omitted")
      fail!("Codex context packet must explicitly omit product inference") unless omitted.include?("product-inference")
      boundary = document.fetch("completion_boundary")
      fail!("Codex context packet completion boundary is invalid") unless statement?(boundary) && boundary.include?("does not")

      {"kind" => "dora_codex_context_packet", "version" => 1, "task" => task, "allowed_paths" => paths, "confirmed" => confirmed, "dependencies" => dependencies, "validation" => validation, "proof_obligations" => obligations, "citations" => citations, "omitted" => omitted, "completion_boundary" => boundary}.freeze
    rescue Psych::Exception => error
      fail!("Codex context packet YAML is invalid: #{error.message}")
    end

    def self.required!(document, fields, label)
      missing = fields.reject { |field| document.key?(field) }
      fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :required!

    def self.validate_task!(task, schema)
      fail!("Codex context packet task must be a mapping") unless task.is_a?(Hash)
      required!(task, schema.fetch("task_required_fields"), "Codex context packet task")
      fail!("Codex context packet task id is invalid") unless identifier?(task.fetch("id"))
      fail!("Codex context packet observable outcome is invalid") unless statement?(task.fetch("observable_outcome"))
      task.slice(*schema.fetch("task_required_fields")).freeze
    end
    private_class_method :validate_task!

    def self.validate_paths!(paths)
      fail!("Codex context packet allowed_paths must be a non-empty list") unless paths.is_a?(Array) && !paths.empty? && paths.all? { |path| safe_relative_path?(path) }
      fail!("Codex context packet allowed_paths must be unique") unless paths.uniq.length == paths.length
      paths.sort.freeze
    end
    private_class_method :validate_paths!

    def self.validate_confirmed!(confirmed, schema)
      fail!("Codex context packet confirmed section must be a mapping") unless confirmed.is_a?(Hash)
      schema.fetch("confirmed_sections").each do |section|
        values = confirmed[section]
        fail!("Codex context packet confirmed #{section} is missing") unless values.is_a?(Array) && !values.empty? && values.all? { |value| identifier?(value) }
      end
      confirmed.slice(*schema.fetch("confirmed_sections")).transform_values { |values| values.sort.freeze }.freeze
    end
    private_class_method :validate_confirmed!

    def self.validate_identifiers!(values, label)
      fail!("Codex context packet #{label} must be a list") unless values.is_a?(Array) && values.all? { |value| identifier?(value) }
      fail!("Codex context packet #{label} must be unique") unless values.uniq.length == values.length
      values.sort.freeze
    end
    private_class_method :validate_identifiers!

    def self.validate_obligations!(obligations, schema)
      fail!("Codex context packet proof_obligations must be a non-empty list") unless obligations.is_a?(Array) && !obligations.empty?
      rows = obligations.map do |obligation|
        fail!("Codex context packet proof obligation must be a mapping") unless obligation.is_a?(Hash)
        required!(obligation, schema.fetch("proof_obligation_required_fields"), "Codex context packet proof obligation")
        fail!("Codex context packet proof obligation id is invalid") unless identifier?(obligation.fetch("id"))
        fail!("Codex context packet evidence class is invalid") unless schema.fetch("evidence_classes").include?(obligation.fetch("evidence_class"))
        fail!("Codex context packet proof obligation status is invalid") unless %w[required unresolved].include?(obligation.fetch("status"))
        fail!("Codex context packet proof obligation boundary is invalid") unless statement?(obligation.fetch("boundary"))
        obligation.slice(*schema.fetch("proof_obligation_required_fields"))
      end
      ids = rows.map { |row| row.fetch("id") }
      fail!("Codex context packet proof obligation ids must be unique") unless ids.uniq.length == ids.length
      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_obligations!

    def self.validate_citations!(citations)
      fail!("Codex context packet citations must be a non-empty list") unless citations.is_a?(Array) && !citations.empty?
      rows = citations.map do |citation|
        fail!("Codex context packet citation must be a mapping") unless citation.is_a?(Hash)
        %w[id path reason].each { |field| fail!("Codex context packet citation is missing #{field}") unless statement?(citation[field]) }
        fail!("Codex context packet citation id is invalid") unless identifier?(citation.fetch("id"))
        fail!("Codex context packet citation path is invalid") unless safe_relative_path?(citation.fetch("path"))
        citation.slice("id", "path", "reason")
      end
      ids = rows.map { |row| row.fetch("id") }
      fail!("Codex context packet citation ids must be unique") unless ids.uniq.length == ids.length
      rows.sort_by { |row| row.fetch("id") }.freeze
    end
    private_class_method :validate_citations!

    def self.safe_relative_path?(value)
      value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
