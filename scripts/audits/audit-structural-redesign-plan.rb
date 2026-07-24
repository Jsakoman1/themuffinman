#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

master_path = ARGV.fetch(0)
inventory_path = ARGV.fetch(1)
master = YAML.load_file(master_path)
inventory = YAML.load_file(inventory_path)
failures = []

children = Array(master.fetch("children"))
child_set = children.to_set if false
children.each do |child_path|
  failures << "missing child plan: #{child_path}" unless File.file?(child_path)
  next unless File.file?(child_path)
  child = YAML.load_file(child_path)
  failures << "child master backlink mismatch: #{child_path}" unless child["master_plan"] == master_path
  failures << "child is not strict serial work: #{child_path}" unless child["kind"] == "work" && child["strict_verification"] == true && child["serial_task_execution"] == true
end

inventory_items = Array(inventory.fetch("items"))
inventory_ids = inventory_items.map { |item| item.fetch("id") }
failures << "inventory sequences are not contiguous" unless inventory_items.map { |item| item.fetch("sequence") }.sort == (0...inventory_items.length).to_a
failures << "inventory IDs are duplicated" unless inventory_ids.uniq.length == inventory_ids.length
failures << "inventory master backlink mismatch" unless inventory["master_plan"] == master_path

all_tasks = {}
children.each do |child_path|
  next unless File.file?(child_path)
  Array(YAML.load_file(child_path)["tasks"]).each do |task|
    id = task.fetch("id")
    failures << "duplicate task ID: #{id}" if all_tasks.key?(id)
    all_tasks[id] = [child_path, task]
  end
end

inventory_items.each_with_index do |item, index|
  plan_path = item.fetch("plan")
  task_id = item.fetch("task")
  plan = File.file?(plan_path) ? YAML.load_file(plan_path) : nil
  task = plan && Array(plan["tasks"]).find { |candidate| candidate["id"] == task_id }
  failures << "inventory task missing: #{item["id"]}" unless task
  next unless task
  failures << "inventory_item mismatch: #{plan_path}##{task_id}" unless task["inventory_item"] == item["id"]
  failures << "inventory task is not represented once: #{task_id}" unless all_tasks[task_id]
  Array(item["depends_on"]).each do |dependency|
    dependency_item = inventory_items.find { |candidate| candidate["id"] == dependency }
    failures << "unknown dependency #{dependency} for #{item["id"]}" unless dependency_item
    failures << "dependency order violation #{dependency} -> #{item["id"]}" if dependency_item && dependency_item["sequence"] >= item["sequence"]
  end
end

all_tasks.each do |task_id, (plan_path, task)|
  failures << "task missing inventory mapping: #{plan_path}##{task_id}" unless inventory_items.any? { |item| item["plan"] == plan_path && item["task"] == task_id }
  failures << "task missing observable outcome: #{plan_path}##{task_id}" if task["observable_outcome"].to_s.strip.empty?
  failures << "task missing exact paths: #{plan_path}##{task_id}" if Array(task["required_paths"]).empty?
  Array(task["required_paths"]).each do |required_path|
    failures << "required path is not file-scoped: #{plan_path}##{task_id}: #{required_path}" if required_path.end_with?("/") || (!required_path.include?(".") && File.directory?(required_path))
  end
  failures << "task missing evidence boundary: #{plan_path}##{task_id}" if Array(task["evidence_boundary"]).empty?
  failures << "task missing leaf validation: #{plan_path}##{task_id}" if task["validation"].to_s.strip.empty?
end

phase_children = Array(master["phases"]).map { |phase| phase["child"] }.compact
failures << "phase child not listed in master children: #{(phase_children - children).join(", ")}" unless (phase_children - children).empty?
failures << "VisionForWeb contract is not mandatory" unless master.dig("vision4web_contract", "status") == "mandatory-product-feature"
failures << "Apple product family contract is missing" unless master.dig("client_architecture_contract", "apple_product_family")

abort "Structural redesign plan audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Structural redesign plan audit passed (#{children.length} children, #{all_tasks.length} tasks, #{inventory_items.length} inventory items)."
