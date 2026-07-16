#!/usr/bin/env ruby

require "yaml"

ROOT = File.expand_path("../..", __dir__)
master_path = File.join(ROOT, "docs/work/capability-coverage-and-reliability.yaml")
inventory_path = File.join(ROOT, "docs/capability-inventory.yaml")
master = YAML.load_file(master_path)
inventory = YAML.load_file(inventory_path)

failures = []
children = Array(master.fetch("children"))
children.each do |child|
  child_path = File.join(ROOT, child)
  failures << "master child missing: #{child}" unless File.file?(child_path)
  next unless File.file?(child_path)
  child_plan = YAML.load_file(child_path)
  failures << "child master_plan mismatch: #{child}" unless child_plan["master_plan"] == "docs/work/capability-coverage-and-reliability.yaml"
  failures << "child capability_ids missing: #{child}" if Array(child_plan["capability_ids"]).empty?
end

active_plan_paths = Array(inventory.fetch("active_plans")).map { |entry| entry.fetch("path") }
active_plan_paths.each do |path|
  failures << "active inventory plan missing: #{path}" unless File.file?(File.join(ROOT, path))
  unless path == "docs/work/capability-coverage-and-reliability.yaml" || children.include?(path)
    failures << "active inventory plan not listed by master: #{path}"
  end
end

if active_plan_paths.uniq.length != active_plan_paths.length
  failures << "active inventory plan paths contain duplicates"
end

abort "Plan coverage audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Plan coverage audit passed (#{children.length} master children, #{active_plan_paths.length} active inventory plans)."
