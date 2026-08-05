# frozen_string_literal: true

module Dora
  class MigrationSafety
    def self.review!(document)
      fail!("migration safety input is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_migration_safety_input" && document["version"].to_i == 1
      migrations = Array(document["migrations"])
      fail!("migration safety migrations must be declared") unless !migrations.empty? && migrations.all? { |migration| valid_migration?(migration) }
      findings = migrations.flat_map { |migration| findings_for(migration) }
      {"kind" => "dora_migration_safety_report", "version" => 1, "findings" => findings, "safe_to_apply" => findings.empty?, "completion_boundary" => "This static report does not modify migrations, connect to a database, or prove schema correctness."}.freeze
    end

    def self.valid_migration?(migration)
      migration.is_a?(Hash) && identifier?(migration["version"]) && statement?(migration["checksum"]) && statement?(migration["baseline_checksum"])
    end
    private_class_method :valid_migration?

    def self.findings_for(migration)
      version = migration.fetch("version")
      findings = []
      findings << finding("historical_migration_changed", version, "The declared checksum differs from the declared baseline checksum.") if migration.fetch("checksum") != migration.fetch("baseline_checksum")
      findings << finding("foreign_key_review_missing", version, "Foreign-key review is not explicitly confirmed.") unless migration["foreign_key_review"] == "confirmed"
      findings << finding("index_review_missing", version, "Index review is not explicitly confirmed.") unless migration["index_review"] == "confirmed"
      findings
    end
    private_class_method :findings_for

    def self.finding(id, version, message); {"id" => id, "migration_version" => version, "message" => message}; end
    private_class_method :finding
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-zA-Z0-9._-]+\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
