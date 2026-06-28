#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "fileutils"

REPO_ROOT = File.expand_path("..", __dir__)
SECTIONS_DIR = File.join(REPO_ROOT, "docs", "agent-operating-model", "sections")
OUTPUT_PATH = File.join(REPO_ROOT, "docs", "agent-operating-model.yaml")
SECTION_ORDER = %w[
  metadata
  source_of_truth
  backend_audit_coverage
  policies
  automation_read_models
  mutating_intent_contracts
  intent_safety_catalog
  documentation_coverage
  frontend_contract_generation
  frontend_contracts
  frontend_safety_regressions
  frontend_feature_expectations
  dead_path_tracker
  capability_registry
  intent_lineage
  prompt_drift_detection
  backend_contract_snapshots
  service_workflow_inventory
  permission_matrix
  state_transition_audit
  request_validation_gate
  documentation_sync
  enums
  api
  intents
].freeze

combined = {}

SECTION_ORDER.each do |section_name|
  path = File.join(SECTIONS_DIR, "#{section_name}.yaml")
  abort("Missing section file: #{path}") unless File.exist?(path)

  parsed = YAML.load_file(path) || {}
  abort("Section file must contain a hash keyed by #{section_name}: #{path}") unless parsed.is_a?(Hash)
  abort("Section file must only contain #{section_name}: #{path}") unless parsed.keys == [section_name]

  combined[section_name] = parsed.fetch(section_name)
end

FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
File.write(OUTPUT_PATH, YAML.dump(combined))
puts "Generated #{OUTPUT_PATH}"
