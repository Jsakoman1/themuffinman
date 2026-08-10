# frozen_string_literal: true

require "yaml"
require "time"

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

    def self.freshness_review!(path, changed_paths)
      log = load!(path)
      paths = Array(changed_paths).uniq.sort
      fail!("decision freshness changed paths must be a non-empty project-relative list") if paths.empty? || paths.any? { |item| !safe_relative_path?(item) }
      candidates = log.fetch("entries").each_with_object([]) do |entry, results|
        next if entry["status"] == "superseded"

        references = Array(entry["impact_references"])
        matches = references.select { |reference| paths.any? { |path| matches_reference?(path, reference) } }
        results << entry.slice("id", "decision", "status").merge("matched_references" => matches, "classification_required" => true) unless matches.empty?
      end
      {"kind" => "dora_decision_freshness_review", "version" => 1, "observed_at" => Time.now.utc.iso8601, "read_only" => true, "disposition" => "advisory", "changed_paths" => paths, "review_candidates" => candidates, "completion_boundary" => "Freshness candidates require owner review; this diagnostic never reopens, changes, or invalidates a decision."}.freeze
    end

    def self.validate_entry!(entry)
      fail!("decision log entry must be a mapping") unless entry.is_a?(Hash)
      REQUIRED_FIELDS.each do |field|
        value = entry[field]
        valid = field.end_with?("_references") ? value.is_a?(Array) && !value.empty? && value.all? { |item| string?(item) } : string?(value)
        fail!("decision log entry is missing #{field}") unless valid
      end
      fail!("decision log entry has invalid status") unless STATUSES.include?(entry.fetch("status"))
      if entry.key?("impact_references")
        references = entry.fetch("impact_references")
        fail!("decision log impact_references must be a non-empty list of project-relative paths") unless references.is_a?(Array) && !references.empty? && references.all? { |reference| safe_relative_path?(reference) }
      end
    end

    def self.matches_reference?(path, reference)
      path == reference || reference.end_with?("/") && path.start_with?(reference)
    end
    private_class_method :matches_reference?

    def self.safe_relative_path?(value)
      string?(value) && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

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
