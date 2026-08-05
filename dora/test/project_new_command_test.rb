#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_memory"
require_relative "../lib/dora/bootstrap_source"

ROOT = File.expand_path("..", __dir__)

def valid_answers
  product = YAML.load_file(File.join(ROOT, "templates/product-brief.yaml"))
  product.merge!("product" => "Garden journal", "user_problem" => "A group needs a shared planting record.", "primary_users" => ["Garden member"], "intended_outcomes" => ["Members can find a planting decision."], "non_goals" => ["Do not build a marketplace."], "assumptions" => ["Members agree to share notes."], "risks" => ["Members may not record decisions."], "unanswered_decisions" => ["Who can archive a note?"])
  domain = YAML.load_file(File.join(ROOT, "templates/domain-library.yaml"))
  domain.merge!("vocabulary" => [{"id" => "planting-note", "description" => "A record of one planting decision."}], "entities" => [{"id" => "planting-note", "description" => "A group-owned note."}], "invariants" => [{"id" => "group-owner", "description" => "A note belongs to one group."}], "permission_rules" => [{"id" => "record", "actor" => "member", "action" => "record", "boundary" => "their group"}], "workflows" => [{"id" => "planting-note", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "recorded", "action" => "record"}]}], "acceptance_scenarios" => [{"id" => "record", "given" => "a member", "when" => "they record a note", "then" => "their group can read it"}])
  profile = YAML.load_file(File.join(ROOT, "templates/agent-project-profile.yaml"))
  profile["stack_commands"] = [{"id" => "test", "command" => "true", "purpose" => "Run the declared fixture test."}]
  source = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => ROOT, "ref" => "a" * 40, "checksum" => Dora::BootstrapSource.send(:checksum_for, ROOT)}, "review" => {"id" => "review-1", "reviewed_by" => "fixture", "reviewed_at" => "2026-08-05T00:00:00Z"}}
  {"kind" => "dora_project_new", "version" => 1, "project_id" => "garden-journal", "intake" => {"kind" => "dora_project_intake", "version" => 1, "product_brief" => product, "domain_library" => domain, "agent_profile" => profile}, "dora_source" => source, "first_work" => {"id" => "record-note", "title" => "Record a planting note", "observable_outcome" => "A planting-note guide exists.", "required_paths" => ["docs/planting-note.md"], "validation" => "true", "evidence_boundary" => ["planting note fixture"]}}
end

Dir.mktmpdir("dora-project-new-command") do |sandbox|
  answers_path = File.join(sandbox, "answers.yaml")
  destination = File.join(sandbox, "garden-journal")
  File.write(answers_path, YAML.dump(valid_answers))
  output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "new", destination, "--answers", answers_path)
  abort "project new command failed: #{output}" unless status.success?
  abort "project new did not write product knowledge" unless YAML.load_file(File.join(destination, "docs/product-brief.yaml")).fetch("product") == "Garden journal"
  plan = YAML.load_file(File.join(destination, "docs/work/first-work.yaml"))
  abort "project new did not declare first bounded work" unless plan.dig("tasks", 0, "id") == "record-note" && plan.fetch("baseline") == "pending_git_baseline"
  memory = Dora::ProjectMemory.load!(File.join(destination, "docs/project-memory.yaml"))
  abort "project new did not link first work in project memory" unless memory.dig("current_work", "task") == "record-note"
  abort "project new memory made a completion claim" unless memory.fetch("completion_boundary").include?("does not prove")
  abort "project new produced product implementation" if File.exist?(File.join(destination, "docs/planting-note.md"))
  abort "project new did not copy the reviewed Dora package" unless File.executable?(File.join(destination, "dora/bin/dora"))
end

puts "Dora project new command test passed (explicit answers create neutral knowledge, project memory, and first work without product implementation)."
