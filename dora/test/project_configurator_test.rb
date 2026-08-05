#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
MANIFEST = File.join(ROOT, "templates/init-manifest.yaml")

def configure!(project_root, source_path)
  adapter = File.join(project_root, ".dora/project.yaml")
  output, status = Open3.capture2e(CLI, "configure", adapter, "--control", "change_routing", "--from", source_path, chdir: ROOT)
  abort "configure failed: #{output}" unless status.success?
end

Dir.mktmpdir("dora-project-configurator") do |sandbox|
  alpha_root = File.join(sandbox, "alpha")
  beta_root = File.join(sandbox, "beta")
  Dora::ProjectInitializer.initialize!(alpha_root, project_id: "alpha", manifest_path: MANIFEST)
  Dora::ProjectInitializer.initialize!(beta_root, project_id: "beta", manifest_path: MANIFEST)
  alpha_source = File.join(sandbox, "alpha-routing.yaml")
  beta_source = File.join(sandbox, "beta-routing.yaml")
  File.write(alpha_source, YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "alpha", "path_prefixes" => ["alpha/"], "commands" => ["alpha-test"]}]}))
  File.write(beta_source, YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "beta", "path_prefixes" => ["beta/"], "commands" => ["beta-test"]}]}))
  configure!(alpha_root, alpha_source)
  configure!(beta_root, beta_source)
  alpha = YAML.load_file(File.join(alpha_root, ".dora/controls/change-routing.yaml"))
  beta = YAML.load_file(File.join(beta_root, ".dora/controls/change-routing.yaml"))
  abort "alpha configuration leaked" unless alpha.fetch("rules").first.fetch("id") == "alpha"
  abort "beta configuration leaked" unless beta.fetch("rules").first.fetch("id") == "beta"
end

puts "Dora project configurator test passed (two selected project-owned control writes)."
