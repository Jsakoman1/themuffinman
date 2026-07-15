#!/usr/bin/env ruby
# frozen_string_literal: true

root = File.expand_path("..", __dir__)
backlogs = Dir[File.join(root, "docs/*backlog*.md")]
open_ids = []

backlogs.each do |path|
  File.readlines(path).each_with_index do |line, index|
    open_ids << [line.match(/\b[A-Z][A-Z0-9_-]{2,}\b/)&.to_s, path, index + 1] if line.match?(/^- \[ \]/)
  end
end

references = Dir.glob(File.join(root, "{apps,services,scripts,docs,.agents}/**/*"), File::FNM_DOTMATCH)
  .select { |path| File.file?(path) }
  .reject { |path| path.include?("/.git/") }
  .map { |path| [path, File.read(path)] rescue [path, ""] }

missing = open_ids.select do |id, path, _line|
  next false if id.to_s.empty?
  references.none? { |other_path, content| other_path != path && content.include?(id) }
end

if missing.empty?
  puts "TODO audit passed"
  exit 0
end

warn "TODO audit failed:"
missing.each { |id, path, line| warn "  - #{id} has no reference outside #{path}:#{line}" }
exit 1
