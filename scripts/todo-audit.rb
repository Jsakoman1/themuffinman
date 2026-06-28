#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

REPO_ROOT = File.expand_path("..", __dir__)
BACKLOG_FILES = [
  "docs/implementation-backlog.md",
  "docs/agent-improvement-backlog.md"
].freeze
TODO_PATTERN = /\b(?:TODO|FIXME)\(([A-Z0-9_-]+)\):/
TODO_CANDIDATE_PATTERN = /\b(?:TODO|FIXME)(?:\([A-Z0-9_<>-]+\))?:/
SKIP_DIRS = %w[
  .git
  node_modules
].freeze

def parse_backlog_entries(path)
  entries = []
  lines = File.readlines(path, chomp: true)
  lines.each_with_index do |line, index|
    next unless line.start_with?("- [")

    if line.start_with?("- [x]")
      raise "#{path}:#{index + 1} resolved backlog items must be removed instead of kept as closed checkboxes"
    end

    next unless line.start_with?("- [ ]")

    match = line.match(/^- \[ \] ([A-Z0-9_-]+): (.+)$/)
    raise "#{path}:#{index + 1} backlog entries must use '- [ ] BACKLOG_ID: description'" unless match

    entries << {
      id: match[1],
      description: match[2],
      path: path,
      line: index + 1
    }
  end
  entries
end

def scan_inline_todos
  references = []
  violations = []

  Dir.glob("#{REPO_ROOT}/**/*", File::FNM_DOTMATCH).sort.each do |absolute_path|
    next unless File.file?(absolute_path)

    relative_path = absolute_path.delete_prefix("#{REPO_ROOT}/")
    next if relative_path.empty?
    next if SKIP_DIRS.any? { |segment| relative_path.split("/").include?(segment) }

    lines = begin
      File.binread(absolute_path)
          .encode("UTF-8", invalid: :replace, undef: :replace, replace: "")
          .lines(chomp: true)
    rescue ArgumentError, Encoding::UndefinedConversionError, Encoding::InvalidByteSequenceError
      next
    end

    lines.each_with_index do |line, index|
      next unless line.match?(TODO_CANDIDATE_PATTERN)
      next if line.include?("TODO(<ID>):") || line.include?("FIXME(<ID>):")

      match = line.match(TODO_PATTERN)
      if match
        references << {
          id: match[1],
          path: relative_path,
          line: index + 1,
          text: line.strip
        }
      else
        violations << "#{relative_path}:#{index + 1} inline TODO/FIXME must use TODO(<ID>): or FIXME(<ID>):"
      end
    end
  end

  [references, violations]
end

def scan_open_plan_tasks
  plan_items = []
  Dir.glob(File.join(REPO_ROOT, ".agents", "*-plan.md")).sort.each do |absolute_path|
    next if absolute_path.include?("/templates/")

    relative_path = absolute_path.delete_prefix("#{REPO_ROOT}/")
    File.readlines(absolute_path, chomp: true).each_with_index do |line, index|
      next unless line.start_with?("- [ ]")

      plan_items << {
        path: relative_path,
        line: index + 1,
        text: line.sub("- [ ] ", "")
      }
    end
  end
  plan_items
end

manifest_path = ARGV.each_cons(2).find { |flag, _| flag == "--manifest" }&.last
manifest = manifest_path ? YAML.load_file(File.join(REPO_ROOT, manifest_path)) : nil

backlog_entries = BACKLOG_FILES.flat_map do |relative_path|
  parse_backlog_entries(File.join(REPO_ROOT, relative_path))
end

open_backlog_ids = {}
backlog_entries.each do |entry|
  if open_backlog_ids.key?(entry[:id])
    existing = open_backlog_ids[entry[:id]]
    raise "Duplicate backlog ID #{entry[:id]} in #{existing[:path]}:#{existing[:line]} and #{entry[:path]}:#{entry[:line]}"
  end
  open_backlog_ids[entry[:id]] = entry
end

todo_references, violations = scan_inline_todos
todo_references.each do |reference|
  unless open_backlog_ids.key?(reference[:id])
    violations << "#{reference[:path]}:#{reference[:line]} references unknown or resolved backlog ID #{reference[:id]}"
  end
end

plan_items = scan_open_plan_tasks

if manifest
  backlog = manifest.fetch("backlog", {})
  created_ids = Array(backlog["createdIds"])
  resolved_ids = Array(backlog["resolvedIds"])

  if manifest["status"] == "complete" && backlog["reviewed"] != true
    violations << "#{manifest_path} must set backlog.reviewed=true before completion"
  end

  created_ids.each do |id|
    unless open_backlog_ids.key?(id)
      violations << "#{manifest_path} declares backlog.createdIds entry #{id} but that ID is not open in a persistent backlog file"
    end
  end

  resolved_ids.each do |id|
    if open_backlog_ids.key?(id)
      violations << "#{manifest_path} declares backlog.resolvedIds entry #{id} but that ID is still open in #{open_backlog_ids[id][:path]}:#{open_backlog_ids[id][:line]}"
    end

    lingering_references = todo_references.select { |reference| reference[:id] == id }
    lingering_references.each do |reference|
      violations << "#{manifest_path} resolved backlog ID #{id} still appears in #{reference[:path]}:#{reference[:line]}"
    end
  end

  plan_file = manifest["planFile"]
  if manifest["status"] == "complete" && plan_file
    open_plan_items = plan_items.select { |item| item[:path] == plan_file }
    open_plan_items.each do |item|
      violations << "#{manifest_path} is complete but plan still has open task at #{item[:path]}:#{item[:line]}"
    end
  end
end

puts "Persistent backlog audit"
puts "  Open backlog items: #{backlog_entries.size}"
backlog_entries.each do |entry|
  puts "    - #{entry[:id]} (#{entry[:path]}:#{entry[:line]}) #{entry[:description]}"
end

puts "  Inline TODO/FIXME references: #{todo_references.size}"
todo_references.each do |reference|
  puts "    - #{reference[:id]} (#{reference[:path]}:#{reference[:line]})"
end

puts "  Open plan tasks: #{plan_items.size}"
plan_items.each do |item|
  puts "    - #{item[:path]}:#{item[:line]} #{item[:text]}"
end

if violations.empty?
  puts "Todo audit passed"
  exit 0
end

warn "Todo audit failed:"
violations.each do |violation|
  warn "  - #{violation}"
end
exit 1
