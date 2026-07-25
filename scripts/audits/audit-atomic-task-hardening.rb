#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

master_path = ARGV.fetch(0, "docs/work/system-map-optimization-master.yaml")
inventory_path = ARGV.fetch(1, "docs/work/system-map-optimization-master-execution-inventory.yaml")
master = YAML.load_file(master_path)
inventory = YAML.load_file(inventory_path)
failures = []

children = Array(master["children"])
inventory_items = Array(inventory["items"])
ids = inventory_items.map { |item| item["id"] }
orders = inventory_items.map { |item| item["sequence"] || item["order"] }
failures << "inventory sequence is not contiguous" unless orders.sort == (1..inventory_items.length).to_a || orders.sort == (0...inventory_items.length).to_a
failures << "inventory item ids are duplicated" unless ids.uniq.length == ids.length

inventory_items.each do |item|
  plan_path = item["plan"]
  plan = YAML.load_file(plan_path)
  task = Array(plan["tasks"]).find { |candidate| candidate["id"] == item["task"] }
  failures << "missing task mapping: #{item["id"]}" unless task
  next unless task
  outcome = task["observable_outcome"].to_s.strip
  paths = Array(task["required_paths"] || task["paths"])
  evidence_boundary = Array(task["evidence_boundary"] || task["evidence"] || paths)
  failures << "missing outcome: #{plan_path}##{task["id"]}" if outcome.empty? && task["title"].to_s.strip.empty?
  failures << "missing paths: #{plan_path}##{task["id"]}" if paths.empty?
  failures << "missing evidence boundary: #{plan_path}##{task["id"]}" if evidence_boundary.empty?
  failures << "missing leaf validation: #{plan_path}##{task["id"]}" if task["validation"].to_s.strip.empty?
end

abort "Atomic task hardening audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Atomic task hardening audit passed (#{inventory_items.length} inventory items)."
