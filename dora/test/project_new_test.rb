#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/project_new"

root = File.expand_path("..", __dir__)
product = YAML.load_file(File.join(root, "templates/product-brief.yaml"))
product.merge!("product" => "Garden journal", "user_problem" => "A group needs a shared planting record.", "primary_users" => ["Garden member"], "intended_outcomes" => ["Members can find a planting decision."], "non_goals" => ["Do not build a marketplace."], "assumptions" => ["Members agree to share notes."], "risks" => ["Members may not record decisions."], "unanswered_decisions" => ["Who can archive a note?"])
domain = YAML.load_file(File.join(root, "templates/domain-library.yaml"))
domain.merge!("vocabulary" => [{"id" => "planting-note", "description" => "A record of one planting decision."}], "entities" => [{"id" => "planting-note", "description" => "A group-owned note."}], "invariants" => [{"id" => "group-owner", "description" => "A note belongs to one group."}], "permission_rules" => [{"id" => "record", "actor" => "member", "action" => "record", "boundary" => "their group"}], "workflows" => [{"id" => "planting-note", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "recorded", "action" => "record"}]}], "acceptance_scenarios" => [{"id" => "record", "given" => "a member", "when" => "they record a note", "then" => "their group can read it"}])
profile = YAML.load_file(File.join(root, "templates/agent-project-profile.yaml"))
profile["stack_commands"] = [{"id" => "test", "command" => "true", "purpose" => "Run the declared fixture test."}]
source = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "/reviewed/dora", "ref" => "a" * 40, "checksum" => "b" * 64}, "review" => {"id" => "review-1", "reviewed_by" => "fixture", "reviewed_at" => "2026-08-05T00:00:00Z"}}
answers = {"kind" => "dora_project_new", "version" => 1, "project_id" => "garden-journal", "intake" => {"kind" => "dora_project_intake", "version" => 1, "product_brief" => product, "domain_library" => domain, "agent_profile" => profile}, "dora_source" => source, "first_work" => {"id" => "record-note", "title" => "Record a planting note", "observable_outcome" => "A planting-note guide exists.", "required_paths" => ["docs/planting-note.md"], "validation" => "true", "evidence_boundary" => ["planting note fixture"]}}
result = Dora::ProjectNew.validate!(answers)
abort "project new lost explicit first work" unless result.dig("first_work", "id") == "record-note"
abort "project new inferred product behavior" unless result.fetch("invention") == "none"
abort "project new lost reviewed Dora source" unless result.dig("dora_source", "review", "id") == "review-1"
begin
  Dora::ProjectNew.validate!(answers.merge("project_id" => "Garden Journal"))
  abort "project new accepted an invalid project id"
rescue ArgumentError
end

puts "Dora project new test passed (explicit valid answers create neutral project and first-work declarations)."
