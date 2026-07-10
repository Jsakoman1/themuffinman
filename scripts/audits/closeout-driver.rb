#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "time"
require "yaml"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT
OUT_ROOT = "docs/generated/local-tooling/closeout-driver"

def options(argv)
  argv.each_with_object({}) do |arg, parsed|
    key, value = arg.split("=", 2)
    parsed[key] = value if value
  end
end

def abs(path)
  File.join(REPO_ROOT, path.to_s)
end

def slug(path)
  File.basename(path.to_s, File.extname(path.to_s)).gsub(/[^A-Za-z0-9_-]+/, "-")
end

def read(path)
  File.read(abs(path))
end

def completion_section(content)
  match = content.match(/^## Completion Evidence\s*$([\s\S]*?)(?=^## |\z)/)
  match && match[1]
end

def completion_status(section)
  return nil unless section

  section[/^\s*-\s*Status:\s*(.+?)\s*$/i, 1]&.strip
end

def top_level_status(content)
  section = content.match(/^## Status\s*$([\s\S]*?)(?=^## |\z)/)
  section && section[1].lines.map(&:strip).reject(&:empty?).first
end

def final_status?(status)
  normalized = status.to_s.strip.downcase.gsub(/[.\s]+\z/, "")
  normalized.match?(/\A(?:complete|completed|done|deferred|skipped|n\/a|not applicable|blocked)\z/i)
end

def open_task_lines(content)
  content.lines.each_with_index.each_with_object([]) do |(line, index), rows|
    rows << {line: index + 1, text: line.strip} if line.start_with?("- [ ]")
  end
end

def deferred_task?(text)
  text.match?(/\bdeferred\b/i) && text.match?(/\b[A-Z][A-Z0-9]+-[A-Z0-9-]+\b/)
end

def child_plan_rows(content)
  content.lines.each_with_index.each_with_object([]) do |(line, index), rows|
    next unless line.match?(/^\s*-\s*\[[ xX]\]/)
    path = line[/`(\.agents\/[^`]+\.md)`/, 1]
    next unless path

    rows << {
      line: index + 1,
      checked: line.match?(/^\s*-\s*\[[xX]\]/),
      path: path,
      text: line.strip
    }
  end
end

def temp_work_products(plan_path)
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
      purpose: payload["purpose"] || payload[:purpose],
      status: payload["status"] || payload[:status],
      delete_when: payload["deleteWhen"] || payload[:deleteWhen]
    }
  rescue StandardError
    nil
  end.compact
end

def archive_path?(path)
  path.start_with?("docs/generated/local-tooling/.history/", "docs/generated/local-tooling/.cache/", ".agents/archive/")
end

def generated_freshness_blockers
  path = abs("docs/generated/local-tooling/generated-artifact-freshness.json")
  return [{severity: "block", message: "generated-artifact-freshness report is missing"}] unless File.exist?(path)

  payload = JSON.parse(File.read(path))
  Array(payload["artifacts"]).select { |entry| entry["status"].to_s == "stale" }.map do |entry|
    {severity: "block", message: "stale generated artifact: #{entry['artifact']}"}
  end
rescue StandardError
  [{severity: "block", message: "generated-artifact-freshness report could not be parsed"}]
end

def plan_blockers(plan_path)
  blockers = []
  warnings = []
  unless File.exist?(abs(plan_path))
    return {blockers: ["plan does not exist: #{plan_path}"], warnings: []}
  end

  content = read(plan_path)
  machine_status = LocalToolingCommon.markdown_frontmatter_value(content, "machine_status").to_s.strip
  top_status = top_level_status(content)
  section = completion_section(content)
  completion = completion_status(section)
  open_tasks = open_task_lines(content)
  child_rows = child_plan_rows(content)
  temp_rows = temp_work_products(plan_path)

  blockers << "plan is not marked complete yet" unless machine_status.match?(/\A(?:complete|completed|done)\z/i) || top_status.to_s.match?(/\A(?:complete|completed|done)\z/i)
  blockers << "machine_status is not final: #{machine_status}" unless machine_status.to_s.strip.empty? || final_status?(machine_status)
  blockers << "top-level status is not final: #{top_status}" unless top_status.to_s.strip.empty? || final_status?(top_status)
  blockers << "completion evidence status is missing or not final" if completion.to_s.empty? || !final_status?(completion)

  open_tasks.reject { |task| deferred_task?(task[:text]) }.each do |task|
    blockers << "open task at #{plan_path}:#{task[:line]} is not explicitly deferred to a backlog ID"
  end

  child_rows.each do |row|
    status = row[:checked] ? "complete" : "incomplete"
    status = "deferred" if !row[:checked] && deferred_task?(row[:text])
    blockers << "child plan #{row[:path]} at #{plan_path}:#{row[:line]} is incomplete without an explicit deferred backlog ID" if status == "incomplete"
  end

  temp_rows.each do |row|
    blockers << "temporary work product #{row[:path]} owned by #{plan_path} must be deleted, promoted, or archived before closeout"
  end

  {
    blockers: blockers.uniq,
    warnings: warnings.uniq,
    machine_status: machine_status,
    top_level_status: top_status,
    completion_status: completion,
    open_tasks: open_tasks.size,
    child_plans: child_rows.size,
    temp_work_products: temp_rows
  }
end

def preflight_report(plan_path, manifest_path, files)
  report = {
    generated_at: Time.now.utc.iso8601,
    plan_path: plan_path,
    manifest_path: manifest_path,
    changed_files: files,
    generated_artifact_freshness: generated_freshness_blockers,
    plan_report: plan_blockers(plan_path),
    archive_only_evidence: []
  }

  if manifest_path && File.exist?(abs(manifest_path))
    manifest = YAML.load_file(abs(manifest_path))
    artifact_paths = []
    artifact_groups = manifest["artifacts"] || {}
    %w[codePaths docPaths testPaths generatorCommands auditCommands].each do |group|
      Array(artifact_groups[group]).each { |path| artifact_paths << [group, path] }
    end
    archive_only = artifact_paths.select { |_group, path| archive_path?(path) }.map do |group, path|
      {group: group, path: path}
    end
    refreshed = Array(manifest.dig("generatedArtifacts", "refreshedPaths")).select { |path| archive_path?(path) }.map do |path|
      {group: "generatedArtifacts", path: path}
    end
    report[:archive_only_evidence] = archive_only + refreshed
    if manifest["status"].to_s != "complete"
      report[:plan_report][:blockers] << "manifest status must be complete for closeout"
    end
  else
    report[:plan_report][:blockers] << "manifest is missing: #{manifest_path}"
  end

  report
end

def write_report(base_name, payload)
  json_path = "#{OUT_ROOT}/#{base_name}.json"
  summary_path = "#{OUT_ROOT}/#{base_name}-summary.md"
  LocalToolingCommon.write_json(json_path, payload)

  lines = ["# Closeout Driver #{base_name}", ""]
  lines << "- Plan: `#{payload[:plan_path]}`"
  lines << "- Manifest: `#{payload[:manifest_path]}`"
  lines << "- Changed files: `#{payload[:changed_files].size}`"
  lines << "- Blockers: `#{payload.dig(:plan_report, :blockers).size}`"
  lines << "- Warnings: `#{payload.dig(:plan_report, :warnings).size}`"
  lines << "- Archive-only evidence: `#{payload[:archive_only_evidence].size}`"
  lines << ""
  if payload[:plan_report].is_a?(Hash)
    lines << "## Plan"
    lines << ""
    lines << "- Machine status: `#{payload.dig(:plan_report, :machine_status)}`"
    lines << "- Top-level status: `#{payload.dig(:plan_report, :top_level_status)}`"
    lines << "- Completion status: `#{payload.dig(:plan_report, :completion_status)}`"
    lines << "- Open tasks: `#{payload.dig(:plan_report, :open_tasks)}`"
    lines << "- Child plans: `#{payload.dig(:plan_report, :child_plans)}`"
    lines << "- Temp work products: `#{payload.dig(:plan_report, :temp_work_products).size}`"
    lines << ""
  end
  if payload[:generated_artifact_freshness].any?
    lines << "## Generated Artifacts"
    lines << ""
    payload[:generated_artifact_freshness].each { |entry| lines << "- #{entry[:severity]}: #{entry[:message]}" }
    lines << ""
  end
  if payload[:archive_only_evidence].any?
    lines << "## Archive-Only Evidence"
    lines << ""
    payload[:archive_only_evidence].each { |entry| lines << "- #{entry[:group]}: #{entry[:path]}" }
    lines << ""
  end
  if payload[:steps].any?
    lines << "## Steps"
    lines << ""
    payload[:steps].each do |step|
      lines << "- `#{step[:status]}` #{step[:label]}: #{step[:command]}"
    end
    lines << ""
  end
  if payload[:errors].any?
    lines << "## Errors"
    lines << ""
    payload[:errors].each { |error| lines << "- #{error}" }
    lines << ""
  end
  LocalToolingCommon.write_text(summary_path, lines.join("\n"))
  [json_path, summary_path]
end

def run_step(label, command, steps)
  stdout, stderr, status = Open3.capture3(*command, chdir: REPO_ROOT)
  result = {
    label: label,
    command: command.join(" "),
    status: status.success? ? "passed" : "failed",
    stdout: LocalToolingCommon.clean_text_output(stdout, max_lines: 20, aggressive: true),
    stderr: LocalToolingCommon.clean_text_output(stderr, max_lines: 20, aggressive: true)
  }
  steps << result
  result
end

parsed = options(ARGV)
plan_path = parsed["plan"].to_s.strip
manifest_path = parsed["manifest"].to_s.strip
files = parsed["files"].to_s.split(",").map(&:strip).reject(&:empty?)

if plan_path.empty? || manifest_path.empty?
  warn "usage: ruby scripts/audits/closeout-driver.rb plan=<plan-file> manifest=<manifest-file> [files=<csv>]"
  exit 1
end

steps = []

freshness_step = run_step("generated-artifact-freshness", ["make", "audit-generated-artifact-freshness"], steps)
if freshness_step[:status] != "passed"
  report = {
    generated_at: Time.now.utc.iso8601,
    plan_path: plan_path,
    manifest_path: manifest_path,
    changed_files: files,
    generated_artifact_freshness: [{severity: "block", message: "generated-artifact-freshness command failed"}],
    plan_report: {blockers: ["generated-artifact-freshness command failed"], warnings: []},
    archive_only_evidence: [],
    steps: steps,
    errors: ["generated-artifact-freshness failed"]
  }
  write_report("preflight/#{slug(plan_path)}", report)
  warn "Closeout preflight failed:"
  warn "  - generated-artifact-freshness command failed"
  exit 1
end

report = preflight_report(plan_path, manifest_path, files)
preflight_blockers = Array(report.dig(:plan_report, :blockers)) + Array(report[:generated_artifact_freshness]).select { |entry| entry[:severity] == "block" }.map { |entry| entry[:message] } + Array(report[:archive_only_evidence]).map { |entry| "archive-only evidence: #{entry[:group]} #{entry[:path]}" }
report[:status] = preflight_blockers.empty? ? "passed" : "failed"
report[:steps] = steps
report[:errors] = preflight_blockers
write_report("preflight/#{slug(plan_path)}", report)

unless preflight_blockers.empty?
  warn "Closeout preflight failed:"
  preflight_blockers.each { |blocker| warn "  - #{blocker}" }
  exit 1
end

sequence = [
  ["cleanup-generated-history", ["make", "cleanup-generated-history"]],
  ["temp-work-product-closeout", ["make", "temp-work-product-closeout", "plan=#{plan_path}"]],
  ["autofill-feature-closeout", ["make", "autofill-feature-closeout", "manifest=#{manifest_path}", "ready=true"]],
  ["audit-plan-completion", ["make", "audit-plan-completion", "plan=#{plan_path}", "manifest=#{manifest_path}"]],
  ["feature-closeout-audit", ["make", "feature-closeout-audit", "manifest=#{manifest_path}"]],
  ["closeout-report", ["make", "closeout-report", "manifest=#{manifest_path}"]]
]

sequence.each do |label, command|
  result = run_step(label, command, steps)
  write_report("driver/#{slug(plan_path)}", report.merge(steps: steps, errors: []))
  next if result[:status] == "passed"

  report[:status] = "failed"
  report[:steps] = steps
  report[:errors] = ["step failed: #{label}"]
  write_report("driver/#{slug(plan_path)}", report)
  warn "Closeout driver failed at #{label}"
  exit 1
end

report[:status] = "passed"
report[:steps] = steps
report[:errors] = []
write_report("driver/#{slug(plan_path)}", report)

puts "Closeout driver"
puts "  plan: #{plan_path}"
puts "  manifest: #{manifest_path}"
puts "  status: passed"
