#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"
require "open3"
require "time"
require "yaml"

ROOT = File.expand_path("..", __dir__)
AST_SCRIPT = File.join(ROOT, "apps/themuffinman/frontend/scripts/repository-ast-index.mjs")

query = nil
check_only = false
write_output = false
ARGV.each_with_index do |arg, index|
  query = ARGV[index + 1] if arg == "--query"
  check_only = true if arg == "--check"
  write_output = true if arg == "--write"
end

ast_stdout, ast_stderr, ast_status = Open3.capture3("node", AST_SCRIPT, "--json", chdir: ROOT)
abort(ast_stderr.empty? ? "Frontend AST index failed" : ast_stderr) unless ast_status.success?
frontend = JSON.parse(ast_stdout)
java_stdout, java_stderr, java_status = Open3.capture3("java", "scripts/RepositoryJavaAstIndex.java", "apps/themuffinman/src/main/java", chdir: ROOT)
abort(java_stderr.empty? ? "Java AST index failed" : java_stderr) unless java_status.success?
java_ast = JSON.parse(java_stdout)

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

if query
  needle = query.downcase
  map["matches"] = {
    "frontend" => frontend["files"].select { |row| JSON.generate(row).downcase.include?(needle) },
    "backend" => backend.select { |row| JSON.generate(row).downcase.include?(needle) },
    "capabilities" => capabilities.select { |row| JSON.generate(row).downcase.include?(needle) }
  }
end

if check_only
  puts "Repository context map passed (#{backend.length} backend files, #{frontend.dig("summary", "files")} frontend files, #{yaml_files.length} YAML files)."
else
  serialized = JSON.pretty_generate(map)
  if write_output
    output_path = File.join(ROOT, "docs/audit-output/repository-context-map.json")
    FileUtils.mkdir_p(File.dirname(output_path))
    File.write(output_path, serialized)
    puts "Repository context map written to docs/audit-output/repository-context-map.json"
  else
    puts serialized
  end
end
