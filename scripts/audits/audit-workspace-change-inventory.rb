#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "shellwords"
require "time"
require "yaml"

ROOT = File.expand_path("../..", __dir__)

def porcelain_changes
  stdout, stderr, status = Open3.capture3("git", "status", "--porcelain=v1", "--untracked-files=all", chdir: ROOT)
  abort stderr unless status.success?

  stdout.lines.each_with_object([]) do |line, changes|
    next if line.length < 4

    raw_path = line[3..].strip
    path = raw_path.start_with?("\"") ? Shellwords.shellsplit(raw_path).first : raw_path
    changes << { "path" => path, "status" => line[0, 2] }
  end
end

def status_group(status)
  return "untracked" if status == "??"
  return "deleted" if status.include?("D")
  return "added" if status.include?("A")
  return "renamed" if status.include?("R")

  "modified"
end

def category(path)
  return "generated_or_build" if path.start_with?("docs/audit-output/", "apps/themuffinman/frontend/dist/", "apps/themuffinman/target/")
  return "runtime_evidence" if path.start_with?("docs/runtime-evidence/")
  return "planning" if path.start_with?("docs/work/")
  return "documentation" if path.start_with?("docs/")
  return "tooling" if path == "Makefile" || path.start_with?("scripts/", ".agents/", ".run/", ".github/", ".codex/")
  return "product_backend" if path.start_with?("apps/themuffinman/src/")
  return "product_frontend" if path.start_with?("apps/themuffinman/frontend/")

  "other"
end

def counts(rows, key)
  rows.group_by { |row| row.fetch(key) }.transform_values(&:length).sort.to_h
end

def snapshot_for(rows, excluded_paths)
  captured_rows = rows.reject { |row| excluded_paths.include?(row.fetch("path")) }.map do |row|
    row.merge("status_group" => status_group(row.fetch("status")), "category" => category(row.fetch("path")))
  end.sort_by { |row| row.fetch("path") }
  {
    "kind" => "workspace_change_snapshot",
    "version" => 1,
    "captured_at" => Time.now.utc.iso8601,
    "source" => "git status --porcelain=v1 --untracked-files=all",
    "immutable" => true,
    "excluded_paths" => excluded_paths.sort,
    "summary" => {
      "changed_paths" => captured_rows.length,
      "status_groups" => counts(captured_rows, "status_group"),
      "categories" => counts(captured_rows, "category")
    },
    "changes" => captured_rows
  }
end

def verify_snapshot(path)
  snapshot = YAML.load_file(path)
  failures = []
  failures << "snapshot kind is invalid" unless snapshot["kind"] == "workspace_change_snapshot"
  failures << "snapshot must be immutable" unless snapshot["immutable"] == true
  changes = Array(snapshot["changes"])
  failures << "snapshot paths are duplicated" unless changes.map { |row| row["path"] }.uniq.length == changes.length
  failures << "snapshot is not sorted by path" unless changes.map { |row| row["path"] } == changes.map { |row| row["path"] }.sort
  summary = snapshot.fetch("summary", {})
  failures << "snapshot changed-path count disagrees" unless summary["changed_paths"] == changes.length
  failures << "snapshot status counts disagree" unless summary["status_groups"] == counts(changes, "status_group")
  failures << "snapshot category counts disagree" unless summary["categories"] == counts(changes, "category")
  changes.each do |row|
    failures << "snapshot row lacks path" if row["path"].to_s.empty?
    failures << "snapshot row has invalid category #{row["category"]}" unless category(row["path"]) == row["category"]
    failures << "snapshot row has invalid status group #{row["status_group"]}" unless status_group(row["status"]) == row["status_group"]
  end
  abort "Workspace change snapshot audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?

  puts "Workspace change snapshot audit passed (#{changes.length} immutable classified paths)."
end

if ARGV == ["--check-fixtures"]
  fixtures = {
    "Makefile" => "tooling",
    "docs/work/example.yaml" => "planning",
    "docs/runtime-evidence/example.json" => "runtime_evidence",
    "apps/themuffinman/frontend/dist/index.html" => "generated_or_build",
    "apps/themuffinman/src/main/java/App.java" => "product_backend",
    "apps/themuffinman/frontend/src/App.vue" => "product_frontend"
  }
  fixtures.each { |path, expected| abort "Fixture #{path} expected #{expected}, got #{category(path)}" unless category(path) == expected }
  abort "Fixture status failed" unless status_group("??") == "untracked" && status_group(" M") == "modified" && status_group(" D") == "deleted"
  puts "Workspace change inventory fixtures passed (#{fixtures.length} categories and porcelain status groups)."
  exit 0
end

if ARGV.first == "--write"
  destination = ARGV[1]
  abort "usage: ruby scripts/audits/audit-workspace-change-inventory.rb --write <snapshot-path>" if destination.to_s.empty? || ARGV.length != 2
  snapshot = snapshot_for(porcelain_changes, [destination])
  File.write(File.join(ROOT, destination), YAML.dump(snapshot).sub(/\A---\n/, ""))
  puts "Workspace change snapshot written (#{snapshot.dig("summary", "changed_paths")} immutable classified paths)."
  exit 0
end

if ARGV.length == 1
  verify_snapshot(File.join(ROOT, ARGV.first))
  exit 0
end

abort "usage: ruby scripts/audits/audit-workspace-change-inventory.rb [--check-fixtures|--write <snapshot-path>|<snapshot-path>]" unless ARGV.empty?
snapshot = snapshot_for(porcelain_changes, [])
puts JSON.pretty_generate(snapshot.slice("captured_at", "summary"))
