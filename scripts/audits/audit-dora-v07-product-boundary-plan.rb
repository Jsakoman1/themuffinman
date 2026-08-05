#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
MASTER_PATH = "docs/work/dora-v07-complete-product-boundary-master.yaml"
INVENTORY_PATH = "docs/work/dora-v07-complete-product-boundary-execution-inventory.yaml"
EXPECTED = %w[dora-v07-harden dora-v07-catalog dora-v07-report-writer dora-v07-plugin-v2 dora-v07-plugin-adapters dora-v07-manifest-coverage dora-v07-wrapper-delegation dora-v07-buildable-starter dora-v07-independent-consumer dora-v07-product-boundary dora-v07-onboarding dora-v07-release muffinman-v07-pin].freeze

master = YAML.load_file(File.join(ROOT, MASTER_PATH))
inventory = YAML.load_file(File.join(ROOT, INVENTORY_PATH))
failures = []
failures << "master kind or id is invalid" unless master["kind"] == "master" && master["id"] == "dora-v07-complete-product-boundary-master"
failures << "master hardening ownership gate is missing" unless master.dig("hardening_review", "ownership_gate").to_s.include?("every local audit")
failures << "master must preserve product evidence authority" unless Array(master["principles"]).any? { |value| value.include?("business meaning") && value.include?("evidence authority") }
items = Array(inventory["items"])
failures << "inventory differs from v0.7 queue" unless items.map { |item| item["id"] } == EXPECTED
failures << "inventory sequence is not contiguous" unless items.map { |item| item["sequence"] } == (1..EXPECTED.length).to_a
failures << "inventory hardening item count is invalid" unless inventory.dig("hardening_contract", "item_count") == EXPECTED.length

mapped = []
items.each_with_index do |item, index|
  plan_path = item["plan"]
  plan = YAML.load_file(File.join(ROOT, plan_path))
  task = Array(plan["tasks"]).find { |candidate| candidate["id"] == item["task"] }
  unless task
    failures << "missing task mapping for #{item["id"]}"
    next
  end
  mapped << [plan_path, task["id"]]
  failures << "inventory mapping mismatch for #{item["id"]}" unless task["inventory_item"] == item["id"]
  failures << "serial dependency mismatch for #{item["id"]}" unless Array(task["dependencies"]) == (index.zero? ? [] : [items[index - 1]["id"]])
  failures << "required paths are not exact for #{item["id"]}" unless Array(task["required_paths"]).any? && Array(task["paths"]) == Array(task["required_paths"])
  failures << "missing outcome or evidence for #{item["id"]}" if task["observable_outcome"].to_s.empty? || Array(task["evidence_boundary"]).empty?
  failures << "recursive or missing validation for #{item["id"]}" if task["validation"].to_s.empty? || task["validation"].to_s.match?(/work-verify|scripts\/verify-work\.rb/)
end

child_tasks = Array(master["children"]).flat_map { |path| Array(YAML.load_file(File.join(ROOT, path))["tasks"]).map { |task| [path, task["id"]] } }
failures << "child task mappings differ from inventory" unless child_tasks.to_set == mapped.to_set
catalog = items.find { |item| item["id"] == "dora-v07-catalog" }
catalog_task = catalog && YAML.load_file(File.join(ROOT, catalog["plan"]))["tasks"].find { |task| task["id"] == catalog["task"] }
failures << "catalog must classify every local audit" unless catalog_task && catalog_task["observable_outcome"].include?("Every MuffinMan local audit") && catalog_task["observable_outcome"].include?("no unclassified")
starter = items.find { |item| item["id"] == "dora-v07-buildable-starter" }
starter_task = starter && YAML.load_file(File.join(ROOT, starter["plan"]))["tasks"].find { |task| task["id"] == starter["task"] }
failures << "starter must stay domain-free" unless starter_task && starter_task["observable_outcome"].include?("without business entities")
%w[dora-v07-release muffinman-v07-pin].each do |id|
  item = items.find { |candidate| candidate["id"] == id }
  task = item && YAML.load_file(File.join(ROOT, item["plan"]))["tasks"].find { |candidate| candidate["id"] == item["task"] }
  failures << "#{id} must require external approval" unless task && task["requires_external_approval"] == true
end

abort "Dora v0.7 product-boundary plan audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora v0.7 product-boundary plan audit passed (#{items.length} atomic serial items)."
