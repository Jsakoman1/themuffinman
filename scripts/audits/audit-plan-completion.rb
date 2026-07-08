#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "yaml"
require "time"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT
OUT_ROOT = "docs/generated/local-tooling/plan-completion"

def options(argv)
  argv.each_with_object({}) do |arg, parsed|
    key, value = arg.split("=", 2)
    parsed[key] = value if value
  end
end

def abs(path)
  File.join(REPO_ROOT, path.to_s)
end

def read(path)
  File.read(abs(path))
end

def slug(path)
  File.basename(path.to_s, File.extname(path.to_s)).gsub(/[^A-Za-z0-9_-]+/, "-")
end

def completion_section(content)
  match = content.match(/^## Completion Evidence\s*$([\s\S]*?)(?=^## |\z)/)
  match && match[1]
end

def status_section(content)
  match = content.match(/^## Status\s*$([\s\S]*?)(?=^## |\z)/)
  match && match[1]
end

def top_level_status(section)
  return nil unless section

  section.lines.map(&:strip).reject(&:empty?).first
end

def completion_status(section)
  return nil unless section

  section[/^\s*-\s*Status:\s*(.+?)\s*$/i, 1]&.strip
end

def weak_status?(status)
  status.nil? || status.empty? || status.match?(/\A(TBD|draft|not started|unknown)\z/i)
end

def open_task_lines(content)
  content.lines.each_with_index.map do |line, index|
    next unless line.start_with?("- [ ]")

    {line: index + 1, text: line.strip}
  end.compact
end

def deferred_task?(text)
  text.match?(/\bdeferred\b/i) && text.match?(/\b[A-Z][A-Z0-9]+-[A-Z0-9-]+\b/)
end

def child_plan_rows(content)
  content.lines.each_with_index.map do |line, index|
    next unless line.match?(/^\s*-\s*\[[ xX]\]/)
    path = line[/`(\.agents\/[^`]+\.md)`/, 1]
    next unless path

    {
      line: index + 1,
      checked: line.match?(/^\s*-\s*\[[xX]\]/),
      path: path,
      text: line.strip
    }
  end.compact
end

def section_line_range(content, heading)
  lines = content.lines
  start_index = lines.find_index { |line| line.strip == heading }
  return nil unless start_index

  finish_offset = lines[(start_index + 1)..]&.find_index { |line| line.match?(/^##\s+/) }
  finish_index = finish_offset ? start_index + 1 + finish_offset : lines.length
  (start_index...finish_index)
end

def final_status?(status)
  normalized = status.to_s.strip.downcase.gsub(/[.\s]+\z/, "")
  normalized.match?(/\A(?:complete|completed|done|deferred|skipped|n\/a|not applicable)\z/i)
end

def child_plan_section_status_issues(content)
  range = section_line_range(content, "## Child Plans")
  return [] unless range

  content.lines.each_with_index.each_with_object([]) do |(line, index), issues|
    next unless range.cover?(index)

    status = line[/\bStatus:\s*(.+?)\s*$/i, 1]
    next unless status
    next if final_status?(status)

    issues << "child plan status at line #{index + 1} is not final: #{line.strip}"
  end
end

def child_completion_status_issues(content)
  range = section_line_range(content, "## Completion Evidence")
  return [] unless range

  content.lines.each_with_index.each_with_object([]) do |(line, index), issues|
    next unless range.cover?(index)

    status = line[/^\s*-\s*Child plan status:\s*(.+?)\s*$/i, 1]
    next unless status
    next if final_status?(status)

    issues << "child plan completion status at line #{index + 1} is not final: #{line.strip}"
  end
end

def temp_work_product_rows(plan_path)
  Dir.glob(abs(".agents/tmp/*.{yaml,yml,json}")).sort.map do |path|
    payload =
      case File.extname(path)
      when ".json"
        JSON.parse(File.read(path))
      else
        YAML.safe_load(File.read(path), permitted_classes: [Date, Time], aliases: true)
      end
    next unless payload.is_a?(Hash)

    owner_plan = payload["ownerPlan"] || payload[:ownerPlan]
    next unless owner_plan.to_s.strip == plan_path.to_s.strip

    {
      path: path.delete_prefix("#{REPO_ROOT}/"),
      id: payload["id"] || payload[:id],
      owner_plan: owner_plan,
      purpose: payload["purpose"] || payload[:purpose],
      status: payload["status"] || payload[:status],
      delete_when: payload["deleteWhen"] || payload[:deleteWhen]
    }
  rescue StandardError
    nil
  end.compact
end

def manifest_rows
  Dir.glob(abs(".agents/feature-manifests/*.yaml")).sort.map do |path|
    manifest = YAML.load_file(path)
    next unless manifest.is_a?(Hash)

    {
      path: path.delete_prefix("#{REPO_ROOT}/"),
      status: manifest["status"].to_s,
      plan_file: manifest["planFile"].to_s
    }
  rescue Psych::SyntaxError
    nil
  end.compact
end

def analyze_plan(plan_path, nested: false)
  issues = []
  warnings = []
  unless File.exist?(abs(plan_path))
    return {
      plan: plan_path,
      exists: false,
      completion_evidence: false,
      completion_status: nil,
      open_tasks: [],
      child_plans: [],
      issues: ["plan does not exist: #{plan_path}"],
      warnings: []
    }
  end

  content = read(plan_path)
  machine_status = LocalToolingCommon.markdown_frontmatter_value(content, "machine_status").to_s.strip
  status_section_value = top_level_status(status_section(content))
  section = completion_section(content)
  status = completion_status(section)
  open_tasks = open_task_lines(content)
  child_rows = child_plan_rows(content)
  child_status_issues = child_plan_section_status_issues(content)
  child_completion_issues = child_completion_status_issues(content)
  temp_rows = temp_work_product_rows(plan_path)
  finalized = machine_status.match?(/\A(?:complete|completed|done)\z/i) || status.to_s.match?(/\A(?:complete|completed|done)\z/i)

  issues << "missing machine status frontmatter" if machine_status.empty?
  issues << "missing ## Status section" if status_section_value.to_s.strip.empty?
  issues << "missing ## Completion Evidence section" unless section
  issues << "completion evidence status is missing or not final" if weak_status?(status)
  issues << "plan is not marked complete yet" unless finalized

  if finalized
    issues << "machine_status is not final: #{machine_status}" unless final_status?(machine_status)
    issues << "top-level status is not final: #{status_section_value}" unless final_status?(status_section_value)
  end

  unresolved_tasks = open_tasks.reject { |task| deferred_task?(task[:text]) }
  unresolved_tasks.each do |task|
    message = "open task at #{plan_path}:#{task[:line]} is not explicitly deferred to a backlog ID"
    finalized ? issues << message : warnings << message
  end

  if finalized
    issues.concat(child_status_issues)
    issues.concat(child_completion_issues)
  else
    warnings.concat(child_status_issues)
    warnings.concat(child_completion_issues)
  end

  children = child_rows.map do |row|
    child = analyze_plan(row[:path], nested: true)
    child_status =
      if row[:checked] && child[:issues].empty?
        "complete"
      elsif !row[:checked] && deferred_task?(row[:text])
        "deferred"
      else
        "incomplete"
      end
    if child_status == "incomplete" && !nested
      message = "child plan #{row[:path]} at #{plan_path}:#{row[:line]} is incomplete without an explicit deferred backlog ID"
      finalized ? issues << message : warnings << message
    end
    row.merge(status: child_status, child_issues: child[:issues])
  end

  if finalized && temp_rows.any?
    temp_rows.each do |row|
      issues << "temporary work product #{row[:path]} owned by #{plan_path} must be deleted, promoted, or archived before closeout"
    end
  end

  {
    plan: plan_path,
    exists: true,
    machine_status: machine_status,
    top_level_status: status_section_value,
    completion_evidence: !!section,
    completion_status: status,
    open_tasks: open_tasks,
    unresolved_open_tasks: unresolved_tasks,
    child_plans: children,
    temp_work_products: temp_rows,
    issues: issues,
    warnings: warnings
  }
end

def write_report(plan_path, manifest_path, result)
  id = slug(plan_path)
  payload = result.merge(
    generated_at: Time.now.utc.iso8601,
    manifest: manifest_path
  )
  LocalToolingCommon.write_json("#{OUT_ROOT}/#{id}.json", payload)
  lines = ["# Plan Completion #{id}", ""]
  lines << "- Plan: `#{plan_path}`"
  lines << "- Manifest: `#{manifest_path || 'none'}`"
  lines << "- Status: `#{payload[:status]}`"
  lines << "- Machine status: `#{payload[:machine_status]}`"
  lines << "- Top-level status: `#{payload[:top_level_status]}`"
  lines << "- Completion evidence: `#{payload[:completion_evidence]}`"
  lines << "- Open tasks: `#{payload[:open_tasks].size}`"
  lines << "- Temp work products: `#{payload[:temp_work_products].size}`"
  lines << "- Issues: `#{payload[:issues].size}`"
  lines << "- Warnings: `#{payload[:warnings].size}`"
  lines << ""
  if payload[:issues].any?
    lines << "## Issues"
    lines << ""
    payload[:issues].each { |issue| lines << "- #{issue}" }
    lines << ""
  end
  if payload[:warnings].any?
    lines << "## Warnings"
    lines << ""
    payload[:warnings].each { |warning| lines << "- #{warning}" }
    lines << ""
  end
  if payload[:temp_work_products].any?
    lines << "## Temp Work Products"
    lines << ""
    payload[:temp_work_products].each do |row|
      lines << "- `#{row[:path]}` | `#{row[:status]}` | `#{row[:delete_when]}`"
    end
    lines << ""
  end
  LocalToolingCommon.write_text("#{OUT_ROOT}/#{id}-summary.md", lines.join("\n"))
end

parsed = options(ARGV)
plan_path = parsed["plan"]
manifest_path = parsed["manifest"]

if plan_path.to_s.empty?
  warn "usage: ruby scripts/audits/audit-plan-completion.rb plan=<plan-file> [manifest=<manifest-file>]"
  exit 1
end

result = analyze_plan(plan_path)
issues = result[:issues].dup

if manifest_path
  unless File.exist?(abs(manifest_path))
    issues << "manifest does not exist: #{manifest_path}"
  else
    manifest = YAML.load_file(abs(manifest_path))
    if manifest["status"].to_s == "complete" && (manifest["planFile"].to_s != plan_path || result[:issues].any?)
      issues << "complete manifest #{manifest_path} references incomplete or mismatched plan evidence"
    end
  end
else
  manifest_rows.select { |row| row[:status] == "complete" && row[:plan_file] == plan_path }.each do |row|
    issues << "complete manifest #{row[:path]} references incomplete plan evidence" if result[:issues].any?
  end
end

result = result.merge(
  status: issues.empty? ? "passed" : "failed",
  issues: issues.uniq
)
write_report(plan_path, manifest_path, result)

if result[:status] == "passed"
  refresh_status = system("make", "control-refresh-full")
  unless refresh_status
    warn "Plan completion refresh failed after plan closeout"
    exit 1
  end
end

puts "Plan completion audit"
puts "  plan: #{plan_path}"
puts "  status: #{result[:status]}"
puts "  completion evidence: #{result[:completion_evidence]}"
puts "  open tasks: #{result[:open_tasks].size}"
puts "  issues: #{result[:issues].size}"

if result[:issues].any?
  warn "Plan completion audit failed:"
  result[:issues].each { |issue| warn "  - #{issue}" }
  exit 1
end
