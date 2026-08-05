#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
MASTER_PATH = "docs/work/dora-v04-operational-one-click-master.yaml"
INVENTORY_PATH = "docs/work/dora-v04-operational-one-click-execution-inventory.yaml"

def load_yaml(relative)
  YAML.load_file(File.join(ROOT, relative))
end

def present?(value)
  !value.nil? && value != "" && value != []
end

master = load_yaml(MASTER_PATH)
inventory = load_yaml(INVENTORY_PATH)
failures = []
failures << "master kind is invalid" unless master["kind"] == "master" && master["status"] == "draft"
failures << "inventory kind is invalid" unless inventory["kind"] == "execution_inventory" && inventory["master_plan"] == MASTER_PATH
children = Array(master["children"])
items = Array(inventory["items"])
failures << "master must declare four child plans" unless children.length == 4
failures << "inventory order must be contiguous" unless items.map { |item| item["order"] } == (1..items.length).to_a
ids = items.map { |item| item["id"] }
failures << "inventory ids are duplicated" unless ids.uniq.length == ids.length

task_index = {}
children.each do |child|
  plan = load_yaml(child)
  failures << "child master mapping differs: #{child}" unless plan["master_plan"] == MASTER_PATH && plan["execution_inventory"] == INVENTORY_PATH
  scope = plan["scope_control"]
  %w[standard reused_verified_plans baseline_only_surfaces residual_scope retest_triggers non_duplication_rule].each do |field|
    failures << "child scope control is missing #{field}: #{child}" unless scope.is_a?(Hash) && present?(scope[field])
  end
  Array(plan["tasks"]).each { |task| task_index[[child, task["id"]]] = task }
end

mapped = []
items.each_with_index do |item, index|
  plan = item["plan"]
  task = task_index[[plan, item["task"]]]
  unless task
    failures << "inventory task mapping is missing: #{item["id"]}"
    next
  end
  mapped << [plan, task["id"]]
  failures << "inventory mapping differs: #{item["id"]}" unless task["inventory_item"] == item["id"]
  %w[id title type observable_outcome inventory_item validation].each { |field| failures << "task #{task["id"]} is missing #{field}" unless present?(task[field]) }
  %w[evidence_boundary paths required_paths].each { |field| failures << "task #{task["id"]} has invalid #{field}" unless task[field].is_a?(Array) && !task[field].empty? }
  failures << "task #{task["id"]} has invalid dependencies" unless task["dependencies"].is_a?(Array)
  failures << "task #{task["id"]} paths and required_paths differ" unless task["paths"] == task["required_paths"]
  failures << "task #{task["id"]} has duplicate required paths" unless Array(task["required_paths"]).uniq == Array(task["required_paths"])
  failures << "task #{task["id"]} has a directory required path" if Array(task["required_paths"]).any? { |path| path.end_with?("/") }
  failures << "task #{task["id"]} recursively invokes verification" if task["validation"].to_s.match?(/\bwork-verify\b|scripts\/verify-work\.rb/)
  expected_dependencies = index.zero? ? [] : [items[index - 1]["task"]]
  failures << "task dependency differs from serial predecessor: #{task["id"]}" unless task["dependencies"] == expected_dependencies
end

failures << "inventory maps tasks more than once" unless mapped.uniq == mapped
failures << "inventory does not map every task exactly once" unless mapped.to_set == task_index.keys.to_set
release_item = items.find { |item| item["id"] == "dora-v04-release" }
release_task = release_item && task_index[[release_item["plan"], release_item["task"]]]
failures << "release task must remain external-approval-gated" unless release_task && release_task["requires_external_approval"] == true

abort "Dora v0.4 operational plan audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora v0.4 operational plan audit passed (#{items.length} atomic serial items)."
