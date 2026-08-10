#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/task_change_set"

def run!(*command, chdir:)
  _output, error, status = Open3.capture3(*command, chdir: chdir)
  abort "command failed: #{command.join(" ")}: #{error}" unless status.success?
end

Dir.mktmpdir("dora-task-change-set") do |root|
  Dir.mkdir(File.join(root, "docs"))
  Dir.mkdir(File.join(root, "docs/work"))
  File.write(File.join(root, "tracked.txt"), "baseline\n")
  run!("git", "init", "-q", chdir: root)
  run!("git", "config", "user.email", "dora@example.test", chdir: root)
  run!("git", "config", "user.name", "Dora Test", chdir: root)
  run!("git", "add", ".", chdir: root)
  run!("git", "commit", "-qm", "baseline", chdir: root)
  baseline, = Open3.capture2("git", "rev-parse", "--short", "HEAD", chdir: root)
  File.write(File.join(root, "preexisting.txt"), "before start\n")
  start_paths = Dora::TaskChangeSet.workspace_paths!(project_root: root, baseline: baseline.strip)
  plan = {"kind" => "work", "version" => 1, "id" => "change-set", "title" => "Change set", "status" => "active", "baseline" => baseline.strip, "tasks" => [{"id" => "task", "started_at" => "2026-08-10T12:00:00Z", "start_workspace_paths" => start_paths}]}
  File.write(File.join(root, "docs/work/change-set.yaml"), YAML.dump(plan))
  File.write(File.join(root, "new.txt"), "after start\n")

  result = Dora::TaskChangeSet.build!(project_root: root, work_plan: "docs/work/change-set.yaml", task_id: "task")
  abort "change set is not explicitly advisory" unless result.fetch("read_only") && result.fetch("disposition") == "advisory" && !result.fetch("observed_at").empty?
  abort "change set misattributed preexisting work" unless result.fetch("preexisting_dirty_paths") == ["preexisting.txt"] && result.fetch("changed_since_start") == ["new.txt"] && result.fetch("excluded_administrative_paths") == ["docs/work/change-set.yaml"]
  abort "change set omitted its attribution boundary" unless result.fetch("attribution_boundary").include?("not attributed")
end

puts "Dora task change set test passed (bounded post-start candidates and explicit ambiguity)."
