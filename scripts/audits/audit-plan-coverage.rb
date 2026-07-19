#!/usr/bin/env ruby

require "yaml"

ROOT = File.expand_path("../..", __dir__)
inventory_path = File.join(ROOT, "docs/capability-inventory.yaml")
inventory = YAML.load_file(inventory_path)

failures = []
open_plan_entries = Array(inventory.fetch("open_plans"))
open_plan_paths = open_plan_entries.map { |entry| entry.fetch("path") }
open_plan_entries.each do |entry|
  path = entry.fetch("path")
  full_path = File.join(ROOT, path)
  failures << "open inventory plan missing: #{path}" unless File.file?(full_path)
  next unless File.file?(full_path)
  plan = YAML.load_file(full_path)
  failures << "open inventory plan is not active or planned: #{path}" unless ["active", "planned"].include?(plan["status"])
end

if open_plan_paths.uniq.length != open_plan_paths.length
  failures << "open inventory plan paths contain duplicates"
end

abort "Plan coverage audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Plan coverage audit passed (#{open_plan_paths.length} open inventory plans)."
