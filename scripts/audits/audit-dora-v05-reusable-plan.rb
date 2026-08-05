#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
MASTER_PATH = "docs/work/dora-v05-reusable-audits-and-plugins-master.yaml"
INVENTORY_PATH = "docs/work/dora-v05-reusable-audits-and-plugins-execution-inventory.yaml"
EXPECTED_ITEMS = %w[
  dora-v05-harden
  dora-v05-plan-contract
  dora-v05-plan-coverage
  dora-v05-serial-parity
  dora-v05-control-ownership
  dora-v05-plugin-contract
  dora-v05-spring-config
  dora-v05-spring-mapping
  dora-v05-http-contract
  dora-v05-vue-navigation
  dora-v05-vue-surface
  dora-v05-architecture
  dora-v05-generic-adoption
  dora-v05-stack-adoption
  dora-v05-release
  muffinman-v05-pin
].freeze

master = YAML.load_file(File.join(ROOT, MASTER_PATH))
inventory = YAML.load_file(File.join(ROOT, INVENTORY_PATH))
failures = []

failures << "master kind or id is invalid" unless master["kind"] == "master" && master["id"] == "dora-v05-reusable-audits-and-plugins-master"
failures << "master is missing the atomic hardening child" unless Array(master["children"]).include?("docs/work/dora-v05-reusable-audits-and-plugins-atomic-hardening.yaml")
failures << "master is missing its serial inventory" unless master["execution_inventory"] == INVENTORY_PATH
failures << "master hardening review is not declared" unless master.dig("hardening_review", "contract").to_s.include?("one-to-one")

items = Array(inventory["items"])
ids = items.map { |item| item["id"] }
failures << "inventory item ids differ from the v0.5 queue" unless ids == EXPECTED_ITEMS
failures << "inventory sequence is not contiguous" unless items.map { |item| item["sequence"] } == (1..EXPECTED_ITEMS.length).to_a
failures << "inventory order is not contiguous" unless items.map { |item| item["order"] } == (1..EXPECTED_ITEMS.length).to_a

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
  failures << "missing exact required paths for #{item["id"]}" if Array(task["required_paths"]).empty? || Array(task["paths"]) != Array(task["required_paths"])
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

release = items.find { |item| item["id"] == "dora-v05-release" }
pin = items.find { |item| item["id"] == "muffinman-v05-pin" }
release_task = release && YAML.load_file(File.join(ROOT, release["plan"]))["tasks"].find { |task| task["id"] == release["task"] }
pin_task = pin && YAML.load_file(File.join(ROOT, pin["plan"]))["tasks"].find { |task| task["id"] == pin["task"] }
failures << "release task must require external approval" unless release_task && release_task["requires_external_approval"] == true
failures << "pin task must follow the release task" unless pin_task && Array(pin_task["dependencies"]) == ["dora-v05-release"]

abort "Dora v0.5 reusable plan audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora v0.5 reusable plan audit passed (#{items.length} atomic serial items)."
