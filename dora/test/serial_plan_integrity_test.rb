#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/serial_plan_integrity"

def write(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
  path
end

Dir.mktmpdir("dora-serial-integrity") do |root|
  task = {"id" => "one", "inventory_item" => "one-item", "validation" => "ruby test/one.rb"}
  write(root, "plans/work.yaml", {"kind" => "work", "version" => 1, "tasks" => [task]})
  master = write(root, "plans/master.yaml", {"kind" => "master", "version" => 1, "children" => ["plans/work.yaml"]})
  inventory = write(root, "plans/inventory.yaml", {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "one-item", "order" => 1, "plan" => "plans/work.yaml", "task" => "one", "status" => "pending"}]})
  Dora::SerialPlanIntegrity.validate!(master, inventory, project_root: root)

  task["validation"] = "dora work-verify .dora/project.yaml plan=plans/work.yaml task=one"
  write(root, "plans/work.yaml", {"kind" => "work", "version" => 1, "tasks" => [task]})
  begin
    Dora::SerialPlanIntegrity.validate!(master, inventory, project_root: root)
    abort "recursive leaf validation passed"
  rescue ArgumentError => error
    abort "wrong integrity failure: #{error.message}" unless error.message.include?("recursive validation")
  end
end

puts "Dora serial plan integrity test passed (one-to-one mapping and recursion rejection)."
