# frozen_string_literal: true

require "yaml"
require "fileutils"
require_relative "decision_log"

module Dora
  class DecisionRecord
    def self.append!(path:, entry:)
      validate_entry!(entry)
      existing = File.exist?(path) ? DecisionLog.load!(path).fetch("entries") : []
      fail!("decision record id already exists: #{entry.fetch("id")}") if existing.any? { |record| record.fetch("id") == entry.fetch("id") }
      document = {"kind" => "dora_decision_log", "version" => 1, "entries" => existing + [entry]}
      FileUtils.mkdir_p(File.dirname(File.expand_path(path)))
      File.write(path, YAML.dump(document))
      {"kind" => "dora_decision_record_append", "version" => 1, "path" => path, "record" => entry, "preserved_records" => existing.map { |record| record.fetch("id") }, "completion_boundary" => "Appending a cited decision record does not infer acceptance, resolve an open question, or prove implementation or release readiness."}.freeze
    end

    def self.validate_entry!(entry)
      fail!("decision record must be a mapping") unless entry.is_a?(Hash)
      DecisionLog::REQUIRED_FIELDS.each do |field|
        value = entry[field]
        valid = field.end_with?("_references") ? value.is_a?(Array) && !value.empty? && value.all? { |item| safe_reference?(item) } : statement?(value)
        fail!("decision record is missing or invalid #{field}") unless valid
      end
      fail!("decision record status is invalid") unless DecisionLog::STATUSES.include?(entry.fetch("status"))
      entry.slice(*DecisionLog::REQUIRED_FIELDS).freeze
    end

    def self.safe_reference?(value)
      statement?(value) && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_reference?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
