#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

root = File.expand_path("../..", __dir__)
violations = []

Dir[File.join(root, "docs/work/*.yaml")].sort.each do |path|
  begin
    plan = YAML.load_file(path)
  rescue Psych::SyntaxError
    next
  end
  next unless plan.is_a?(Hash) && plan["kind"] == "work"

  Array(plan["tasks"]).each do |task|
    command = task.is_a?(Hash) ? task["validation"].to_s : ""
    recursive = command.match?(/\bwork-verify\b/) || (command.match?(/scripts\/verify-work\.rb/) && !command.match?(/ruby\s+-c\s+scripts\/verify-work\.rb/))
    next unless recursive

    violations << "#{path}:#{task["id"]} -> #{command}"
  end
end

if violations.empty?
  puts "Work-plan recursion audit passed (no recursive validation commands)."
else
  warn "Work-plan recursion audit failed:"
  violations.each { |violation| warn "- #{violation}" }
  exit 1
end
