#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "open3"
require "pathname"
require "set"

PLACEHOLDER = /\A(?:|tbd|todo|pending|draft|unknown|not started|replace.*|none)\z/i
FINAL = /\A(?:complete|completed|done)\.?\z/i
CODE_PREFIXES = %w[apps/ services/ scripts/].freeze

def args_to_hash(argv)
  argv.each_with_object({}) do |arg, values|
    key, value = arg.split("=", 2)
    abort "usage: ruby scripts/audits/audit-plan-completion.rb plan=<plan-file> [manifest=<manifest-file>]" if value.nil?
    values[key] = value
  end
end

def section(content, heading)
  content[/^## #{Regexp.escape(heading)}\s*$([\s\S]*?)(?=^## |\z)/, 1].to_s
end

def field(content, name)
  content[/^\s*-\s*#{Regexp.escape(name)}:\s*(.+?)\s*$/i, 1].to_s.strip
end

def frontmatter(content)
  raw = content[/\A---\s*\n([\s\S]*?)\n---\s*\n/, 1]
  raw ? YAML.safe_load(raw, permitted_classes: [], aliases: false) || {} : {}
rescue Psych::Exception
  {}
end

def placeholder?(value)
  value.to_s.strip.match?(PLACEHOLDER)
end

def repo_path?(repo_root, path)
  expanded = File.expand_path(path, repo_root)
  expanded == repo_root || expanded.start_with?("#{repo_root}/")
end

def code_path?(path)
  path == "Makefile" || CODE_PREFIXES.any? { |prefix| path.start_with?(prefix) }
end

def paths_from(value)
  value.to_s.scan(/`([^`]+)`/).flatten.map do |candidate|
    candidate if candidate == "Makefile" || candidate.match?(%r{\A(?:apps|services|scripts)/})
  end.compact.uniq
end

def git_paths(repo_root, baseline)
  diff, status = Open3.capture2e("git", "-C", repo_root, "diff", "--name-only", baseline)
  return [[], "could not diff against baseline #{baseline}: #{diff.strip}"] unless status.success?

  tracked = diff.lines.map(&:strip).reject(&:empty?)
  porcelain, porcelain_status = Open3.capture2e("git", "-C", repo_root, "status", "--porcelain=v1", "--untracked-files=all")
  return [tracked, "could not read git status: #{porcelain.strip}"] unless porcelain_status.success?

  untracked = porcelain.lines.map do |line|
    line.start_with?("?? ") ? line[3..].strip : nil
  end.compact
  [(tracked + untracked).uniq, nil]
end

def manifest_for_plan(repo_root, plan_file, explicit_manifest, plan_content)
  return explicit_manifest unless explicit_manifest.to_s.empty?

  frame = section(plan_content, "Workflow Frame")
  decision = field(frame, "Manifest decision")
  path = field(frame, "Manifest path")
  return nil unless decision.casecmp?("required")
  return nil if placeholder?(path) || path.include?("create only")

  path
end

def validate_manifest(repo_root, manifest_file, plan_file, implemented_paths, changed_paths, errors)
  return if manifest_file.nil?

  unless repo_path?(repo_root, manifest_file) && File.file?(File.join(repo_root, manifest_file))
    errors << "required manifest is missing: #{manifest_file}"
    return
  end

  manifest = YAML.load_file(File.join(repo_root, manifest_file)) || {}
  errors << "manifest closeoutContractVersion must be 2" unless manifest["closeoutContractVersion"].to_i == 2
  errors << "manifest status must be complete" unless manifest["status"] == "complete"
  errors << "manifest planFile must match #{plan_file}" unless manifest["planFile"] == plan_file
  errors << "manifest checklist.codeImplemented must be true" unless manifest.dig("checklist", "codeImplemented") == true

  code_paths = Array(manifest.dig("artifacts", "codePaths")).select { |path| code_path?(path) }
  errors << "manifest must list at least one runtime or tooling code path" if code_paths.empty?
  if code_paths.any? && (code_paths & changed_paths).empty?
    errors << "manifest codePaths do not intersect files changed since the plan baseline"
  end
  if code_paths.any? && implemented_paths.any? && (code_paths & implemented_paths & changed_paths).empty?
    errors << "plan implemented code paths, manifest codePaths, and baseline diff must share at least one path"
  end
rescue Psych::Exception => e
  errors << "could not parse manifest #{manifest_file}: #{e.message}"
end

def child_plan_paths(content)
  [section(content, "Master Plan Frame"), section(content, "Plan Breakdown")]
    .join("\n")
    .scan(%r{\.agents/[A-Za-z0-9_-]+-plan\.md})
    .uniq
end

def audit_plan(repo_root, plan_file, explicit_manifest: nil, visited: Set.new)
  errors = []
  normalized = Pathname.new(plan_file).cleanpath.to_s
  return [[], ["plan hierarchy contains a cycle at #{normalized}"]] if visited.include?(normalized)
  visited.add(normalized)

  unless repo_path?(repo_root, normalized) && File.file?(File.join(repo_root, normalized))
    return [[], ["plan is missing: #{normalized}"]]
  end

  content = File.read(File.join(repo_root, normalized))
  metadata = frontmatter(content)
  is_master = metadata["machine_kind"] == "master_plan" || normalized.include?("master-plan") || content.include?("## Master Plan Frame")
  errors << "#{normalized}: machine_closeout_contract must be 2" unless metadata["machine_closeout_contract"].to_i == 2
  errors << "#{normalized}: machine_status must be complete" unless metadata["machine_status"] == "complete"
  errors << "#{normalized}: ## Status must be Complete" unless field(section(content, "Status"), "").match?(FINAL) || section(content, "Status").strip.match?(FINAL)

  completion = section(content, "Completion Evidence")
  errors << "#{normalized}: missing ## Completion Evidence" if completion.empty?
  errors << "#{normalized}: completion evidence status must be complete" unless field(completion, "Status").match?(FINAL)
  ["Changed files", "Validation evidence", "Doc delta summary", "Backlog update", "Residual risk"].each do |name|
    errors << "#{normalized}: completion evidence #{name.downcase} is missing or placeholder" if placeholder?(field(completion, name))
  end
  open_tasks = content.lines.select { |line| line.start_with?("- [ ]") }
  errors << "#{normalized}: #{open_tasks.size} unchecked plan task(s) remain" unless open_tasks.empty?

  baseline = metadata["machine_baseline_ref"].to_s
  if !is_master
    errors << "#{normalized}: machine_baseline_ref is missing or placeholder" if placeholder?(baseline)
    unless placeholder?(baseline)
      _, baseline_status = Open3.capture2e("git", "-C", repo_root, "cat-file", "-e", "#{baseline}^{commit}")
      errors << "#{normalized}: machine_baseline_ref is not a Git commit: #{baseline}" unless baseline_status.success?
      changed_paths, git_error = git_paths(repo_root, baseline)
      errors << "#{normalized}: #{git_error}" if git_error
      evidence_baseline = field(completion, "Baseline ref").delete("`")
      errors << "#{normalized}: completion evidence baseline must match machine_baseline_ref" unless evidence_baseline == baseline
      implemented_paths = paths_from(field(completion, "Implemented code paths"))
      errors << "#{normalized}: implemented code paths must name runtime or tooling code" if implemented_paths.empty?
      errors << "#{normalized}: implemented code paths do not intersect files changed since baseline" if implemented_paths.any? && (implemented_paths & changed_paths).empty?
      manifest_file = manifest_for_plan(repo_root, normalized, explicit_manifest, content)
      frame = section(content, "Workflow Frame")
      errors << "#{normalized}: a required manifest path is missing" if field(frame, "Manifest decision").casecmp?("required") && manifest_file.nil?
      validate_manifest(repo_root, manifest_file, normalized, implemented_paths, changed_paths, errors) if manifest_file
    end
  end

  if is_master
    children = child_plan_paths(content).reject { |path| path == normalized }
    errors << "#{normalized}: master plan must explicitly list child plan files" if children.empty?
    children.each do |child|
      _, child_errors = audit_plan(repo_root, child, visited: visited)
      errors.concat(child_errors.map { |error| "#{normalized} -> #{error}" })
    end
  end

  [[], errors]
end

args = args_to_hash(ARGV)
repo_root = File.expand_path("../..", __dir__)
plan_file = args["plan"]
abort "usage: ruby scripts/audits/audit-plan-completion.rb plan=<plan-file> [manifest=<manifest-file>]" if plan_file.to_s.empty?

_result, errors = audit_plan(repo_root, plan_file, explicit_manifest: args["manifest"])
puts "Plan completion audit"
puts "  Plan: #{plan_file}"
puts "  Manifest: #{args.fetch("manifest", "auto")}" 
if errors.empty?
  puts "Plan completion audit passed"
  exit 0
end

warn "Plan completion audit failed:"
errors.uniq.each { |error| warn "  - #{error}" }
exit 1
