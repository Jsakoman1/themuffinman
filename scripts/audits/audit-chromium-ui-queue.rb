#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

master = YAML.load_file(ARGV.fetch(0))
inventory = YAML.load_file(ARGV.fetch(1))
items = inventory.fetch("items")
failures = []
failures << "master is not strict" unless master["strict_verification"] == true
failures << "master has no execution inventory" if master["execution_inventory"].to_s.empty?
failures << "inventory is not serial" unless inventory["serial_task_execution"] == true
failures << "inventory item ids are duplicated" unless items.map { |item| item.fetch("id") }.uniq.length == items.length
items.each do |item|
  plan = YAML.load_file(item.fetch("plan"))
  task = plan.fetch("tasks").find { |candidate| candidate.fetch("id") == item.fetch("task") }
  failures << "#{item.fetch("id")}: task mapping is missing" unless task
  next unless task
  %w[observable_outcome required_paths validation evidence_boundary].each do |field|
    value = task[field]
    failures << "#{item.fetch("id")}: #{field} is missing" if value.nil? || (value.respond_to?(:empty?) && value.empty?)
  end
  failures << "#{item.fetch("id")}: strict task has no exact required paths" if Array(task["required_paths"]).empty?
end
abort "Chromium UI queue audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Chromium UI queue audit passed (#{items.length} atomic items)."
