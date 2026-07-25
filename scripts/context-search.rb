#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"

budget = 12_000
max_files = 12
max_rows_per_file = 8
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
  source_weight = path.start_with?("docs/") ? 2 : 1
  [-rows.length * source_weight, path]
end

max_output_chars = budget
output = +"Context search: #{query}\n"
output << "Matches: #{matches.values.sum(&:length)} in #{matches.length} files; showing up to #{max_files} files.\n\n"
output << "Canonical authorities: docs/system-map.md, docs/capability-inventory.yaml, docs/implementation-control.md, docs/runtime-acceptance-matrix.yaml\n\n"

ranked.first(max_files).each do |path, rows|
  display_path = path.delete_prefix("./")
  output << "## #{display_path} (#{rows.length} matches)\n"
  rows.first(max_rows_per_file).each do |line_number, text|
    output << "#{line_number}: #{text}\n"
  end
  output << "\n"
end

if output.length > max_output_chars
  output = output.byteslice(0, max_output_chars)
  output << "\n...[context truncated at #{max_output_chars} characters]\n"
end

puts output
warn stderr unless stderr.empty?
