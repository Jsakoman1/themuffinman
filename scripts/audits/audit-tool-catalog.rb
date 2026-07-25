#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"

root = File.expand_path("../..", __dir__)
check_only = ARGV.include?("--check")
all_files = Dir[File.join(root, "scripts/**/*.rb"), File.join(root, "scripts/*.java"), File.join(root, "apps/themuffinman/frontend/scripts/*.{mjs,js}")].select { |path| File.file?(path) }
corpus_paths = Dir[File.join(root, "Makefile"), File.join(root, "AGENTS.md"), File.join(root, "docs/**/*"), File.join(root, "apps/themuffinman/frontend/package.json")].select { |path| File.file?(path) }
corpus = corpus_paths.map { |path| File.read(path, mode: "rb").force_encoding("UTF-8") }.join("\n")
local_tool_corpus = all_files.to_h { |path| [path, File.read(path, mode: "rb").force_encoding("UTF-8")] }

rows = all_files.map do |path|
  relative = path.delete_prefix("#{root}/")
  basename = File.basename(path)
  stem = File.basename(path, File.extname(path))
  referenced = corpus.include?(relative) || corpus.include?(basename) ||
    corpus.include?(stem) ||
    local_tool_corpus.any? { |other_path, text| other_path != path && (text.include?(relative) || text.include?(basename) || text.include?(stem)) }
  category = if relative.include?("/audits/audit-")
               "audit"
             elsif relative.end_with?(".java")
               "java-ast-parser"
             elsif relative.include?("/audits/generate-")
               "generator"
             elsif relative.include?("frontend/scripts/")
               "frontend-runtime-or-validator"
             elsif relative.include?("scripts/db/")
               "database-helper"
             else
               "control-helper"
             end
  { "path" => relative, "category" => category, "referenced" => referenced }
end

unreferenced = rows.reject { |row| row["referenced"] }
report = {
  "version" => 1,
  "kind" => "local_tool_catalog",
  "summary" => {
    "tools" => rows.length,
    "referenced" => rows.count { |row| row["referenced"] },
    "unreferenced" => unreferenced.length,
    "categories" => rows.group_by { |row| row["category"] }.transform_values(&:length)
  },
  "tools" => rows,
  "unreferenced" => unreferenced.map { |row| row["path"] }
}

abort "Tool catalog found missing local tools" if rows.any? { |row| !File.file?(File.join(root, row["path"])) }
if check_only
  abort "Unreferenced local tools: #{unreferenced.map { |row| row["path"] }.join(", ")}" unless unreferenced.empty?
  puts "Tool catalog passed (#{rows.length} tools, #{report["summary"]["categories"].length} categories, 0 unreferenced)."
else
  puts JSON.pretty_generate(report)
  warn "Unreferenced local tools: #{unreferenced.map { |row| row["path"] }.join(", ")}" unless unreferenced.empty?
end
