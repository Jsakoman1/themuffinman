#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
MANIFEST = File.join(ROOT, "templates/init-manifest.yaml")

def capture!(*command)
  output, status = Open3.capture2e(*command, chdir: ROOT)
  abort "command failed: #{command.join(" ")}: #{output}" unless status.success?
  output
end

def configure_project(root, id, source, related)
  Dora::ProjectInitializer.initialize!(root, project_id: id, manifest_path: MANIFEST)
  evidence = File.join(root, "docs/#{id}.md")
  File.write(evidence, "#{id} invariant\n")
  File.write(File.join(root, ".dora/controls/documentation-evidence.yaml"), YAML.dump({"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => id, "match" => "#{id} invariant", "evidence" => ["docs/#{id}.md"]}]}))
  File.write(File.join(root, ".dora/controls/system-map.yaml"), YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => source}, {"id" => related}], "edges" => [{"from" => source, "to" => related}]}))
end

Dir.mktmpdir("dora-analysis") do |sandbox|
  alpha_root = File.join(sandbox, "alpha")
  beta_root = File.join(sandbox, "beta")
  configure_project(alpha_root, "alpha", "alpha-code", "alpha-docs")
  configure_project(beta_root, "beta", "beta-api", "beta-tests")
  alpha_adapter = File.join(alpha_root, ".dora/project.yaml")
  beta_adapter = File.join(beta_root, ".dora/project.yaml")

  abort "alpha evidence failed" unless YAML.load(capture!(CLI, "evidence", alpha_adapter)).first.fetch("matched")
  abort "beta evidence failed" unless YAML.load(capture!(CLI, "evidence", beta_adapter)).first.fetch("matched")
  abort "alpha impact failed" unless YAML.load(capture!(CLI, "impact", alpha_adapter, "alpha-code")).fetch("related") == ["alpha-docs"]
  abort "beta impact failed" unless YAML.load(capture!(CLI, "impact", beta_adapter, "beta-api")).fetch("related") == ["beta-tests"]
end

puts "Dora analysis command test passed (two isolated projects)."
