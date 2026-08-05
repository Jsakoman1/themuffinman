#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
FIXTURE = File.join(ROOT, "test/fixtures/agent-command-projects.yaml")

def run_command(*arguments)
  output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), *arguments)
  abort "agent command failed: #{arguments.join(" ")}\n#{output}" unless status.success?
  YAML.safe_load(output)
end

def configure_project(root, project)
  Dora::ProjectInitializer.initialize!(root, project_id: project.fetch("id"), manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief_path = File.join(root, "docs/product-brief.yaml")
  brief = YAML.load_file(brief_path).merge("product" => project.fetch("product"), "unanswered_decisions" => [project.fetch("open_decision")])
  File.write(brief_path, YAML.dump(brief))
  plan_path = "docs/work/first.yaml"
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => project.fetch("task"), "title" => "Record a garden note", "observable_outcome" => "A garden note is recorded.", "dependencies" => [], "required_paths" => ["docs/note.md"], "validation" => "true", "evidence_boundary" => ["garden note fixture"]}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "first", "plan" => plan_path, "task" => project.fetch("task"), "status" => "pending"}]}
  impact = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "note", "path_prefixes" => ["docs/"], "validations" => ["true"], "documentation" => ["docs/note.md"], "runtime_evidence" => ["garden-note-fixture"], "decisions" => [project.fetch("node")]}]}
  system_map = {"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => project.fetch("node")}], "edges" => []}
  File.write(File.join(root, plan_path), YAML.dump(plan))
  File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump(inventory))
  File.write(File.join(root, ".dora/change-impact.yaml"), YAML.dump(impact))
  File.write(File.join(root, ".dora/controls/system-map.yaml"), YAML.dump(system_map))
end

Dir.mktmpdir("dora-agent-command-consumer") do |sandbox|
  projects = YAML.load_file(FIXTURE).fetch("projects")
  roots = projects.to_h do |project|
    root = File.join(sandbox, project.fetch("id"))
    configure_project(root, project)
    [project.fetch("id"), root]
  end

  projects.each do |project|
    root = roots.fetch(project.fetch("id"))
    adapter = File.join(root, ".dora/project.yaml")
    context = run_command("agent-context", adapter, "docs/work/first.yaml", project.fetch("task"))
    next_action = run_command("agent-next", adapter, "docs/work/inventory.yaml")
    status = run_command("status", adapter, "docs/work/inventory.yaml")
    impact = run_command("impact", adapter, project.fetch("node"))
    closeout = run_command("agent-closeout", adapter, "docs/work/first.yaml", project.fetch("task"), ".dora/change-impact.yaml", "docs/note.md")
    output = [context, next_action, status, impact, closeout].to_yaml

    abort "agent context crossed projects" unless context.dig("knowledge", "product", "product") == project.fetch("product")
    abort "agent next crossed projects" unless next_action.slice("action", "task") == {"action" => "start", "task" => project.fetch("task")}
    abort "agent status crossed projects" unless status.fetch("open_decisions") == [project.fetch("open_decision")]
    abort "agent impact crossed projects" unless impact.fetch("changed") == [project.fetch("node")]
    abort "agent closeout crossed projects" unless closeout.dig("task", "id") == project.fetch("task") && !closeout.dig("completion", "verified")
    other_product = (projects - [project]).fetch(0).fetch("product")
    abort "agent command output leaked another project" if output.include?(other_product)
  end
end

puts "Dora independent agent command surface consumer test passed (two projects receive only their own documented guidance)."
