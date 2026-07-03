#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../local_tooling_common"

REPO_ROOT = LocalToolingCommon::REPO_ROOT

def rel_glob(*patterns)
  patterns.flat_map { |pattern| Dir.glob(File.join(REPO_ROOT, pattern), File::FNM_EXTGLOB) }.select { |path| File.file?(path) }.sort
end

def kind_for(path)
  relative = path.delete_prefix("#{REPO_ROOT}/")
  if relative.include?("/god-plans/")
    "god-plan"
  elsif File.basename(relative).include?("master-plan")
    "master-plan"
  else
    "plan"
  end
end

def title_from(content)
  content[/^#\s+(.+)$/, 1].to_s.strip
end

def status_from(content)
  section = content[/^## Status\s*$([\s\S]*?)(?=^## |\z)/, 1]
  value = section.to_s.lines.map(&:strip).reject(&:empty?).first.to_s
  normalized = value.sub(/\.$/, "").downcase
  return "complete" if %w[complete completed done closed].include?(normalized)
  return "active" if %w[active in-progress in_progress draft pending].include?(normalized)

  normalized.empty? ? "unknown" : normalized
end

def goal_from(content)
  section = content[/^## (Goal|Purpose|Objective)\s*$([\s\S]*?)(?=^## |\z)/, 2]
  return nil unless section

  section.lines.map(&:strip).reject(&:empty?).first
end

def frontmatter_from(content)
  LocalToolingCommon.markdown_frontmatter(content)
end

def body_without_frontmatter(content)
  return content unless content.match?(/\A---\n.*?\n---\n/m)

  content.sub(/\A---\n.*?\n---\n\n?/m, "")
end

changed = []

rel_glob(".agents/*-plan.md", ".agents/god-plans/*.md").each do |path|
  content = File.read(path)
  relative = path.delete_prefix("#{REPO_ROOT}/")
  title = title_from(content)
  status = status_from(content)
  goal = goal_from(content)
  next if title.empty? || status.empty?

  existing = frontmatter_from(content)
  desired = {
    "machine_kind" => kind_for(path),
    "machine_status" => status,
    "machine_title" => title
  }
  desired["machine_goal"] = goal if goal && !goal.empty?

  next if existing == desired

  yaml = YAML.dump(desired).sub(/\A---\n/, "").sub(/\n\.\.\.\n?\z/, "\n")
  new_content = "---\n#{yaml}---\n\n#{body_without_frontmatter(content)}"
  File.write(path, new_content)
  changed << relative
end

puts "Normalized plan metadata"
puts "  changed: #{changed.size}"
changed.first(20).each { |path| puts "  - #{path}" }
