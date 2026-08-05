#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
readme = File.read(File.join(ROOT, "README.md"))
guide = File.read(File.join(ROOT, "docs/operator-guide.md"))

abort "README does not link the operator guide" unless readme.include?("docs/operator-guide.md")
required = [
  "bootstrap my-app",
  "spring-vue-buildable",
  "./bin/dora doctor .dora/project.yaml",
  ".dora/project-commands.yaml",
  "plugin-run .dora/plugins.yaml",
  "docs/audit-output/",
  "Upgrade Dora safely",
  "does not silently upgrade an existing project",
  "What Dora owns and what the product retains",
  "product-retained"
]
missing = required.reject { |term| guide.include?(term) }
abort "operator guide is missing: #{missing.join(', ')}" unless missing.empty?
abort "operator guide promises product behavior" if guide.match?(/Dora (creates|implements) product/i)

puts "Dora operator guide test passed (bootstrap, starter, plugin, upgrade, and product-boundary guidance)."
