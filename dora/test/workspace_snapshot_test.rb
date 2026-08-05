#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/workspace_snapshot"

def project(root, name, source_root, category)
  project_root = File.join(root, name)
  FileUtils.mkdir_p(File.join(project_root, source_root))
  system("git", "init", "-q", project_root) || abort("cannot initialize fixture Git repository")
  File.write(File.join(project_root, source_root, "#{name}.txt"), name)
  File.write(File.join(project_root, "ignored.txt"), "ignored")
  config = File.join(project_root, "snapshot.yaml")
  File.write(config, YAML.dump({"kind" => "dora_workspace_snapshot_config", "version" => 1, "roots" => [source_root], "categories" => [{"id" => category, "path_prefixes" => ["#{source_root}/"]}]}))
  [project_root, config]
end

Dir.mktmpdir("dora-workspace-snapshot") do |sandbox|
  alpha_root, alpha_config = project(sandbox, "alpha", "src", "source")
  beta_root, beta_config = project(sandbox, "beta", "notes", "notes")
  alpha = Dora::WorkspaceSnapshot.write!(alpha_config, "snapshot-output.yaml", project_root: alpha_root)
  beta = Dora::WorkspaceSnapshot.write!(beta_config, "snapshot-output.yaml", project_root: beta_root)
  abort "alpha snapshot leaked paths" unless alpha.fetch("changes").map { |row| row.fetch("path") } == ["src/alpha.txt"]
  abort "beta snapshot leaked paths" unless beta.fetch("changes").map { |row| row.fetch("path") } == ["notes/beta.txt"]
  abort "alpha category failed" unless alpha.fetch("changes").first.fetch("category") == "source"
  Dora::WorkspaceSnapshot.verify!(File.join(alpha_root, "snapshot-output.yaml"))
end

puts "Dora workspace snapshot test passed (two bounded project snapshots)."
