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

Dir.mktmpdir("dora-control-commands") do |sandbox|
  root = File.join(sandbox, "project")
  Dora::ProjectInitializer.initialize!(root, project_id: "project", manifest_path: MANIFEST)
  FileUtils.mkdir_p(File.join(root, "output"))
  File.write(File.join(root, "output/remove.tmp"), "candidate")
  FileUtils.mkdir_p(File.join(root, "templates")); File.write(File.join(root, "templates/readme.md"), "See docs/current.md")
  File.write(File.join(root, "docs/current.md"), "current")
  FileUtils.mkdir_p(File.join(root, "source")); File.write(File.join(root, "source/App.java"), "class App {}")
  File.write(File.join(root, "retention.yaml"), YAML.dump({"kind" => "dora_retention_policy", "version" => 1, "generated_roots" => ["output"], "retained_paths" => [], "cleanup_candidate_globs" => ["output/*.tmp"]}))
  File.write(File.join(root, "freshness.yaml"), YAML.dump({"kind" => "dora_template_freshness", "version" => 1, "templates" => [{"path" => "templates/readme.md", "required_references" => ["docs/current.md"], "retired_references" => []}]}))
  File.write(File.join(root, "map.yaml"), YAML.dump({"kind" => "dora_repository_map", "version" => 1, "source_roots" => [{"id" => "source", "path" => "source", "plugins" => ["java_spring"]}], "relationships" => []}))
  adapter = File.join(root, ".dora/project.yaml")
  abort "retention command failed" unless YAML.load(capture!(CLI, "retention", adapter, "--config", "retention.yaml")).first.fetch("classification") == "cleanup_candidate"
  dry_run = YAML.load(capture!(CLI, "cleanup-dry-run", adapter, "--config", "retention.yaml"))
  abort "cleanup dry-run failed" unless dry_run == ["output/remove.tmp"] && File.file?(File.join(root, "output/remove.tmp"))
  capture!(CLI, "cleanup", adapter, "--config", "retention.yaml", "--approve", "output/remove.tmp")
  abort "confirmed cleanup did not delete its target" if File.exist?(File.join(root, "output/remove.tmp"))
  abort "template command failed" unless YAML.load(capture!(CLI, "templates", adapter, "--config", "freshness.yaml")).first.fetch("fresh")
  abort "repository-map command failed" unless YAML.load(capture!(CLI, "repository-map", adapter, "--config", "map.yaml")).fetch("sources").first.fetch("kind") == "java_source"
end

puts "Dora control command test passed (explicit config and cleanup confirmation)."
