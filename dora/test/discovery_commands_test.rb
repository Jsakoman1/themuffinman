#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
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

def configure_project(root, id, directory, category)
  Dora::ProjectInitializer.initialize!(root, project_id: id, manifest_path: MANIFEST)
  FileUtils.mkdir_p(File.join(root, directory))
  File.write(File.join(root, directory, "#{id}.txt"), "#{id}-needle\n")
  File.write(File.join(root, ".dora/controls/context-search.yaml"), YAML.dump({"kind" => "dora_context_search", "version" => 1, "roots" => [directory], "exclusions" => []}))
  File.write(File.join(root, ".dora/controls/workspace-inventory.yaml"), YAML.dump({"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => category, "path_prefixes" => ["#{directory}/"]}]}))
end

Dir.mktmpdir("dora-discovery") do |sandbox|
  alpha_root = File.join(sandbox, "alpha")
  beta_root = File.join(sandbox, "beta")
  configure_project(alpha_root, "alpha", "source", "source")
  configure_project(beta_root, "beta", "notes", "notes")
  alpha_adapter = File.join(alpha_root, ".dora/project.yaml")
  beta_adapter = File.join(beta_root, ".dora/project.yaml")

  alpha_search = capture!(CLI, "search", alpha_adapter, "alpha-needle")
  abort "alpha search leaked another project" unless alpha_search.strip == "source/alpha.txt"
  beta_search = capture!(CLI, "search", beta_adapter, "beta-needle")
  abort "beta search leaked another project" unless beta_search.strip == "notes/beta.txt"
  abort "alpha inventory failed" unless YAML.load(capture!(CLI, "inventory", alpha_adapter, "source/alpha.txt")).fetch("source/alpha.txt") == "source"
  abort "beta inventory failed" unless YAML.load(capture!(CLI, "inventory", beta_adapter, "notes/beta.txt")).fetch("notes/beta.txt") == "notes"
end

puts "Dora discovery command test passed (two bounded project configurations)."
