#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
MASTER_PATH = "docs/work/dora-v06-standalone-bootstrap-master.yaml"
INVENTORY_PATH = "docs/work/dora-v06-standalone-bootstrap-execution-inventory.yaml"
EXPECTED_ITEMS = %w[
  dora-v06-harden
  dora-v06-source-contract
  dora-v06-bootstrap
  dora-v06-starter-contract
  dora-v06-starters
  dora-v06-plugin-runner
  dora-v06-plugin-reports
  dora-v06-muffinman-manifest
  dora-v06-wrapper-parity
  dora-v06-onboarding
  dora-v06-zero-knowledge
  dora-v06-release
  muffinman-v06-pin
].freeze

master = YAML.load_file(File.join(ROOT, MASTER_PATH))
inventory = YAML.load_file(File.join(ROOT, INVENTORY_PATH))
failures = []

failures << "master kind or id is invalid" unless master["kind"] == "master" && master["id"] == "dora-v06-standalone-bootstrap-master"
failures << "master is missing the atomic hardening child" unless Array(master["children"]).include?("docs/work/dora-v06-standalone-bootstrap-atomic-hardening.yaml")
failures << "master is missing its serial inventory" unless master["execution_inventory"] == INVENTORY_PATH
failures << "master hardening contract is missing" unless master.dig("hardening_review", "contract").to_s.include?("thirteen")
failures << "master must retain explicit-source safety" unless Array(master["principles"]).any? { |principle| principle.include?("explicit source") && principle.include?("never silently") }

items = Array(inventory["items"])
ids = items.map { |item| item["id"] }
failures << "inventory item ids differ from the v0.6 queue" unless ids == EXPECTED_ITEMS
failures << "inventory sequence is not contiguous" unless items.map { |item| item["sequence"] } == (1..EXPECTED_ITEMS.length).to_a
failures << "inventory order is not contiguous" unless items.map { |item| item["order"] } == (1..EXPECTED_ITEMS.length).to_a
failures << "inventory hardening item count is invalid" unless inventory.dig("hardening_contract", "item_count") == EXPECTED_ITEMS.length

task_keys = []
items.each_with_index do |item, index|
  plan_path = item["plan"].to_s
  plan_file = File.join(ROOT, plan_path)
  unless File.file?(plan_file)
    failures << "missing plan for #{item["id"]}: #{plan_path}"
    next
  end

  plan = YAML.load_file(plan_file)
  task = Array(plan["tasks"]).find { |candidate| candidate["id"] == item["task"] }
  unless task
    failures << "missing mapped task for #{item["id"]}"
    next
  end

  task_keys << [plan_path, task["id"]]
  failures << "inventory mapping mismatch for #{item["id"]}" unless task["inventory_item"] == item["id"]
  expected_dependencies = index.zero? ? [] : [items[index - 1]["id"]]
  failures << "serial dependency mismatch for #{item["id"]}" unless Array(task["dependencies"]) == expected_dependencies
  failures << "missing observable outcome for #{item["id"]}" if task["observable_outcome"].to_s.strip.empty?
  failures << "required paths must be exact for #{item["id"]}" if Array(task["required_paths"]).empty? || Array(task["paths"]) != Array(task["required_paths"])
  failures << "missing evidence boundary for #{item["id"]}" if Array(task["evidence_boundary"]).empty?
  validation = task["validation"].to_s
  failures << "missing leaf validation for #{item["id"]}" if validation.empty? || validation.match?(/\bwork-verify\b|scripts\/verify-work\.rb/)
end

children = Array(master["children"])
child_task_keys = children.flat_map do |path|
  plan_path = File.join(ROOT, path)
  next [] unless File.file?(plan_path)

  Array(YAML.load_file(plan_path)["tasks"]).map { |task| [path, task["id"]] }
end
failures << "child tasks and inventory mappings differ" unless child_task_keys.to_set == task_keys.to_set

source = items.find { |item| item["id"] == "dora-v06-source-contract" }
source_task = source && YAML.load_file(File.join(ROOT, source["plan"]))["tasks"].find { |task| task["id"] == source["task"] }
failures << "bootstrap source task must reject implicit network use" unless source_task && source_task["observable_outcome"].include?("declared local source") && source_task["observable_outcome"].include?("implicit network")

release = items.find { |item| item["id"] == "dora-v06-release" }
pin = items.find { |item| item["id"] == "muffinman-v06-pin" }
release_task = release && YAML.load_file(File.join(ROOT, release["plan"]))["tasks"].find { |task| task["id"] == release["task"] }
pin_task = pin && YAML.load_file(File.join(ROOT, pin["plan"]))["tasks"].find { |task| task["id"] == pin["task"] }
failures << "release task must require external approval" unless release_task && release_task["requires_external_approval"] == true
failures << "consumer pin must require external approval" unless pin_task && pin_task["requires_external_approval"] == true
failures << "consumer pin must follow release" unless pin_task && Array(pin_task["dependencies"]) == ["dora-v06-release"]

abort "Dora v0.6 bootstrap plan audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora v0.6 bootstrap plan audit passed (#{items.length} atomic serial items)."
