#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_next"

Dir.mktmpdir("dora-agent-next") do |root|
  FileUtils.mkdir_p(File.join(root, "docs/work"))
  %w[first second].each { |name| File.write(File.join(root, "docs/work/#{name}.yaml"), "kind: work\nversion: 1\n") }
  path = File.join(root, "docs/work/inventory.yaml")
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "first", "plan" => "docs/work/first.yaml", "task" => "first-task", "status" => "verified"}, {"id" => "second", "plan" => "docs/work/second.yaml", "task" => "second-task", "status" => "pending"}]}
  File.write(path, YAML.dump(inventory))
  result = Dora::AgentNext.next!(project_root: root, inventory_path: "docs/work/inventory.yaml")
  abort "agent-next did not select the first eligible item" unless result.slice("action", "item") == {"action" => "start", "item" => "second"}
  inventory.fetch("items").last["status"] = "in_progress"
  File.write(path, YAML.dump(inventory))
  abort "agent-next did not retain the active item" unless Dora::AgentNext.next!(project_root: root, inventory_path: "docs/work/inventory.yaml").fetch("action") == "continue"
end

puts "Dora agent-next test passed (first eligible item or the sole declared active item)."
