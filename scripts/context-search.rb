#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"

budget = 12_000
max_files = 12
max_rows_per_file = 8
mode = "general"
if (index = ARGV.index("--mode"))
  mode = ARGV[index + 1].to_s
  ARGV.slice!(index, 2)
end
mode_rules = {
  "general" => {
    "description" => "balanced source and canonical-document ranking",
    "weight" => ->(path) { path.start_with?("docs/") ? 2 : 1 }
  },
  "symbol" => {
    "description" => "source definitions and typed client/backend references before documentation",
    "weight" => ->(path) { path.start_with?("apps/") ? 4 : path.start_with?("scripts/") ? 3 : 1 }
  },
  "callsite" => {
    "description" => "executable callers, controllers, services, and client callsites before prose",
    "weight" => ->(path) { path.start_with?("apps/") ? 5 : path.start_with?("scripts/") ? 2 : 1 }
  },
  "canonical" => {
    "description" => "canonical documentation before implementation references",
    "weight" => lambda do |path|
      next 6 if %w[docs/system-map.md docs/capability-inventory.yaml docs/implementation-control.md docs/runtime-acceptance-matrix.yaml].include?(path)

      path.start_with?("docs/") ? 4 : 1
    end
  }
}.freeze
abort "--mode must be one of: #{mode_rules.keys.join(", ")}" unless mode_rules.key?(mode)
if (index = ARGV.index("--budget"))
  begin
    budget = Integer(ARGV[index + 1])
  rescue ArgumentError, TypeError
    abort "budget must be a positive integer"
  end
  ARGV.slice!(index, 2)
end
[["--max-files", :max_files], ["--max-lines", :max_rows_per_file]].each do |flag, variable|
  next unless (index = ARGV.index(flag))
  begin
    value = Integer(ARGV[index + 1])
  rescue ArgumentError, TypeError
    abort "#{flag} must be a positive integer"
  end
  abort "#{flag} must be at least 1" if value < 1
  if variable == :max_files
    max_files = value
  else
    max_rows_per_file = value
  end
  ARGV.slice!(index, 2)
end
abort "budget must be at least 512 characters" if budget < 512

query = ARGV.join(" ").strip
if query.empty?
  warn "usage: ruby scripts/context-search.rb <search phrase>"
  exit 1
end

root = File.expand_path("..", __dir__)
globs = [
  "*.java", "*.kt", "*.vue", "*.ts", "*.mjs", "*.js", "*.rb",
  "*.yaml", "*.yml", "*.md", "*.properties", "Makefile", "AGENTS.md"
]
excluded = [
  "!apps/themuffinman/frontend/node_modules/**",
  "!apps/themuffinman/frontend/dist/**",
  "!docs/audit-output/**",
  "!docs/runtime-evidence/**",
  "!target/**",
  "!.git/**"
]

command = ["rg", "-n", "-i", "--fixed-strings", "--no-heading", "--color", "never"]
globs.each { |glob| command.concat(["--glob", glob]) }
excluded.each { |glob| command.concat(["--glob", glob]) }
command.concat([query, "."])

stdout, stderr, status = Open3.capture3(*command, chdir: root)
if !status.success? && stdout.empty?
  puts "No matches for: #{query}"
  exit 0
end

matches = Hash.new { |hash, path| hash[path] = [] }
stdout.each_line do |line|
  path, line_number, text = line.chomp.split(":", 3)
  next if path.nil? || line_number.nil? || text.nil?

  matches[path] << [line_number.to_i, text.strip]
end

ranked = matches.sort_by do |path, rows|
  [-rows.length * mode_rules.fetch(mode).fetch("weight").call(path), path]
end

max_output_chars = budget
output = +"Context search (mode=#{mode}): #{query}\n"
output << "Matches: #{matches.values.sum(&:length)} in #{matches.length} files; showing up to #{max_files} files.\n\n"
output << "Ranking: #{mode_rules.fetch(mode).fetch("description")}.\n\n"
output << "Canonical authorities: docs/system-map.md, docs/capability-inventory.yaml, docs/implementation-control.md, docs/runtime-acceptance-matrix.yaml\n\n"

ranked.first(max_files).each do |path, rows|
  display_path = path.delete_prefix("./")
  output << "## #{display_path} (#{rows.length} matches)\n"
  rows.first(max_rows_per_file).each do |line_number, text|
    output << "#{line_number}: #{text}\n"
  end
  output << "\n"
end

if output.bytesize > max_output_chars
  suffix = "\n...[context truncated at #{max_output_chars} bytes]\n"
  output = output.byteslice(0, max_output_chars - suffix.bytesize)
  output << suffix
end

puts output
warn stderr unless stderr.empty?
