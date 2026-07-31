#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
CATALOG_PATH = File.join(ROOT, "docs/local-tool-catalog.yaml")
REQUIRED_METADATA = %w[category owner invocation_template preconditions mutation_class expected_cost].freeze

def discovered_tools
  Dir[
    File.join(ROOT, "scripts/**/*.rb"),
    File.join(ROOT, "scripts/*.java"),
    File.join(ROOT, "apps/themuffinman/frontend/scripts/*.{mjs,js}")
  ].select { |path| File.file?(path) }.sort
end

catalog = YAML.load_file(CATALOG_PATH)
abort "Tool catalog metadata kind is invalid" unless catalog["kind"] == "local_tool_catalog_metadata"
metadata_rules = Array(catalog["metadata_rules"])
abort "Tool catalog metadata needs matching rules" if metadata_rules.empty?
metadata_rules.each do |rule|
  missing = (["pattern"] + REQUIRED_METADATA).reject { |field| rule.key?(field) && !rule[field].nil? && rule[field] != "" }
  abort "Tool metadata rule is missing #{missing.join(", ")}" unless missing.empty?
end

all_files = discovered_tools
corpus_paths = Dir[File.join(ROOT, "Makefile"), File.join(ROOT, "AGENTS.md"), File.join(ROOT, "docs/**/*"), File.join(ROOT, "apps/themuffinman/frontend/package.json")].select { |path| File.file?(path) }
corpus = corpus_paths.map { |path| File.read(path, mode: "rb").force_encoding("UTF-8") }.join("\n")
local_tool_corpus = all_files.to_h { |path| [path, File.read(path, mode: "rb").force_encoding("UTF-8")] }

rows = all_files.map do |path|
  relative = path.delete_prefix("#{ROOT}/")
  matches = metadata_rules.select { |rule| File.fnmatch?(rule.fetch("pattern"), relative, File::FNM_PATHNAME) }
  abort "Tool #{relative} matches #{matches.length} metadata rules" unless matches.length == 1
  metadata = matches.first.reject { |key, _value| key == "pattern" }
  basename = File.basename(path)
  stem = File.basename(path, File.extname(path))
  referenced = corpus.include?(relative) || corpus.include?(basename) || corpus.include?(stem) || local_tool_corpus.any? { |other_path, text| other_path != path && (text.include?(relative) || text.include?(basename) || text.include?(stem)) }
  { "path" => relative, "referenced" => referenced }.merge(metadata)
end

commands = Array(catalog["commands"])
ids = commands.map { |command| command["id"] }
targets = commands.map { |command| command["target"] }
abort "Tool catalog command ids are duplicated" unless ids.uniq.length == ids.length
abort "Tool catalog command targets are duplicated" unless targets.uniq.length == targets.length
commands.each do |command|
  missing = %w[id target purpose preconditions expected_cost].reject { |field| command.key?(field) && !command[field].nil? && command[field] != "" }
  abort "Tool catalog command #{command["id"]} is missing #{missing.join(", ")}" unless missing.empty?
end

unreferenced = rows.reject { |row| row["referenced"] }
report = {
  "version" => 2,
  "kind" => "local_tool_catalog",
  "summary" => {
    "tools" => rows.length,
    "referenced" => rows.count { |row| row["referenced"] },
    "unreferenced" => unreferenced.length,
    "categories" => rows.group_by { |row| row["category"] }.transform_values(&:length),
    "help_commands" => commands.length
  },
  "tools" => rows,
  "unreferenced" => unreferenced.map { |row| row["path"] }
}

abort "Unreferenced local tools: #{unreferenced.map { |row| row["path"] }.join(", ")}" if ARGV.include?("--check") && !unreferenced.empty?
if ARGV.include?("--check")
  puts "Tool catalog passed (#{rows.length} tools, #{report.dig("summary", "categories").length} categories, #{commands.length} help commands, 0 unreferenced)."
else
  puts JSON.pretty_generate(report)
end
