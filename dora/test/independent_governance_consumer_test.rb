#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)

def run!(root, *command)
  output, status = Open3.capture2e(*command, chdir: root)
  abort "consumer command failed: #{command.join(" ")}: #{output}" unless status.success?
  output
end

Dir.mktmpdir("dora-governance-consumer") do |sandbox|
  root = File.join(sandbox, "consumer")
  FileUtils.mkdir_p(File.join(root, "docs/work"))
  FileUtils.cp_r(ROOT, File.join(root, "dora"))
  FileUtils.mkdir_p(File.join(root, ".dora"))
  fixture = File.join(ROOT, "fixtures/independent-governance-consumer/.dora/project.yaml")
  FileUtils.cp(fixture, File.join(root, ".dora/project.yaml"))
  File.write(File.join(root, "bin-dora-placeholder"), "")
  FileUtils.mkdir_p(File.join(root, "bin"))
  File.write(File.join(root, "bin/dora"), "#!/bin/sh\nexec \"$(dirname \"$0\")/../dora/bin/dora\" \"$@\"\n")
  FileUtils.chmod("+x", File.join(root, "bin/dora"))
  plan = {"kind" => "work", "version" => 1, "id" => "current", "status" => "draft", "baseline" => "0000000", "tasks" => [{"id" => "one", "title" => "One", "observable_outcome" => "A declared result exists", "dependencies" => [], "evidence_boundary" => ["unit test"], "paths" => ["docs/result.txt"], "required_paths" => ["docs/result.txt"], "validation" => "true"}]}
  File.write(File.join(root, "docs/work/current.yaml"), YAML.dump(plan))
  registry = {"kind" => "dora_plan_registry", "version" => 1, "plans" => [{"id" => "current", "path" => "docs/work/current.yaml", "allowed_statuses" => ["draft"]}]}
  File.write(File.join(root, "plans.yaml"), YAML.dump(registry))
  ownership = {"kind" => "dora_control_ownership", "version" => 1, "allowed_owners" => ["core", "extension"], "rules" => [{"id" => "core", "owner" => "core", "subjects" => ["script:tools/check.rb"]}, {"id" => "extension", "owner" => "extension", "patterns" => ["make:*"]}]}
  File.write(File.join(root, "ownership.yaml"), YAML.dump(ownership))
  run!(root, "./bin/dora", "plan-contract", "docs/work/current.yaml")
  run!(root, "./bin/dora", "plan-coverage", "plans.yaml")
  output = run!(root, "ruby", "-I", "dora/lib", "-e", "require 'dora/control_ownership'; require 'yaml'; puts Dora::ControlOwnership.classify!(YAML.load_file('ownership.yaml'), ['script:tools/check.rb']).first['owner']")
  abort "ownership control did not run" unless output.strip == "core"
  content = Dir[File.join(root, "{.dora,docs,plans.yaml,ownership.yaml}")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "consumer fixture refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent governance consumer test passed (local launcher and declared governance controls)."
