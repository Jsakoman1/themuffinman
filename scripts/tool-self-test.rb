#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "json"
require "yaml"

ROOT = File.expand_path("..", __dir__)
STAGE_RESULTS = Hash.new do |stages, id|
  stages[id] = { stage_class: nil, details: [] }
end

STAGE_CONTRACT = {
  "ruby_syntax" => "tool_mechanics",
  "frontend_script_syntax" => "tool_mechanics",
  "yaml_parse" => "tool_mechanics",
  "frontend_ast_index" => "tool_mechanics",
  "generated_contracts" => "product_contract",
  "web_surface_contract" => "product_contract",
  "admin_agent_scenarios" => "product_contract",
  "modern_surface_contract" => "product_contract",
  "repository_map" => "tool_mechanics",
  "repository_map_query" => "tool_mechanics",
  "tool_catalog" => "tool_mechanics",
  "runtime_tools" => "tool_mechanics",
  "context_search" => "tool_mechanics",
  "change_validation" => "tool_mechanics",
  "template_freshness" => "tool_mechanics",
  "work_artifact_schema" => "tool_mechanics",
  "tool_help" => "tool_mechanics"
}.freeze

tooling_only = ARGV.delete("--tooling-only")
check_stage_contract = ARGV.delete("--check-stage-contract")
verbose = ARGV.delete("--verbose")
abort "usage: ruby scripts/tool-self-test.rb [--tooling-only] [--verbose] [--check-stage-contract]" unless ARGV.empty?

if check_stage_contract
  required = %w[tool_mechanics product_contract]
  actual = STAGE_CONTRACT.values.uniq
  abort "Stage contract is missing #{(required - actual).join(", ")}" unless (required - actual).empty?
  abort "Stage ids are duplicated" unless STAGE_CONTRACT.keys.uniq.length == STAGE_CONTRACT.length
  puts "Tool self-test stage contract passed (#{STAGE_CONTRACT.length} stages, tooling and product classes)."
  exit 0
end

def record_stage(stage_id, stage_class, detail)
  result = STAGE_RESULTS[stage_id]
  result[:stage_class] ||= stage_class
  result[:details] << detail
end

def run(stage_id, stage_class, tooling_only, *command)
  return if tooling_only && stage_class != "tool_mechanics"

  stdout, stderr, status = Open3.capture3(*command, chdir: ROOT)
  abort "FAILED class=#{stage_class} id=#{stage_id}: #{command.join(" ")}\n#{stdout}\n#{stderr}" unless status.success?
  record_stage(stage_id, stage_class, command.join(" "))
  stdout
end

Dir[File.join(ROOT, "scripts/**/*.rb")].sort.each { |path| run("ruby_syntax", "tool_mechanics", tooling_only, "ruby", "-c", path) }
Dir[File.join(ROOT, "apps/themuffinman/frontend/scripts/*.mjs")].sort.each { |path| run("frontend_script_syntax", "tool_mechanics", tooling_only, "node", "--check", path) }
Dir[File.join(ROOT, "docs/**/*.yaml")].sort.each do |path|
  YAML.load_file(path)
  record_stage("yaml_parse", "tool_mechanics", path.delete_prefix("#{ROOT}/"))
end

run("frontend_ast_index", "tool_mechanics", tooling_only, "node", "apps/themuffinman/frontend/scripts/repository-ast-index.mjs")
run("generated_contracts", "product_contract", tooling_only, "node", "apps/themuffinman/frontend/scripts/generate-vision-contracts.mjs", "--check")
run("web_surface_contract", "product_contract", tooling_only, "node", "apps/themuffinman/frontend/scripts/validate-web-surface-contract.mjs")
run("admin_agent_scenarios", "product_contract", tooling_only, "node", "apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs")
run("modern_surface_contract", "product_contract", tooling_only, "node", "apps/themuffinman/frontend/scripts/validate-modern-surface-contract.mjs")
run("repository_map", "tool_mechanics", tooling_only, "ruby", "scripts/repository-map.rb", "--check")
query_output = run("repository_map_query", "tool_mechanics", tooling_only, "ruby", "scripts/repository-map.rb", "--query", "WorkspaceNavigationService", "--max-output", "20000")
query = JSON.parse(query_output)
abort "Repository map query leaked global AST" if query.key?("frontend_ast") || query.key?("graph")
abort "Repository map query missed known backend symbol" if query.dig("matches", "backend").empty?
abort "Repository map query exceeded configured output budget" if query_output.bytesize > 20_000
run("tool_catalog", "tool_mechanics", tooling_only, "ruby", "scripts/audits/audit-tool-catalog.rb", "--check")
run("runtime_tools", "tool_mechanics", tooling_only, "ruby", "scripts/audits/audit-runtime-tools.rb")
context_output = run("context_search", "tool_mechanics", tooling_only, "ruby", "scripts/context-search.rb", "--mode", "symbol", "--budget", "1024", "VisionConversationService")
abort "Context search did not report symbol mode" unless context_output.include?("mode=symbol")
abort "Context search exceeded configured output budget" if context_output.bytesize > 1024
run("change_validation", "tool_mechanics", tooling_only, "ruby", "scripts/change-validation.rb", "--check-fixtures")
run("template_freshness", "tool_mechanics", tooling_only, "ruby", "scripts/audits/audit-template-freshness.rb")
run("work_artifact_schema", "tool_mechanics", tooling_only, "ruby", "scripts/audits/audit-work-artifact-schema.rb")
run("tool_help", "tool_mechanics", tooling_only, "ruby", "scripts/tool-help.rb", "--check")

scope = tooling_only ? "tooling-only" : "all stages"
STAGE_RESULTS.each do |stage_id, result|
  puts "STAGE class=#{result.fetch(:stage_class)} id=#{stage_id} files=#{result.fetch(:details).length}"
  result.fetch(:details).each { |detail| puts "  DETAIL id=#{stage_id} #{detail}" } if verbose
end
puts "Tool self-test passed (#{scope}; Ruby/Node syntax, YAML, AST, contracts, repository map, catalog, and bounded search)."
