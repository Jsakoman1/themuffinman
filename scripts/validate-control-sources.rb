#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

required_files = %w[
  docs/system-map.md
  docs/capability-inventory.yaml
  docs/target-capability-catalog.yaml
  docs/runtime-acceptance-matrix.yaml
  docs/agent-operating-model.yaml
  docs/implementation-control.md
  scripts/verify-work.rb
  scripts/audits/audit-atomic-task-hardening.rb
  scripts/context-search.rb
  scripts/tool-self-test.rb
  scripts/audits/audit-tool-catalog.rb
  scripts/audits/audit-runtime-tools.rb
  .dora/repository-map.yaml
  bin/dora
  apps/themuffinman/frontend/package.json
]

missing = required_files.reject { |path| File.file?(path) }
abort "Control source validation failed; missing: #{missing.join(', ')}" unless missing.empty?

yaml_files = Dir["docs/**/*.yaml"].sort
yaml_files.each { |path| YAML.load_file(path) }

inventory = YAML.load_file("docs/capability-inventory.yaml")
target_catalog = YAML.load_file("docs/target-capability-catalog.yaml")
abort "Capability inventory has no modules" unless Array(inventory["modules"]).any?
abort "Target capability catalog has no capabilities" unless Array(target_catalog["capabilities"]).any?

puts "Control sources valid (#{yaml_files.length} YAML files, capability and target inventories present)."
