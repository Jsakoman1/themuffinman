#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

ROOT = File.expand_path("..", __dir__)
FIXTURE = File.join(ROOT, "test/fixtures/self-contained-project-answers.yaml")

def run!(root, *command)
  output, status = Open3.capture2e(*command, chdir: root)
  abort "independent self-contained consumer command failed: #{command.join(' ')}\n#{output}" unless status.success?
  output
end

Dir.mktmpdir("dora-independent-self-contained") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  fixture = YAML.load_file(FIXTURE)
  answers = YAML.load_file(File.join(ROOT, fixture.fetch("answers_template").delete_prefix("dora/")))
  descriptor = fixture.fetch("dora_source")
  descriptor.fetch("source")["path"] = source
  descriptor.fetch("source")["ref"] = "c" * 40
  descriptor.fetch("source")["checksum"] = Dora::BootstrapSource.send(:checksum_for, source)
  answers["dora_source"] = descriptor
  answers_path = File.join(sandbox, "answers.yaml")
  File.write(answers_path, YAML.dump(answers))

  project = File.join(sandbox, "community-library")
  run!(sandbox, File.join(source, "bin/dora"), "new", project, "--answers", answers_path)
  run!(project, "./bin/dora", "help")
  readiness = YAML.safe_load(run!(project, "./bin/dora", "readiness", "."))
  abort "independent consumer did not report its missing baseline" unless readiness.fetch("state") == "missing_git_repository"
  ready = YAML.safe_load(run!(project, "./bin/dora", "readiness", ".", "--initialize-git"))
  abort "independent consumer did not create an explicit baseline" unless ready.fetch("ready")

  provenance = YAML.load_file(File.join(project, ".dora/bootstrap-source.yaml"))
  abort "independent consumer lost source provenance" unless provenance.dig("source", "source", "checksum") == descriptor.dig("source", "checksum")
  abort "independent consumer created product code" unless Dir[File.join(project, "apps/**/*")].empty?
  control_files = [File.join(project, "AGENTS.md")] + %w[bin docs .dora].flat_map { |relative| Dir[File.join(project, relative, "**/*")] }
  content = control_files.select { |path| File.file?(path) }.map { |path| File.binread(path).force_encoding(Encoding::UTF_8).scrub }.join("\n")
  abort "independent self-contained consumer refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent self-contained consumer test passed (local launcher, explicit baseline, provenance, and no product dependency)."
