#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"

ROOT = File.expand_path("..", __dir__)

ROUTES = [
  { id: "backend", matches: ->(path) { path.start_with?("apps/themuffinman/src/main/") || path.start_with?("apps/themuffinman/src/test/") }, commands: ["make backend-test"], reason: "Spring backend source or test changed." },
  { id: "migration", matches: ->(path) { path.start_with?("apps/themuffinman/src/main/resources/db/migration/") }, commands: ["make backend-test", "make audit-data-workflow-impact"], reason: "Flyway migration changed; backend behavior and data-workflow links need review." },
  { id: "frontend", matches: ->(path) { path.start_with?("apps/themuffinman/frontend/src/") }, commands: ["make frontend-type-check", "make frontend-build"], reason: "Vue or TypeScript source changed." },
  { id: "frontend_contract", matches: ->(path) { path.start_with?("apps/themuffinman/frontend/scripts/") || path == "apps/themuffinman/frontend/package.json" }, commands: ["make frontend-type-check", "make frontend-build", "make tool-self-test"], reason: "Frontend validator or package contract changed." },
  { id: "migration_config", matches: ->(path) { path.match?(%r{\Aapps/themuffinman/src/main/resources/application.*\.properties\z}) || path.start_with?("apps/themuffinman/src/main/java/com/themuffinman/app/config/") }, commands: ["make backend-test", "make audit-configuration-environment-drift"], reason: "Operational configuration changed." },
  { id: "tooling", matches: ->(path) { path == "Makefile" || path.start_with?("scripts/") }, commands: ["make tool-self-test"], reason: "Repository tooling changed." },
  { id: "agent_templates", matches: ->(path) { path.start_with?(".agents/") }, commands: ["make audit-template-freshness", "make tool-self-test"], reason: "Agent template or local automation guidance changed." },
  { id: "intellij_run_configuration", matches: ->(path) { path.start_with?(".run/") }, commands: ["make audit-intellij-mcp-routing", "make tool-self-test"], reason: "Shared IntelliJ run configuration changed and its routing contract must remain healthy." },
  { id: "ci_workflow", matches: ->(path) { path.start_with?(".github/workflows/") }, commands: ["make backend-test", "make frontend-type-check", "make frontend-build", "make tool-self-test"], reason: "Repository CI workflow changed; each configured validation contract needs local coverage." },
  { id: "agent_safety", matches: ->(path) { path == "docs/agent-operating-model.yaml" || path.start_with?("docs/agent-operating-model/") }, commands: ["make audit-agent-safety", "make audit-docs"], reason: "Agent operating safety changed." },
  { id: "truth_registry", matches: ->(path) { path == "docs/system-truth-registry.yaml" || path == "docs/system-drift-control-registry.yaml" }, commands: ["make audit-truth-registry", "make audit-docs"], reason: "Canonical truth or drift-control registry changed." },
  { id: "documentation", matches: ->(path) { path.start_with?("docs/") }, commands: ["make audit-docs"], reason: "Documentation or work-plan source changed." }
].freeze

FIXTURES = {
  "backend" => { paths: ["apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java"], commands: ["make backend-test"] },
  "frontend" => { paths: ["apps/themuffinman/frontend/src/modules/app-shell/views/HomeHubView.vue"], commands: ["make frontend-type-check", "make frontend-build"] },
  "migration" => { paths: ["apps/themuffinman/src/main/resources/db/migration/V99__fixture.sql"], commands: ["make backend-test", "make audit-data-workflow-impact"] },
  "documentation_control" => { paths: ["docs/agent-operating-model.yaml"], commands: ["make audit-agent-safety", "make audit-docs"] },
  "tooling" => { paths: ["scripts/context-search.rb"], commands: ["make tool-self-test"] },
  "agent_templates" => { paths: [".agents/templates/docs/new-module.template.md"], commands: ["make audit-template-freshness", "make tool-self-test"] },
  "intellij_run_configuration" => { paths: [".run/TheMuffinMan Backend Tests.run.xml"], commands: ["make audit-intellij-mcp-routing", "make tool-self-test"] },
  "ci_workflow" => { paths: [".github/workflows/repository-validation.yml"], commands: ["make backend-test", "make frontend-type-check", "make frontend-build", "make tool-self-test"] },
  "mixed" => { paths: ["apps/themuffinman/frontend/src/router.ts", "scripts/context-search.rb", "docs/system-truth-registry.yaml"], commands: ["make frontend-type-check", "make frontend-build", "make tool-self-test", "make audit-truth-registry", "make audit-docs"] }
}.freeze

def changed_paths
  stdout, stderr, status = Open3.capture3("git", "diff", "--name-only", chdir: ROOT)
  abort(stderr) unless status.success?
  stdout.lines.map(&:strip).reject(&:empty?)
end

def route(paths)
  matched = ROUTES.select { |rule| paths.any? { |path| rule.fetch(:matches).call(path) } }
  { paths: paths.sort, classifications: matched.map { |rule| { id: rule.fetch(:id), reason: rule.fetch(:reason) } }, commands: matched.flat_map { |rule| rule.fetch(:commands) }.uniq }
end

def render(result)
  lines = ["Changed-path validation routing (advisory only)", ""]
  return (lines << "No changed paths supplied or found.").join("\n") if result.fetch(:paths).empty?
  lines << "Paths:"
  result.fetch(:paths).each { |path| lines << "- #{path}" }
  lines << "" << "Why:"
  result.fetch(:classifications).each { |entry| lines << "- #{entry.fetch(:id)}: #{entry.fetch(:reason)}" }
  lines << "" << "Recommended leaf validations:"
  result.fetch(:commands).each { |command| lines << "- #{command}" }
  lines.join("\n")
end

if ARGV == ["--check-fixtures"]
  FIXTURES.each do |id, fixture|
    actual = route(fixture.fetch(:paths)).fetch(:commands)
    abort "Fixture #{id} expected #{fixture.fetch(:commands).inspect}, got #{actual.inspect}" unless actual == fixture.fetch(:commands)
  end
  puts "Changed-path validation fixtures passed (#{FIXTURES.length} classes, advisory-only)."
  exit 0
end

paths = ARGV == ["--changed"] || ARGV.empty? ? changed_paths : ARGV
invalid = paths.reject { |path| path.match?(%r{\A(?:apps|docs|scripts|\.agents|\.run|\.github)/}) || path == "Makefile" || path == "AGENTS.md" }
abort "Paths must be repository-relative: #{invalid.join(", ")}" unless invalid.empty?
puts render(route(paths))
