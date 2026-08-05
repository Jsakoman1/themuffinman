#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_status"

ROOT = File.expand_path("..", __dir__)

Dir.mktmpdir("dora-evidence-status") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "evidence-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump({"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "pending", "plan" => "docs/work/first.yaml", "task" => "record-note", "status" => "pending"}]}))
  File.write(File.join(root, ".dora/decision-log.yaml"), YAML.dump({"kind" => "dora_decision_log", "version" => 1, "entries" => [{"id" => "DEC-1", "decision" => "Keep notes group-owned.", "status" => "proposed", "domain_references" => ["docs/domain-library.yaml"], "plan_references" => ["docs/work/first.yaml"], "evidence_references" => ["docs/audit-output/plugin.json"]}]}))
  File.write(File.join(root, "docs/audit-output/plugin.json"), JSON.dump({"kind" => "dora_plugin_report", "version" => 1, "finding_contract" => "dora_finding", "findings" => [{"id" => "plugin-note", "severity" => "warning", "location" => {"path" => "docs/note.md"}, "explanation" => "A note requires review.", "repair" => "Review the note.", "evidence" => ["plugin:note"], "diagnostic_boundary" => "does not prove completion"}]}))
  status = Dora::ProjectStatus.report!(adapter_path: File.join(root, ".dora/project.yaml"), inventory_path: "docs/work/inventory.yaml", adapter_schema_path: File.join(ROOT, "project-adapter.schema.yaml"), control_schema_path: File.join(ROOT, "project-control.schema.yaml"))
  abort "status lost diagnostic finding" unless status.fetch("findings").map { |finding| finding.fetch("id") } == ["plugin-note"]
  abort "status lost declared decision" unless status.fetch("decision_log").map { |entry| entry.fetch("id") } == ["DEC-1"]
  abort "status mixed evidence gaps with findings" unless status.fetch("evidence_gaps").map { |item| item.fetch("id") } == ["pending"]
  abort "status made a completion claim" if status.dig("completion", "claimed")
end

puts "Dora evidence status test passed (findings, decisions, gaps, and completion boundary remain separate)."
