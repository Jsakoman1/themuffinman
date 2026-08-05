#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
Dir.mktmpdir("dora-next-diagnose") do |project|
  _output, status = Open3.capture2e(CLI, "init", project, "--project", "guided-project")
  abort "cannot initialize command fixture" unless status.success?
  plan = {"kind" => "work", "version" => 1, "id" => "first", "tasks" => [{"id" => "implement-first"}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "first", "plan" => "docs/work/first.yaml", "task" => "implement-first", "status" => "pending"}]}
  File.write(File.join(project, "docs/work/first.yaml"), YAML.dump(plan))
  File.write(File.join(project, "docs/work/inventory.yaml"), YAML.dump(inventory))
  output, status = Open3.capture2e(CLI, "next", File.join(project, ".dora/project.yaml"), "docs/work/inventory.yaml", "--format", "json")
  abort output unless status.success?
  next_action = JSON.parse(output)
  abort "next command did not preserve the start boundary" unless next_action.dig("payload", "action") == "start" && next_action.fetch("side_effect") == "read_only"
  abort "next command omitted its declared inventory citation" unless next_action.fetch("citations").include?("docs/work/inventory.yaml")
  output, status = Open3.capture2e(CLI, "diagnose", File.join(project, ".dora/project.yaml"), "--format", "json")
  abort output unless status.success?
  diagnosis = JSON.parse(output)
  abort "diagnose command did not report health" unless diagnosis.dig("payload", "kind") == "dora_diagnosis" && diagnosis.fetch("side_effect") == "read_only"
end
puts "Dora next and diagnose command test passed (safe next action and compact diagnosis are read-only)."
