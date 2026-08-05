#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/project_new"

ROOT = File.expand_path("..", __dir__)

def intake
  product = YAML.load_file(File.join(ROOT, "templates/product-brief.yaml"))
  product.merge!("product" => "Garden journal", "user_problem" => "A group needs a shared planting record.", "primary_users" => ["Garden member"], "intended_outcomes" => ["Members can find a planting decision."], "non_goals" => ["Do not build a marketplace."], "assumptions" => ["Members agree to share notes."], "risks" => ["Members may not record decisions."], "unanswered_decisions" => ["Who can archive a note?"])
  domain = YAML.load_file(File.join(ROOT, "templates/domain-library.yaml"))
  domain.merge!("vocabulary" => [{"id" => "planting-note", "description" => "A record of one planting decision."}], "entities" => [{"id" => "planting-note", "description" => "A group-owned note."}], "invariants" => [{"id" => "group-owner", "description" => "A note belongs to one group."}], "permission_rules" => [{"id" => "record", "actor" => "member", "action" => "record", "boundary" => "their group"}], "workflows" => [{"id" => "planting-note", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "recorded", "action" => "record"}]}], "acceptance_scenarios" => [{"id" => "record", "given" => "a member", "when" => "they record a note", "then" => "their group can read it"}])
  profile = YAML.load_file(File.join(ROOT, "templates/agent-project-profile.yaml"))
  profile["stack_commands"] = [{"id" => "test", "command" => "true", "purpose" => "Run the declared fixture test."}]
  {"kind" => "dora_project_intake", "version" => 1, "product_brief" => product, "domain_library" => domain, "agent_profile" => profile}
end

source = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "/reviewed/dora", "ref" => "a" * 40, "checksum" => "b" * 64}, "review" => {"id" => "review-1", "reviewed_by" => "fixture", "reviewed_at" => "2026-08-05T00:00:00Z"}}
answers = {"kind" => "dora_project_new", "version" => 1, "project_id" => "garden-journal", "intake" => intake, "dora_source" => source, "first_work" => {"id" => "record-note", "title" => "Record a planting note", "observable_outcome" => "A planting-note guide exists.", "required_paths" => ["docs/planting-note.md"], "validation" => "true", "evidence_boundary" => ["source contract fixture"]}}

result = Dora::ProjectNew.validate!(answers)
abort "source contract lost immutable provenance" unless result.dig("dora_source", "source", "checksum") == "b" * 64

tampered = Marshal.load(Marshal.dump(answers))
tampered.dig("dora_source", "source")["checksum"] = "tampered"
begin
  Dora::ProjectNew.validate!(tampered)
  abort "source contract accepted tampered checksum"
rescue ArgumentError
end

schema = YAML.load_file(File.join(ROOT, "project-new.schema.yaml"))
abort "project-new schema does not require dora_source" unless schema.fetch("required_fields").include?("dora_source")
bootstrap_schema = YAML.load_file(File.join(ROOT, "bootstrap-source.schema.yaml"))
abort "bootstrap source schema does not require review" unless bootstrap_schema.fetch("required").include?("review")

puts "Dora self-contained source contract test passed (reviewed local source and immutable provenance are explicit)."
