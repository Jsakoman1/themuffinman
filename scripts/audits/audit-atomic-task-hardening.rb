#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

master_path = ARGV.fetch(0)
inventory_path = ARGV.fetch(1)
master = YAML.load_file(master_path)
inventory = YAML.load_file(inventory_path)
failures = []

children = Array(master["children"])
inventory_items = Array(inventory["items"])
ids = inventory_items.map { |item| item["id"] }
failures << "inventory sequence is not contiguous" unless inventory_items.map { |item| item["sequence"] }.sort == (0...inventory_items.length).to_a
failures << "inventory item ids are duplicated" unless ids.uniq.length == ids.length

inventory_items.each do |item|
  plan_path = item["plan"]
  plan = YAML.load_file(plan_path)
  task = Array(plan["tasks"]).find { |candidate| candidate["id"] == item["task"] }
  failures << "missing task mapping: #{item["id"]}" unless task
  next unless task
  failures << "missing outcome: #{plan_path}##{task["id"]}" if task["observable_outcome"].to_s.strip.empty?
  failures << "missing paths: #{plan_path}##{task["id"]}" if Array(task["required_paths"]).empty?
  failures << "missing evidence boundary: #{plan_path}##{task["id"]}" if Array(task["evidence_boundary"]).empty?
  failures << "missing leaf validation: #{plan_path}##{task["id"]}" if task["validation"].to_s.strip.empty?
end

failures << "backend parity plan is not a master child" unless children.include?("docs/work/frontend-structural-redesign-backend-api-parity.yaml")
abort "Atomic task hardening audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Atomic task hardening audit passed (#{inventory_items.length} inventory items)."
