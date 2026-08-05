#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

def answers_for(source)
  product = YAML.load_file(File.join(ROOT, "templates/product-brief.yaml"))
  product.merge!("product" => "Garden journal", "user_problem" => "A group needs a shared planting record.", "primary_users" => ["Garden member"], "intended_outcomes" => ["Members can find a planting decision."], "non_goals" => ["Do not build a marketplace."], "assumptions" => ["Members agree to share notes."], "risks" => ["Members may not record decisions."], "unanswered_decisions" => ["Who can archive a note?"])
  domain = YAML.load_file(File.join(ROOT, "templates/domain-library.yaml"))
  domain.merge!("vocabulary" => [{"id" => "planting-note", "description" => "A record of one planting decision."}], "entities" => [{"id" => "planting-note", "description" => "A group-owned note."}], "invariants" => [{"id" => "group-owner", "description" => "A note belongs to one group."}], "permission_rules" => [{"id" => "record", "actor" => "member", "action" => "record", "boundary" => "their group"}], "workflows" => [{"id" => "planting-note", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "recorded", "action" => "record"}]}], "acceptance_scenarios" => [{"id" => "record", "given" => "a member", "when" => "they record a note", "then" => "their group can read it"}])
  profile = YAML.load_file(File.join(ROOT, "templates/agent-project-profile.yaml"))
  profile["stack_commands"] = [{"id" => "test", "command" => "true", "purpose" => "Run the declared fixture test."}]
  {"kind" => "dora_project_new", "version" => 1, "project_id" => "garden-journal", "intake" => {"kind" => "dora_project_intake", "version" => 1, "product_brief" => product, "domain_library" => domain, "agent_profile" => profile}, "dora_source" => source, "first_work" => {"id" => "record-note", "title" => "Record a planting note", "observable_outcome" => "A planting-note guide exists.", "required_paths" => ["docs/planting-note.md"], "validation" => "true", "evidence_boundary" => ["self-contained new fixture"]}}
end

Dir.mktmpdir("dora-self-contained-new") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  descriptor = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => source, "ref" => "a" * 40, "checksum" => Dora::BootstrapSource.send(:checksum_for, source)}, "review" => {"id" => "review-1", "reviewed_by" => "fixture", "reviewed_at" => "2026-08-05T00:00:00Z"}}
  answers_path = File.join(sandbox, "answers.yaml")
  destination = File.join(sandbox, "garden-journal")
  File.write(answers_path, YAML.dump(answers_for(descriptor)))

  output, status = Open3.capture2e(CLI, "new", destination, "--answers", answers_path)
  abort "self-contained project creation failed: #{output}" unless status.success?
  abort "new project did not copy Dora" unless File.executable?(File.join(destination, "dora/bin/dora"))
  record = YAML.load_file(File.join(destination, ".dora/bootstrap-source.yaml"))
  abort "new project did not preserve verified source provenance" unless record.dig("source", "source", "checksum") == descriptor.dig("source", "checksum") && record.dig("source", "review", "id") == "review-1"

  output, status = Open3.capture2e(File.join(destination, "bin/dora"), "help", chdir: destination)
  abort "generated local launcher is unusable: #{output}" unless status.success? && output.include?("dora new")
  abort "new project created product code" unless Dir[File.join(destination, "apps/**/*")].empty?
end

puts "Dora self-contained new test passed (generated project has a usable local launcher and recorded source provenance)."
