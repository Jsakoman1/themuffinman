#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
FIXTURE_ROOT = File.join(ROOT, "fixtures/initialized-project")
CLI = File.join(ROOT, "bin/dora")

def run!(*command)
  _output, status = Open3.capture2e(*command, chdir: ROOT)
  abort "command failed: #{command.join(" ")}" unless status.success?
end

def fixture_yaml(relative)
  YAML.load_file(File.join(FIXTURE_ROOT, relative))
end

Dir.mktmpdir("dora-initialized-project") do |sandbox|
  project_root = File.join(sandbox, "fixture-project")
  run!(CLI, "init", project_root, "--project", "fixture-project")

  %w[project.yaml project-control.yaml].each do |name|
    actual = YAML.load_file(File.join(project_root, ".dora", name))
    expected = fixture_yaml(File.join(".dora", name))
    abort "generated #{name} diverges from standalone fixture" unless actual == expected
  end

  run!(CLI, "validate-adapter", File.join(project_root, ".dora/project.yaml"))
  fixture_text = Dir[File.join(FIXTURE_ROOT, "**", "*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "fixture refers to MuffinMan" if fixture_text.downcase.include?("muffinman")
  abort "init created product source" unless Dir[File.join(project_root, "apps/**/*")].empty?
end

puts "Dora standalone initialized-project fixture test passed."
