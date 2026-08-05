#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_closeout"
require_relative "../lib/dora/agent_context"
require_relative "../lib/dora/agent_next"
require_relative "../lib/dora/analysis_cache"
require_relative "../lib/dora/change_impact"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_intake"
require_relative "../lib/dora/project_status"

ROOT = File.expand_path("..", __dir__)
IDEA = File.join(ROOT, "test/fixtures/accelerated-project-idea.yaml")

Dir.mktmpdir("dora-accelerated-consumer") do |sandbox|
  root = File.join(sandbox, "garden-journal")
  idea = YAML.load_file(IDEA)
  Dora::ProjectInitializer.initialize!(root, project_id: "garden-journal", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  product = YAML.load_file(File.join(ROOT, "templates/product-brief.yaml"))
  product.merge!("product" => idea.fetch("product"), "user_problem" => idea.fetch("user_problem"), "primary_users" => [idea.fetch("primary_user")], "intended_outcomes" => ["A member can find a planting decision."], "non_goals" => [idea.fetch("non_goal")], "assumptions" => ["Members consent to shared notes."], "risks" => ["Members may not record planting decisions."], "unanswered_decisions" => [idea.fetch("open_decision")])
  domain = YAML.load_file(File.join(ROOT, "templates/domain-library.yaml"))
  domain.merge!("vocabulary" => [{"id" => "planting-note", "description" => "A shared record of one planting decision."}], "entities" => [{"id" => "planting-note", "description" => "A note owned by one garden group."}], "invariants" => [{"id" => "group-owner", "description" => "A planting note belongs to one group."}], "permission_rules" => [{"id" => "record", "actor" => "member", "action" => "record", "boundary" => "their garden group"}], "workflows" => [{"id" => "planting-note", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "recorded", "action" => "record"}]}], "acceptance_scenarios" => [{"id" => "record", "given" => "a garden member", "when" => "they record a planting note", "then" => "the group can read it"}])
  profile = YAML.load_file(File.join(ROOT, "templates/agent-project-profile.yaml"))
  profile["stack_commands"] = [{"id" => "test", "command" => "true", "purpose" => "Run the declared fixture test."}]
  knowledge = Dora::ProjectIntake.build!({"kind" => "dora_project_intake", "version" => 1, "product_brief" => product, "domain_library" => domain, "agent_profile" => profile})
  File.write(File.join(root, "docs/product-brief.yaml"), YAML.dump(knowledge.fetch("product_brief")))
  File.write(File.join(root, "docs/domain-library.yaml"), YAML.dump(knowledge.fetch("domain_library")))
  File.write(File.join(root, ".dora/agent-project-profile.yaml"), YAML.dump(knowledge.fetch("agent_profile")))

  plan_path = "docs/work/record-note.yaml"
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => "record-note", "title" => "Record a planting note", "observable_outcome" => "A first planting-note guide exists.", "dependencies" => [], "required_paths" => ["docs/planting-note.md"], "validation" => "true", "evidence_boundary" => ["garden note fixture"]}]}
  File.write(File.join(root, plan_path), YAML.dump(plan))
  File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump({"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "record-note", "plan" => plan_path, "task" => "record-note", "status" => "pending"}]}))
  impact_path = ".dora/change-impact.yaml"
  File.write(File.join(root, impact_path), YAML.dump({"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "documentation", "path_prefixes" => ["docs/"], "validations" => ["true"], "documentation" => ["docs/planting-note.md"], "runtime_evidence" => ["garden-note-fixture"], "decisions" => ["GARDEN-1"]}]}))

  context = Dora::AgentContext.build!(project_root: root, work_plan: plan_path, task_id: "record-note")
  next_action = Dora::AgentNext.next!(project_root: root, inventory_path: "docs/work/inventory.yaml")
  status = Dora::ProjectStatus.report!(adapter_path: File.join(root, ".dora/project.yaml"), inventory_path: "docs/work/inventory.yaml", adapter_schema_path: File.join(ROOT, "project-adapter.schema.yaml"), control_schema_path: File.join(ROOT, "project-control.schema.yaml"))
  impact = Dora::ChangeImpact.assess!(File.join(root, impact_path), ["docs/planting-note.md"])
  cache_calls = 0
  first_cache = Dora::AnalysisCache.fetch!(cache_root: File.join(root, ".dora/cache"), key: "garden-analysis", input: {"path" => "docs/planting-note.md"}) { cache_calls += 1; {"notes" => 1} }
  second_cache = Dora::AnalysisCache.fetch!(cache_root: File.join(root, ".dora/cache"), key: "garden-analysis", input: {"path" => "docs/planting-note.md"}) { cache_calls += 1; {"notes" => 2} }
  closeout = Dora::AgentCloseout.review!(project_root: root, work_plan: plan_path, task_id: "record-note", impact_path: impact_path, changed_paths: ["docs/planting-note.md"])

  abort "consumer context lost its product" unless context.dig("knowledge", "product", "product") == "Garden journal"
  abort "consumer next action is not bounded" unless next_action.slice("action", "item") == {"action" => "start", "item" => "record-note"}
  abort "consumer status lost its open decision" unless status.fetch("open_decisions") == [idea.fetch("open_decision")]
  abort "consumer impact lost declared evidence" unless impact.fetch("runtime_evidence") == ["garden-note-fixture"]
  abort "consumer cache did not accelerate repeat analysis" unless !first_cache.dig("cache", "hit") && second_cache.dig("cache", "hit") && cache_calls == 1
  abort "consumer closeout made a completion claim" if closeout.dig("completion", "verified") || closeout.dig("completion", "status_mutation")
  content = Dir[File.join(root, "{.dora,docs,AGENTS.md}/**/*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "accelerated consumer refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent accelerated consumer test passed (fresh project uses context, next, status, impact, cache, and closeout without MuffinMan data)."
