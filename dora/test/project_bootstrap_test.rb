#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-project-bootstrap") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  descriptor = File.join(sandbox, "bootstrap-source.yaml")
  File.write(descriptor, YAML.dump({
    "kind" => "dora_bootstrap_source",
    "version" => 1,
    "source" => {"path" => source, "ref" => "b" * 40}
  }))
  destination = File.join(sandbox, "new-project")

  output, status = Open3.capture2e(CLI, "bootstrap", destination, "--project", "new-project", "--source", descriptor, chdir: ROOT)
  abort "bootstrap command failed: #{output}" unless status.success?
  abort "bootstrap did not copy Dora" unless File.file?(File.join(destination, "dora", "bin", "dora"))
  abort "bootstrap did not create a local launcher" unless File.executable?(File.join(destination, "bin", "dora"))

  source_record = YAML.load_file(File.join(destination, ".dora/bootstrap-source.yaml"))
  abort "bootstrap did not record the reviewed source" unless source_record.dig("source", "ref") == "b" * 40 && source_record["package_path"] == "dora"

  output, status = Open3.capture2e(File.join(destination, "bin", "dora"), "doctor", ".dora/project.yaml", chdir: destination)
  abort "project-local launcher is unhealthy: #{output}" unless status.success? && output.include?("PASSED")

  second_output, second_status = Open3.capture2e(CLI, "bootstrap", destination, "--project", "new-project", "--source", descriptor, chdir: ROOT)
  abort "bootstrap overwrote a non-empty project" if second_status.success? || !second_output.include?("destination must be empty")
end

puts "Dora project bootstrap test passed (explicit local source and launcher)."
