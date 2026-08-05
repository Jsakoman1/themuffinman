#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-github-actions-pack") do |sandbox|
  root = File.join(sandbox, "ci-project")
  output, status = Open3.capture2e(CLI, "init", root, "--project", "ci-project", "--ci", "github-actions", chdir: ROOT)
  abort "CI init failed: #{output}" unless status.success?
  workflow = File.read(File.join(root, ".github/workflows/dora-control.yml"))
  %w[dora\ doctor dora\ validate-adapter make\ test make\ build].each { |command| abort "workflow missing #{command}" unless workflow.include?(command.tr("\\", "")) }
  abort "workflow makes a release claim" if workflow.downcase.include?("release")
  abort "workflow makes a runtime claim" if workflow.downcase.include?("runtime")
end

puts "Dora GitHub Actions pack test passed (project-configured control workflow)."
