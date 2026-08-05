#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"
require "open3"
require "time"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/task-lease-consumers.yaml"))

def run(dora, root, *command)
  Open3.capture2e(File.join(dora, "bin/dora"), *command, chdir: root)
end

Dir.mktmpdir("dora-independent-task-lease") do |sandbox|
  installed_dora = File.join(sandbox, "dora")
  FileUtils.cp_r(ROOT, installed_dora)
  expires_at = (Time.now.utc + 3600).iso8601

  FIXTURE.fetch("consumers").each do |consumer|
    project = File.join(sandbox, consumer.fetch("id"))
    FileUtils.mkdir_p(project)
    registry = ".dora/task-leases.yaml"
    output, status = run(installed_dora, project, "lease-acquire", registry, consumer.fetch("work_plan"), consumer.fetch("task"), consumer.fetch("initial_holder"), "--expires-at", expires_at, "--format", "json")
    acquired = JSON.parse(output)
    lease = acquired.dig("payload", "leases", 0)
    abort "independent consumer could not acquire its lease" unless status.success? && lease.fetch("holder") == consumer.fetch("initial_holder")

    output, status = run(installed_dora, project, "lease-acquire", registry, consumer.fetch("work_plan"), consumer.fetch("task"), consumer.fetch("handoff_holder"), "--expires-at", expires_at)
    abort "independent consumer accepted conflicting claim" if status.success?
    abort "independent consumer lost conflict boundary" unless output.include?("conflict")

    output, status = run(installed_dora, project, "lease-handoff", registry, lease.fetch("id"), consumer.fetch("handoff_holder"), "--reason", "Focused task handed over.", "--format", "json")
    handoff = JSON.parse(output).dig("payload", "leases", 0)
    abort "independent consumer lost handoff record" unless status.success? && handoff.fetch("holder") == consumer.fetch("handoff_holder") && handoff.dig("handoff", "from") == consumer.fetch("initial_holder")
    content = File.read(File.join(project, registry))
    abort "independent lease record refers to MuffinMan" if content.downcase.include?("muffinman")
  end
end

puts "Dora independent task lease consumer test passed (two isolated projects reject conflicts and retain explicit handoffs)."
