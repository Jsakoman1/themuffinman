#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/domain_library"

library = {"kind" => "dora_domain_library", "version" => 1, "vocabulary" => [{"id" => "circle", "description" => "A bounded group."}], "entities" => [{"id" => "request", "description" => "A user-owned request."}], "invariants" => [{"id" => "owner-required", "description" => "Every request has an owner."}], "permission_rules" => [{"id" => "owner-edit", "actor" => "owner", "action" => "edit", "boundary" => "own request"}], "workflows" => [{"id" => "request-life", "initial_state" => "draft", "transitions" => [{"from" => "draft", "to" => "submitted", "action" => "submit"}]}], "acceptance_scenarios" => [{"id" => "submit-request", "given" => "an owner has a draft", "when" => "the owner submits", "then" => "the request becomes submitted"}]}
Dir.mktmpdir("dora-domain-library") do |root|
  path = File.join(root, "domain-library.yaml")
  File.write(path, YAML.dump(library))
  loaded = Dora::DomainLibrary.load!(path)
  abort "domain library did not retain workflow state" unless loaded.fetch("workflows").first.fetch("initial_state") == "draft"
  library.fetch("workflows").first["transitions"] = []
  File.write(path, YAML.dump(library))
  begin
    Dora::DomainLibrary.load!(path)
    abort "domain library accepted an empty workflow"
  rescue ArgumentError => error
    abort "wrong domain library failure: #{error.message}" unless error.message.include?("transitions")
  end
end

puts "Dora domain library test passed (vocabulary, invariants, permissions, workflows, and acceptance scenarios)."
