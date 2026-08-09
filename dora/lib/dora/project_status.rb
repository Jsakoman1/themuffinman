# frozen_string_literal: true

require "yaml"
require "json"
require_relative "adapter"
require_relative "decision_log"
require_relative "project_doctor"
require_relative "project_knowledge"

module Dora
  class ProjectStatus
    def self.report!(adapter_path:, inventory_path:, adapter_schema_path:, control_schema_path:)
      adapter = Adapter.validate!(adapter_path, adapter_schema_path)
      root = adapter.fetch("root")
      fail!("execution inventory path must be project-relative") unless safe_relative_path?(inventory_path)
      absolute_inventory = File.expand_path(inventory_path, root)
      fail!("execution inventory is missing: #{inventory_path}") unless absolute_inventory.start_with?("#{root}/") && File.file?(absolute_inventory)
      inventory = YAML.load_file(absolute_inventory)
      fail!("execution inventory kind is invalid") unless inventory.is_a?(Hash) && inventory["kind"] == "execution_inventory" && inventory["version"].to_i == 1 && inventory["items"].is_a?(Array)

      health = ProjectDoctor.run(adapter_path, schema_path: adapter_schema_path, control_schema_path: control_schema_path)
      knowledge = ProjectKnowledge.validate!(root)
      items = inventory.fetch("items").select { |item| item.is_a?(Hash) }
      {
        "kind" => "dora_project_status", "version" => 1,
        "health" => {"healthy" => health.fetch("healthy"), "failed_checks" => health.fetch("checks").select { |check| check.fetch("status") == "failed" }.map { |check| check.fetch("id") }},
        "active_work" => items.select { |item| item["status"] == "in_progress" }.map { |item| item.slice("id", "plan", "task") },
        "open_decisions" => knowledge.dig("product_brief", "unanswered_decisions"),
        "findings" => findings_for(root),
        "decision_log" => decision_entries_for(root, adapter.dig("paths", "docs")),
        "evidence_gaps" => items.reject { |item| item["status"] == "verified" }.map { |item| item.slice("id", "plan", "task", "status") },
        "completion" => {"claimed" => false, "reason" => "Only project work verification records completion evidence."}
      }.freeze
    rescue Psych::Exception => error
      fail!("project status YAML is invalid: #{error.message}")
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.findings_for(root)
      Dir[File.join(root, "docs/audit-output/**/*.json")].sort.flat_map do |path|
        report = JSON.parse(File.read(path))
        next [] unless report["kind"] == "dora_plugin_report" && report["finding_contract"] == "dora_finding"

        Array(report["findings"]).map { |finding| finding.slice("id", "severity", "location", "explanation", "repair", "evidence", "diagnostic_boundary") }
      rescue JSON::ParserError
        []
      end
    end
    private_class_method :findings_for

    def self.decision_entries_for(root, docs_root)
      [File.join(docs_root.to_s, "decision-log.yaml"), ".dora/decision-log.yaml"].uniq.flat_map do |relative|
        path = File.join(root, relative)
        next [] unless File.file?(path)

        DecisionLog.load!(path).fetch("entries").map { |entry| entry.slice("id", "decision", "status", "evidence_references") }
      end
    end
    private_class_method :decision_entries_for

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
