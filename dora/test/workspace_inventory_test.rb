#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "yaml"
require_relative "../lib/dora/workspace_inventory"
Dir.mktmpdir("dora-workspace-inventory") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml"); beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => "source", "path_prefixes" => ["src/"]}]}))
  File.write(beta, YAML.dump({"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => "notes", "path_prefixes" => ["notes/"]}]}))
  abort "alpha classification failed" unless Dora::WorkspaceInventory.classify!(alpha, ["src/a.rb", "docs/a.md"]) == {"src/a.rb" => "source", "docs/a.md" => "other"}
  abort "beta classification failed" unless Dora::WorkspaceInventory.classify!(beta, ["notes/a.md"]) == {"notes/a.md" => "notes"}
end
puts "Dora workspace inventory test passed (two project category sets)."
