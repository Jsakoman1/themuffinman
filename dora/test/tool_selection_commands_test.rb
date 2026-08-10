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

def configure_project(root, id, target, prefix, validation)
  Dora::ProjectInitializer.initialize!(root, project_id: id, manifest_path: MANIFEST)
  File.write(File.join(root, ".dora/controls/tool-catalog.yaml"), YAML.dump({"kind" => "dora_tool_catalog", "version" => 1, "commands" => [{"id" => "#{id}-tool", "target" => target, "purpose" => "Run #{target}.", "preconditions" => ["ready"], "expected_cost" => "short"}]}))
  File.write(File.join(root, ".dora/controls/change-routing.yaml"), YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => id, "path_prefixes" => [prefix], "commands" => [validation]}]}))
end

Dir.mktmpdir("dora-tool-selection") do |sandbox|
  alpha_root = File.join(sandbox, "alpha")
  beta_root = File.join(sandbox, "beta")
  configure_project(alpha_root, "alpha", "alpha-check", "src/", "alpha-test")
  configure_project(beta_root, "beta", "beta-audit", "guide/", "beta-lint")

  alpha_adapter = File.join(alpha_root, ".dora/project.yaml")
  beta_adapter = File.join(beta_root, ".dora/project.yaml")
  abort "alpha tool catalog leaked beta" unless capture!(CLI, "tools", alpha_adapter).include?("alpha-check")
  abort "beta tool catalog leaked alpha" unless capture!(CLI, "tools", beta_adapter).include?("beta-audit")
  abort "alpha route failed" unless YAML.load(capture!(CLI, "route", alpha_adapter, "src/app.rb")).fetch("commands") == ["alpha-test"]
  abort "beta route failed" unless YAML.load(capture!(CLI, "route", beta_adapter, "guide/readme.md")).fetch("commands") == ["beta-lint"]

  catalog_root = File.join(sandbox, "catalog")
  Dora::ProjectInitializer.initialize!(catalog_root, project_id: "catalog", manifest_path: MANIFEST)
  File.write(File.join(catalog_root, ".dora/controls/tool-catalog.yaml"), YAML.dump({"kind" => "dora_tool_catalog", "version" => 1, "commands" => [{"id" => "source-tool", "target" => "source-test", "purpose" => "Run source test.", "preconditions" => ["ready"], "expected_cost" => "short"}]}))
  File.write(File.join(catalog_root, ".dora/controls/change-routing.yaml"), YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "tool_ids" => ["source-tool"]}]}))
  catalog_adapter = File.join(catalog_root, ".dora/project.yaml")
  catalog_route = YAML.load(capture!(CLI, "route", catalog_adapter, "src/app.rb"))
  abort "catalog route did not resolve the declared tool" unless catalog_route.fetch("tool_ids") == ["source-tool"] && catalog_route.fetch("commands") == ["source-test"] && catalog_route.fetch("read_only")
end

puts "Dora tool-selection command test passed (two isolated projects)."
