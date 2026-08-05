#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_closeout"
require_relative "../lib/dora/agent_context"
require_relative "../lib/dora/agent_next"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
FIXTURE = File.join(ROOT, "test/fixtures/agent-feedback-project.yaml")

Dir.mktmpdir("dora-agent-feedback-consumer") do |sandbox|
  fixture = YAML.load_file(FIXTURE)
  outputs = fixture.fetch("projects").map do |project|
    root = File.join(sandbox, project.fetch("id"))
    Dora::ProjectInitializer.initialize!(root, project_id: project.fetch("id"), manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
    brief_path = File.join(root, "docs/product-brief.yaml")
    brief = YAML.load_file(brief_path)
    brief["product"] = project.fetch("product")
    brief["user_problem"] = project.fetch("user_problem")
    File.write(brief_path, YAML.dump(brief))

    plan_path = "docs/work/#{project.fetch("id")}.yaml"
    plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => project.fetch("task_id"), "title" => "Record declared guidance", "observable_outcome" => "Project guidance is bounded.", "dependencies" => [], "required_paths" => [project.fetch("required_path")], "validation" => "true", "evidence_boundary" => ["project-specific feedback"]}]}
    File.write(File.join(root, plan_path), YAML.dump(plan))
    inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => project.fetch("task_id"), "plan" => plan_path, "task" => project.fetch("task_id"), "status" => "pending"}]}
    File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump(inventory))
    impact_path = ".dora/change-impact.yaml"
    impact = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "documentation", "path_prefixes" => ["docs/"], "validations" => ["true"], "documentation" => [project.fetch("documentation")], "runtime_evidence" => [], "decisions" => [project.fetch("decision")]}]}
    File.write(File.join(root, impact_path), YAML.dump(impact))

    context = Dora::AgentContext.build!(project_root: root, work_plan: plan_path, task_id: project.fetch("task_id"))
    next_action = Dora::AgentNext.next!(project_root: root, inventory_path: "docs/work/inventory.yaml")
    closeout = Dora::AgentCloseout.review!(project_root: root, work_plan: plan_path, task_id: project.fetch("task_id"), impact_path: impact_path, changed_paths: [project.fetch("required_path")])
    abort "agent-next did not select the project task" unless next_action.slice("action", "item") == {"action" => "start", "item" => project.fetch("task_id")}
    abort "context crossed project boundaries" unless context.dig("knowledge", "product", "product") == project.fetch("product")
    abort "closeout lost project-specific decision" unless closeout.dig("impact_obligations", "decisions") == [project.fetch("decision")]
    [context.dig("knowledge", "product", "product"), closeout.dig("impact_obligations", "documentation")]
  end
  abort "independent projects received identical feedback" unless outputs.uniq.length == 2
end

puts "Dora independent agent feedback consumer test passed (two projects receive bounded, declared, different guidance)."
