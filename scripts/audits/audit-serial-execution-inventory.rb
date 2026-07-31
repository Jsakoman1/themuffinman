#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require "yaml"

master_path = ARGV.fetch(0)
inventory_path = ARGV.fetch(1)
master = YAML.load_file(master_path)
inventory = YAML.load_file(inventory_path)
failures = []

items = Array(inventory["items"])
sequences = items.map { |item| item["sequence"] }
ids = items.map { |item| item["id"] }
failures << "inventory sequences must be contiguous from 1" unless sequences == (1..items.length).to_a
failures << "inventory ids must be unique" unless ids.uniq.length == ids.length

children = Array(master["children"])
inventory_plans = items.map { |item| item["plan"] }.uniq
failures << "inventory references a non-child plan" unless (inventory_plans - children).empty?

inventory_keys = []
items.each_with_index do |item, index|
  plan_path = item["plan"]
  plan = YAML.load_file(plan_path)
  task = Array(plan["tasks"]).find { |candidate| candidate["id"] == item["task"] }
  unless task
    failures << "missing task mapping for #{item["id"]}"
    next
  end

  inventory_keys << [plan_path, task["id"]]
  failures << "task inventory_item mismatch for #{item["id"]}" unless task["inventory_item"] == item["id"]
  expected_dependencies = index.zero? ? [] : [items[index - 1]["id"]]
  failures << "dependency mismatch for #{item["id"]}" unless Array(task["dependencies"]) == expected_dependencies

  required_paths = Array(task["required_paths"])
  failures << "required_paths missing for #{item["id"]}" if required_paths.empty?
  failures << "required_paths duplicated for #{item["id"]}" unless required_paths.uniq.length == required_paths.length
  failures << "paths and required_paths differ for #{item["id"]}" unless Array(task["paths"]) == required_paths
  failures << "evidence boundary missing for #{item["id"]}" if Array(task["evidence_boundary"]).empty?

  validation = task["validation"].to_s
  failures << "leaf validation missing for #{item["id"]}" if validation.empty?
  failures << "recursive validation for #{item["id"]}" if validation.match?(/\bwork-verify\b|scripts\/verify-work\.rb/)
end

child_keys = children.flat_map do |plan_path|
  plan = YAML.load_file(plan_path)
  Array(plan["tasks"]).map { |task| [plan_path, task["id"]] }
end
failures << "child tasks and inventory mappings differ" unless child_keys.to_set == inventory_keys.to_set
failures << "inventory task mappings must be unique" unless inventory_keys.uniq.length == inventory_keys.length

abort "Serial execution inventory audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?

puts "Serial execution inventory audit passed (#{items.length} strict items)."
