#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"
require "open3"
require "time"
require "yaml"
require_relative "../dora/lib/dora/plugins/java_ast_index"
require_relative "../dora/lib/dora/plugins/typescript_vue_ast_index"

ROOT = File.expand_path("..", __dir__)

query = nil
check_only = false
write_output = false
max_output = 20_000
ARGV.each_with_index do |arg, index|
  query = ARGV[index + 1] if arg == "--query"
  check_only = true if arg == "--check"
  write_output = true if arg == "--write"
  next unless arg == "--max-output"

  begin
    max_output = Integer(ARGV[index + 1])
  rescue ArgumentError, TypeError
    abort "--max-output must be a positive integer"
  end
  abort "--max-output must be at least 512" if max_output < 512
end

frontend = Dora::Plugins::TypeScriptVueAstIndex.index!(root: ROOT, source_roots: [{"id" => "frontend", "path" => "apps/themuffinman/frontend/src"}], package_root: "apps/themuffinman/frontend")
java_ast = Dora::Plugins::JavaAstIndex.index!(root: ROOT, source_roots: [{"id" => "backend", "path" => "apps/themuffinman/src/main/java"}])

java_files = Dir[File.join(ROOT, "apps/themuffinman/src/main/java/**/*.java")]
backend = java_files.map do |path|
  source = File.read(path)
  relative = path.delete_prefix("#{ROOT}/")
  {
    "path" => relative,
    "package" => source[/^\s*package\s+([^;]+);/, 1],
    "classes" => source.scan(/\b(?:public\s+|private\s+|protected\s+)?(?:abstract\s+|final\s+)?class\s+(\w+)/).flatten,
    "interfaces" => source.scan(/\binterface\s+(\w+)/).flatten,
    "controllers" => source.include?("@RestController") || source.include?("@Controller"),
    "services" => source.include?("@Service"),
    "repositories" => source.include?("@Repository") || relative.include?("/repository/"),
    "endpoints" => source.scan(/@(Get|Post|Put|Patch|Delete|Request)Mapping\s*\(\s*(?:value\s*=\s*)?[\"']([^\"']+)[\"']/).map { |verb, route| { "verb" => verb.upcase, "route" => route } }
  }
end

yaml_files = Dir[File.join(ROOT, "docs/**/*.yaml")]
yaml_kinds = yaml_files.each_with_object(Hash.new(0)) do |path, counts|
  parsed = YAML.load_file(path)
  counts[parsed["kind"].to_s] += 1 if parsed.is_a?(Hash) && parsed["kind"]
rescue StandardError
  counts["parse_error"] += 1
end

def referenced_paths(value, paths = [])
  case value
  when Hash then value.each_value { |child| referenced_paths(child, paths) }
  when Array then value.each { |child| referenced_paths(child, paths) }
  when String then paths << value if value.match?(%r{^(?:apps|docs|scripts)/})
  end
  paths
end

def resolve_local_import(root, source_path, import_path)
  base = File.expand_path(import_path, File.dirname(File.join(root, source_path)))
  candidates = [base, "#{base}.ts", "#{base}.js", "#{base}.mjs", "#{base}.vue", File.join(base, "index.ts"), File.join(base, "index.js")]
  resolved = candidates.find { |candidate| File.file?(candidate) }
  [base.delete_prefix("#{root}/"), resolved && resolved.delete_prefix("#{root}/")]
end

inventory = YAML.load_file(File.join(ROOT, "docs/capability-inventory.yaml"))
capabilities = Array(inventory["modules"]).flat_map { |module_row| Array(module_row["capabilities"]) }.map do |capability|
  paths = referenced_paths(capability).uniq
  { "id" => capability["id"], "status" => capability["status"], "paths" => paths }
end

import_edges = frontend.fetch("files").flat_map do |file|
  file.fetch("imports").each_with_object([]) do |import_path, edges|
    next unless import_path.start_with?(".")
    target, resolved = resolve_local_import(ROOT, file.fetch("path"), import_path)
    edges << { "from" => file.fetch("path"), "to" => target, "resolved" => !resolved.nil?, "resolved_path" => resolved }
  end
end

map = {
  "version" => 1,
  "kind" => "repository_context_map",
  "generated_at" => Time.now.utc.iso8601,
  "frontend_ast" => frontend,
  "backend_ast" => java_ast,
  "backend" => {
    "files" => backend,
    "summary" => {
      "files" => backend.length,
      "classes" => backend.sum { |row| row["classes"].length },
      "controllers" => backend.count { |row| row["controllers"] },
      "services" => backend.count { |row| row["services"] },
      "repositories" => backend.count { |row| row["repositories"] },
      "endpoint_annotations" => backend.sum { |row| row["endpoints"].length }
    }
  },
  "documentation" => {
    "yaml_files" => yaml_files.length,
    "kinds" => yaml_kinds
  },
  "graph" => {
    "capabilities" => capabilities,
    "import_edges" => import_edges,
    "summary" => {
      "capability_nodes" => capabilities.length,
      "frontend_import_edges" => import_edges.length,
      "unresolved_frontend_import_edges" => import_edges.count { |edge| !edge["resolved"] },
      "backend_symbol_nodes" => java_ast.dig("summary", "symbols") || 0
    }
  }
}

def compact_frontend_match(row, needle)
  {
    "path" => row.fetch("path"),
    "symbols" => row.fetch("symbols").select { |symbol| JSON.generate(symbol).downcase.include?(needle) },
    "matching_imports" => row.fetch("imports").select { |import_path| import_path.downcase.include?(needle) }
  }
end

def compact_backend_match(row)
  row.slice("path", "package", "classes", "interfaces", "controllers", "services", "repositories", "endpoints")
end

def bounded_rows(rows, max_output)
  selected = []
  rows.each do |row|
    candidate = selected + [row]
    break if JSON.generate(candidate).bytesize > max_output

    selected << row
  end
  { "rows" => selected, "truncated" => selected.length < rows.length }
end

def compact_query_map(map, query, max_output)
  needle = query.downcase
  frontend_matches = map.dig("frontend_ast", "files").select { |row| JSON.generate(row).downcase.include?(needle) }
    .map { |row| compact_frontend_match(row, needle) }
  backend_matches = map.dig("backend", "files").select { |row| JSON.generate(row).downcase.include?(needle) }
    .map { |row| compact_backend_match(row) }
  capability_matches = map.dig("graph", "capabilities").select { |row| JSON.generate(row).downcase.include?(needle) }

  # Keep each result family bounded so a common query never turns a targeted lookup
  # into a serialization of the complete AST/import graph.
  per_family_budget = [max_output / 3, 512].max
  frontend_result = bounded_rows(frontend_matches, per_family_budget)
  backend_result = bounded_rows(backend_matches, per_family_budget)
  capability_result = bounded_rows(capability_matches, per_family_budget)

  {
    "version" => map.fetch("version"),
    "kind" => "repository_context_query",
    "generated_at" => map.fetch("generated_at"),
    "query" => query,
    "max_output_bytes" => max_output,
    "summary" => {
      "backend_files" => map.dig("backend", "summary", "files"),
      "frontend_files" => map.dig("frontend_ast", "summary", "files"),
      "capability_nodes" => map.dig("graph", "summary", "capability_nodes"),
      "frontend_import_edges" => map.dig("graph", "summary", "frontend_import_edges")
    },
    "matches" => {
      "frontend" => frontend_result.fetch("rows"),
      "backend" => backend_result.fetch("rows"),
      "capabilities" => capability_result.fetch("rows")
    },
    "truncated" => {
      "frontend" => frontend_result.fetch("truncated"),
      "backend" => backend_result.fetch("truncated"),
      "capabilities" => capability_result.fetch("truncated")
    }
  }
end

if check_only
  puts "Repository context map passed (#{backend.length} backend files, #{frontend.dig("summary", "files")} frontend files, #{yaml_files.length} YAML files)."
else
  output = query ? compact_query_map(map, query, max_output) : map
  serialized = JSON.pretty_generate(output)
  abort "Repository context query exceeded --max-output (#{serialized.bytesize} > #{max_output})" if query && serialized.bytesize > max_output
  if write_output
    output_path = File.join(ROOT, "docs/audit-output/repository-context-map.json")
    FileUtils.mkdir_p(File.dirname(output_path))
    File.write(output_path, JSON.pretty_generate(map))
    puts "Repository context map written to docs/audit-output/repository-context-map.json"
  else
    puts serialized
  end
end
