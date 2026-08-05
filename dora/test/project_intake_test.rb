#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/project_intake"

root = File.expand_path("..", __dir__)
product = YAML.load_file(File.join(root, "templates/product-brief.yaml"))
product.merge!("product" => "Shared notes", "user_problem" => "A group needs visible decisions.", "primary_users" => ["Group member"], "intended_outcomes" => ["A member can find a decision."], "non_goals" => ["Do not build chat."], "assumptions" => ["Members agree to share notes."], "risks" => ["Members may not record decisions."], "unanswered_decisions" => ["Who can archive a note?"])
domain = YAML.load_file(File.join(root, "templates/domain-library.yaml"))
domain.merge!("vocabulary" => [{"id" => "note", "description" => "A shared decision record."}], "entities" => [{"id" => "note", "description" => "A group-owned record."}], "invariants" => [{"id" => "owner", "description" => "A note belongs to one group."}], "permission_rules" => [{"id" => "write", "actor" => "member", "action" => "write", "boundary" => "their group"}], "workflows" => [{"id" => "note", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "published", "action" => "publish"}]}], "acceptance_scenarios" => [{"id" => "publish", "given" => "a draft note", "when" => "a member publishes it", "then" => "the group can read it"}])
profile = YAML.load_file(File.join(root, "templates/agent-project-profile.yaml"))
profile["stack_commands"] = [{"id" => "test", "command" => "ruby test.rb", "purpose" => "Validate note behavior."}]

knowledge = Dora::ProjectIntake.build!({"kind" => "dora_project_intake", "version" => 1, "product_brief" => product, "domain_library" => domain, "agent_profile" => profile})
abort "intake changed an explicit product answer" unless knowledge.dig("product_brief", "product") == "Shared notes"
abort "intake invented a domain rule" unless knowledge.dig("domain_library", "invariants").map { |item| item.fetch("id") } == ["owner"]
abort "intake does not declare its non-invention boundary" unless knowledge.fetch("invention") == "none"
begin
  Dora::ProjectIntake.build!({"kind" => "dora_project_intake", "version" => 1, "product_brief" => product})
  abort "intake accepted missing explicit answer groups"
rescue ArgumentError
end

puts "Dora project intake test passed (explicit valid knowledge with no inferred product rules)."
