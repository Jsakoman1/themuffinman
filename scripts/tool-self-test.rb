#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "yaml"

ROOT = File.expand_path("..", __dir__)

def run(*command)
  stdout, stderr, status = Open3.capture3(*command, chdir: ROOT)
  abort "FAILED: #{command.join(" ")}\n#{stdout}\n#{stderr}" unless status.success?
  stdout
end

Dir[File.join(ROOT, "scripts/**/*.rb")].sort.each { |path| run("ruby", "-c", path) }
Dir[File.join(ROOT, "apps/themuffinman/frontend/scripts/*.mjs")].sort.each { |path| run("node", "--check", path) }
Dir[File.join(ROOT, "docs/**/*.yaml")].sort.each { |path| YAML.load_file(path) }

run("node", "apps/themuffinman/frontend/scripts/repository-ast-index.mjs")
run("node", "apps/themuffinman/frontend/scripts/generate-vision-contracts.mjs", "--check")
run("node", "apps/themuffinman/frontend/scripts/validate-web-surface-contract.mjs")
run("node", "apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs")
run("node", "apps/themuffinman/frontend/scripts/validate-modern-surface-contract.mjs")
run("ruby", "scripts/repository-map.rb", "--check")
run("ruby", "scripts/audits/audit-tool-catalog.rb", "--check")
run("ruby", "scripts/audits/audit-runtime-tools.rb")
run("ruby", "scripts/context-search.rb", "--budget", "1024", "VisionConversationService")

puts "Tool self-test passed (Ruby/Node syntax, YAML, AST, frontend validators, repository map, catalog, and bounded search)."
