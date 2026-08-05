# frozen_string_literal: true

require "yaml"

module Dora
  class DecisionLog
    STATUSES = %w[proposed accepted superseded].freeze
    REQUIRED_FIELDS = %w[id decision status domain_references plan_references evidence_references].freeze

    def self.load!(path)
      log = YAML.load_file(path)
      fail!("decision log kind is invalid") unless log.is_a?(Hash) && log["kind"] == "dora_decision_log" && log["version"].to_i == 1
      entries = Array(log["entries"])
      fail!("decision log has no entries") if entries.empty?
      ids = entries.map { |entry| validate_entry!(entry); entry.fetch("id") }
      fail!("decision log ids must be unique") unless ids.uniq.length == ids.length
      {"kind" => "dora_decision_log", "version" => 1, "entries" => entries.sort_by { |entry| entry.fetch("id") }, "decision_boundary" => "Decision records describe declared choices and references; they do not resolve open product questions by inference.", "completion_boundary" => "Decision traceability is documentation only and does not prove implementation, runtime acceptance, or release approval."}.freeze
    rescue Psych::Exception => error
      fail!("decision log YAML is invalid: #{error.message}")
    end

    def self.validate_entry!(entry)
      fail!("decision log entry must be a mapping") unless entry.is_a?(Hash)
      REQUIRED_FIELDS.each do |field|
        value = entry[field]
        valid = field.end_with?("_references") ? value.is_a?(Array) && !value.empty? && value.all? { |item| string?(item) } : string?(value)
        fail!("decision log entry is missing #{field}") unless valid
      end
      fail!("decision log entry has invalid status") unless STATUSES.include?(entry.fetch("status"))
    end
    private_class_method :validate_entry!

    def self.string?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :string?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
