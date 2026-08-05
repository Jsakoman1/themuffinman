#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/starter_pack"

ROOT = File.expand_path("..", __dir__)
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/postgres-starter-consumer.yaml"))
abort "Postgres consumer fixture is invalid" unless fixture["kind"] == "dora_postgres_starter_consumer" && fixture["version"] == 1

Dir.mktmpdir("dora-independent-postgres-consumer") do |sandbox|
  project = File.join(sandbox, fixture.fetch("id"))
  FileUtils.mkdir_p(File.join(project, ".dora"))
  result = Dora::StarterPack.apply!(File.join(ROOT, "starters/#{fixture.fetch("starter")}.yaml"), project_root: project)
  abort "Postgres starter selection changed" unless result.fetch("id") == fixture.fetch("starter")

  fixture.fetch("expected_files").each do |relative|
    abort "independent consumer omits #{relative}" unless File.file?(File.join(project, relative))
  end
  commands = YAML.load_file(File.join(project, ".dora/project-commands.yaml")).fetch("commands")
  fixture.fetch("expected_commands").each do |id|
    abort "independent consumer omits #{id} command" unless commands[id].is_a?(String) && !commands[id].empty?
  end
  abort "database command does not declare Compose" unless commands.fetch("database").include?("docker compose")
  abort "health command does not declare the health endpoint" unless commands.fetch("health").include?("actuator/health")

  content = Dir[File.join(project, "**", "*")].select { |path| File.file?(path) }.map { |path| File.binread(path).downcase }.join("\n")
  leaked = fixture.fetch("forbidden_terms").find { |term| content.include?(term) }
  abort "independent consumer leaked product term: #{leaked}" if leaked
end

puts "Dora independent Postgres starter consumer test passed (fresh technical consumer, declared commands, and no product leakage)."
