#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "time"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

def run(root, *command)
  Open3.capture2e(CLI, *command, chdir: root)
end

Dir.mktmpdir("dora-task-lease-command") do |root|
  registry = ".dora/task-leases.yaml"
  expires_at = (Time.now.utc + 3600).iso8601
  output, status = run(root, "lease-acquire", registry, "docs/work/note.yaml", "write-note", "alpha", "--expires-at", expires_at, "--format", "json")
  acquired = JSON.parse(output)
  lease = acquired.dig("payload", "leases", 0)
  abort "lease acquire did not record a local mutation" unless status.success? && acquired.fetch("side_effect") == "local_mutation" && lease.fetch("holder") == "alpha"

  output, status = run(root, "lease-inspect", registry, "--format", "json")
  inspected = JSON.parse(output)
  abort "lease inspect did not remain read-only" unless status.success? && inspected.fetch("side_effect") == "read_only" && inspected.dig("payload", "leases", 0, "id") == lease.fetch("id")

  output, status = run(root, "lease-acquire", registry, "docs/work/note.yaml", "write-note", "beta", "--expires-at", expires_at)
  abort "conflicting lease acquire unexpectedly passed" if status.success?
  abort "conflicting lease rejection is missing" unless output.include?("conflict")

  output, status = run(root, "lease-handoff", registry, lease.fetch("id"), "beta", "--reason", "Alpha paused the task.", "--format", "json")
  handed_off = JSON.parse(output)
  abort "lease handoff lost provenance" unless status.success? && handed_off.fetch("side_effect") == "local_mutation" && handed_off.dig("payload", "leases", 0, "handoff", "from") == "alpha" && handed_off.dig("payload", "leases", 0, "holder") == "beta"
end

puts "Dora task lease command test passed (inspect, conflict-safe acquire, and explicit handoff records)."
