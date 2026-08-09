#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_status"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-project-status") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "status-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  adapter_path = File.join(root, ".dora/project.yaml")
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "active", "plan" => "docs/work/active.yaml", "task" => "write-status", "status" => "in_progress"}, {"id" => "pending", "plan" => "docs/work/pending.yaml", "task" => "record-proof", "status" => "pending"}]}
  File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump(inventory))
  decision_log = {"kind" => "dora_decision_log", "version" => 1, "entries" => [{"id" => "retention-choice", "decision" => "Choose a retention policy.", "status" => "proposed", "domain_references" => ["docs/domain-library.yaml"], "plan_references" => ["docs/work/pending.yaml"], "evidence_references" => ["docs/product-brief.yaml"]}]}
  File.write(File.join(root, "docs/decision-log.yaml"), YAML.dump(decision_log))
  status = Dora::ProjectStatus.report!(adapter_path: adapter_path, inventory_path: "docs/work/inventory.yaml", adapter_schema_path: File.join(ROOT, "project-adapter.schema.yaml"), control_schema_path: File.join(ROOT, "project-control.schema.yaml"))
  abort "status lost active work" unless status.fetch("active_work").map { |item| item.fetch("id") } == ["active"]
  abort "status lost an open product decision" unless status.fetch("open_decisions").any?
  abort "status did not read the declared documentation decision log" unless status.fetch("decision_log").map { |item| item.fetch("id") } == ["retention-choice"]
  abort "status did not expose evidence gaps" unless status.fetch("evidence_gaps").map { |item| item.fetch("id") } == %w[active pending]
  abort "status made a completion claim" if status.dig("completion", "claimed")
end

puts "Dora project status test passed (health, work, decisions, and evidence gaps stay separate)."
