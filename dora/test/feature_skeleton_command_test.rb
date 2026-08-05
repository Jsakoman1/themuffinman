#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-feature-skeleton-command") do |root|
  input_path = File.join(root, "record-supply.yaml")
  input = {
    "kind" => "dora_feature_skeleton", "version" => 1,
    "capability" => "record-supply", "entity" => "supply",
    "fields" => [{"id" => "name", "type" => "string", "confirmed" => true}],
    "permission" => "household-write", "workflow" => "supply-active",
    "api_operations" => [{"id" => "create-supply", "purpose" => "Record one supply.", "confirmed" => true}],
    "ui_blueprint" => "list-detail", "confirmation" => true
  }
  File.write(input_path, YAML.dump(input))

  output, status = Open3.capture2e(CLI, "feature-skeleton", input_path, "--format", "json", chdir: ROOT)
  abort "feature skeleton preview failed: #{output}" unless status.success?
  preview = JSON.parse(output).fetch("payload")
  paths = preview.fetch("proposed_files").map { |file| file.fetch("path") }
  abort "migration proposal is missing" unless paths.include?("backend/src/main/resources/db/migration/V__create_supply.sql")
  abort "Vue proposal is missing" unless paths.include?("frontend/src/features/record-supply/feature-view.vue")
  abort "preview wrote project files" unless Dir.children(root) == ["record-supply.yaml"]

  input["confirmation"] = false
  File.write(input_path, YAML.dump(input))
  rejected, rejected_status = Open3.capture2e(CLI, "feature-skeleton", input_path, chdir: ROOT)
  abort "unconfirmed skeleton was accepted" if rejected_status.success?
  abort "missing confirmation explanation" unless rejected.include?("confirmation")
end

puts "Dora feature skeleton command test passed (read-only proposals cover migration, backend, API, Vue, tests, runtime, and documentation)."
