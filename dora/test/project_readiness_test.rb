#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_readiness"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-project-readiness") do |sandbox|
  project = File.join(sandbox, "project")
  Dir.mkdir(project)
  File.write(File.join(project, "README.md"), "# Project\n")

  report = Dora::ProjectReadiness.report!(project_root: project)
  abort "readiness accepted a project without Git" if report.fetch("ready") || report.fetch("state") != "missing_git_repository"
  abort "readiness mutated Git without an explicit request" if Dir.exist?(File.join(project, ".git"))

  output, status = Open3.capture2e(CLI, "readiness", project, chdir: ROOT)
  abort "readiness command failed: #{output}" unless status.success?
  abort "readiness command did not report a missing Git repository" unless YAML.safe_load(output).fetch("state") == "missing_git_repository"

  initialized = Dora::ProjectReadiness.report!(project_root: project, initialize_git: true)
  abort "explicit readiness initialization did not create a usable baseline" unless initialized.fetch("ready") && initialized.fetch("baseline").match?(/\A[0-9a-f]{40}\z/)
  abort "explicit readiness initialization did not create Git metadata" unless Dir.exist?(File.join(project, ".git"))

  ready = Dora::ProjectReadiness.report!(project_root: project)
  abort "readiness did not preserve the created baseline" unless ready.fetch("ready") && ready.fetch("baseline") == initialized.fetch("baseline")
end

puts "Dora project readiness test passed (read-only diagnosis and explicit local Git initialization)."
