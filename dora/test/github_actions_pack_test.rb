#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-github-actions-pack") do |sandbox|
  root = File.join(sandbox, "ci-project")
  commands = File.join(sandbox, "project-commands.yaml")
  File.write(commands, YAML.dump({"kind" => "dora_project_commands", "version" => 1, "commands" => {"setup" => "bundle install", "test" => "bundle exec rake test", "build" => "bundle exec rake build"}}))
  output, status = Open3.capture2e(CLI, "init", root, "--project", "ci-project", "--ci", "github-actions", "--commands", commands, chdir: ROOT)
  abort "CI init failed: #{output}" unless status.success?
  workflow = File.read(File.join(root, ".github/workflows/dora-control.yml"))
  ["./bin/dora doctor", "./bin/dora validate-adapter", "bundle install", "bundle exec rake test", "bundle exec rake build"].each { |command| abort "workflow missing #{command}" unless workflow.include?(command) }
  abort "workflow makes a release claim" if workflow.downcase.include?("release")
  abort "workflow makes a runtime claim" if workflow.downcase.include?("runtime")
end

puts "Dora GitHub Actions pack test passed (project-configured control workflow)."
