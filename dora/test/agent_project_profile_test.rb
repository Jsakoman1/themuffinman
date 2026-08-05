#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/agent_project_profile"

profile = {"kind" => "dora_agent_project_profile", "version" => 1, "entrypoints" => [{"id" => "guide", "path" => "AGENTS.md", "purpose" => "Read first."}], "canonical_knowledge" => [{"id" => "brief", "path" => "docs/product-brief.yaml", "purpose" => "Understand the product."}], "stack_commands" => [{"id" => "test", "command" => "make test", "purpose" => "Validate behavior."}], "authority_limits" => [{"id" => "external", "rule" => "Require approval for external changes."}], "evidence_requirements" => [{"id" => "leaf", "rule" => "Run the leaf validation."}], "implementation_order" => [{"id" => "understand", "rule" => "Read knowledge first."}, {"id" => "verify", "rule" => "Verify last."}]}
Dir.mktmpdir("dora-agent-profile") do |root|
  path = File.join(root, "agent-project-profile.yaml")
  File.write(path, YAML.dump(profile))
  loaded = Dora::AgentProjectProfile.load!(path)
  abort "agent profile lost its safe implementation order" unless loaded.fetch("implementation_order").map { |item| item.fetch("id") } == %w[understand verify]
  profile.fetch("canonical_knowledge").first["path"] = "../outside.yaml"
  File.write(path, YAML.dump(profile))
  begin
    Dora::AgentProjectProfile.load!(path)
    abort "agent profile accepted an unsafe knowledge path"
  rescue ArgumentError => error
    abort "wrong agent profile failure: #{error.message}" unless error.message.include?("invalid path")
  end
end

puts "Dora agent project profile test passed (entrypoints, authority, evidence, and safe work order)."
