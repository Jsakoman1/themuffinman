#!/usr/bin/env ruby
# frozen_string_literal: true

# Goal-pursuit leaf-gate expansion reviewed 2026-07-24.

require "yaml"

expansion_path = ARGV.fetch(0)
inventory_path = ARGV.fetch(1)
expansion = YAML.load_file(expansion_path)
inventory = YAML.load_file(inventory_path)
failures = []
failures << "leaf task naming rule is missing" if expansion["leaf_task_naming"].to_s.strip.empty?
failures << "leaf path scope rule is missing" if expansion["leaf_path_scope_rule"].to_s.strip.empty?

inventory_items = Array(inventory.fetch("items"))
inventory_ids = inventory_items.map { |item| item.fetch("id") }
expansion_items = Array(expansion.fetch("items"))
expansion_ids = expansion_items.map { |item| item.fetch("inventory_item") }
failures << "leaf expansion contains duplicate inventory items" unless expansion_ids.uniq.length == expansion_ids.length
failures << "leaf expansion does not cover every inventory item" unless expansion_ids.sort == inventory_ids.sort

templates = expansion.fetch("leaf_templates")
planning_templates = expansion.fetch("planning_leaf_templates")
expansion_items.each do |item|
  id = item.fetch("inventory_item")
  templates_for_item = item.fetch("kind") == "planning" ? planning_templates : templates
  required = item.fetch("required_leaf_steps")
  missing = required - templates_for_item.keys
  failures << "#{id}: unknown leaf templates #{missing.join(", ")}" unless missing.empty?
  failures << "#{id}: scope is missing" if item.fetch("scope").to_s.strip.empty?
  failures << "#{id}: required leaf steps are empty" if item.fetch("required_leaf_steps").empty?
  task = inventory_items.find { |candidate| candidate.fetch("id") == id }
  failures << "#{id}: no inventory task" unless task
  next unless task
  failures << "#{id}: leaf expansion plan differs from inventory plan" unless item.fetch("plan") == task.fetch("plan")
  failures << "#{id}: leaf expansion task differs from inventory task" unless item.fetch("parent_task") == task.fetch("task")
  required.each do |step|
    template = templates_for_item.fetch(step)
    %w[outcome validation evidence].each do |field|
      failures << "#{id}/#{step}: missing #{field}" if template.fetch(field).to_s.strip.empty?
    end
  end
end

abort "Apple leaf expansion audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Apple leaf expansion audit passed (#{expansion_items.length} parents)."
