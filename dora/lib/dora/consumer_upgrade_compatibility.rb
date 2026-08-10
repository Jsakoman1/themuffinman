# frozen_string_literal: true

require "time"

module Dora
  # Projects facts already produced by ProjectUpgrade.preview! into conservative
  # review dimensions. It cannot retrieve sources, apply an upgrade, or infer
  # that any consumer is compatible.
  class ConsumerUpgradeCompatibility
    DIMENSIONS = {
      "schema" => {pattern: ->(path) { path.end_with?(".schema.yaml") || path.end_with?("-schema.yaml") }, retest: "work-artifact-schema"},
      "command" => {pattern: ->(path) { path == "bin/dora" || path == "lib/dora/command_registry.rb" }, retest: "command-surface"},
      "plugin" => {pattern: ->(path) { path.include?("plugin") }, retest: "plugin-contract"},
      "template" => {pattern: ->(path) { path.start_with?("templates/") }, retest: "template-freshness"},
      "core_implementation" => {pattern: ->(path) { path.start_with?("lib/dora/") }, retest: "targeted-dora-tests"}
    }.freeze

    def self.report!(preview:)
      validate_preview!(preview)
      changes = preview.fetch("migrations").flat_map { |change_kind, paths| paths.map { |path| classify(path, change_kind) } }
      dimensions = changes.group_by { |change| change.fetch("dimension") }.sort.to_h do |dimension, entries|
        [dimension, {"paths" => entries.map { |entry| entry.fetch("path") }.sort, "classification" => classification(entries), "required_retests" => entries.map { |entry| entry.fetch("retest") }.uniq.sort}]
      end

      {
        "kind" => "dora_consumer_upgrade_compatibility_report",
        "version" => 1,
        "observed_at" => Time.now.utc.iso8601,
        "source_references" => ["project_upgrade_preview", "current:#{preview.dig("consumer", "current_ref")}", "target:#{preview.dig("target", "ref")}"],
        "read_only" => true,
        "disposition" => "advisory",
        "consumer_compatibility" => "not_proven",
        "dimensions" => dimensions,
        "manual_decisions" => changes.select { |change| change.fetch("classification") != "additive" }.map { |change| "Review #{change.fetch("change_kind")} #{change.fetch("path")}: #{change.fetch("classification")}." }.sort,
        "completion_boundary" => "This report classifies a supplied local read-only upgrade preview only; it cannot retrieve sources, apply or roll back an upgrade, modify a consumer pin, create an approval or decision, change work status, invoke GitHub, or prove consumer compatibility."
      }.freeze
    end

    def self.validate_preview!(preview)
      fail!("upgrade preview is invalid") unless preview.is_a?(Hash) && preview["kind"] == "dora_project_upgrade_preview" && preview["version"].to_i == 1 && preview["read_only"] == true
      fail!("upgrade preview consumer source is invalid") unless reference?(preview.dig("consumer", "current_ref"))
      fail!("upgrade preview target source is invalid") unless reference?(preview.dig("target", "ref"))
      migrations = preview["migrations"]
      fail!("upgrade preview migrations are invalid") unless migrations.is_a?(Hash) && migrations.keys.sort == %w[added changed removed] && migrations.values.all? { |paths| paths.is_a?(Array) && paths.all? { |path| safe_path?(path) } }
    end
    private_class_method :validate_preview!

    def self.classify(path, change_kind)
      dimension, rule = DIMENSIONS.find { |_id, candidate| candidate.fetch(:pattern).call(path) } || ["unknown", {retest: "owner-review"}]
      {"path" => path, "change_kind" => change_kind, "dimension" => dimension, "classification" => compatibility_classification(dimension, change_kind), "retest" => rule.fetch(:retest)}
    end
    private_class_method :classify

    def self.compatibility_classification(dimension, change_kind)
      return "breaking" if change_kind == "removed"
      return "additive" if change_kind == "added" && dimension == "template"

      "unknown"
    end
    private_class_method :compatibility_classification

    def self.classification(entries)
      classes = entries.map { |entry| entry.fetch("classification") }
      return "breaking" if classes.include?("breaking")
      return "unknown" if classes.include?("unknown")

      "additive"
    end
    private_class_method :classification

    def self.reference?(value)
      value.is_a?(String) && !value.strip.empty? && !value.match?(/[\r\n]/)
    end
    private_class_method :reference?

    def self.safe_path?(value)
      reference?(value) && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
