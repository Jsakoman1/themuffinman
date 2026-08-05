#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("../..", __dir__)
MASTER_PATH = "docs/work/dora-v08-agent-first-operating-system-master.yaml"
INVENTORY_PATH = "docs/work/dora-v08-agent-first-operating-system-execution-inventory.yaml"
PLAN_PATHS = %w[
  docs/work/dora-v08-agent-first-operating-system-atomic-hardening.yaml
  docs/work/dora-v08-agent-first-operating-system-01-source-engines.yaml
  docs/work/dora-v08-agent-first-operating-system-02-agent-knowledge.yaml
  docs/work/dora-v08-agent-first-operating-system-03-blueprints-consumer.yaml
  docs/work/dora-v08-agent-first-operating-system-04-release.yaml
].freeze
EXPECTED_IDS = %w[
  dora-v08-harden dora-v08-builtin-runner dora-v08-java-ast dora-v08-ts-vue-ast
  dora-v08-architecture-plugin dora-v08-http-plugin dora-v08-spring-plugin dora-v08-vue-plugin
  dora-v08-retire-wrappers dora-v08-product-brief dora-v08-domain-library dora-v08-agent-profile
  dora-v08-bootstrap-knowledge dora-v08-knowledge-audit dora-v08-architecture-blueprint
  dora-v08-voice-blueprint dora-v08-blueprint-tests dora-v08-agent-consumer dora-v08-product-boundary
  dora-v08-operator-guide dora-v08-release muffinman-v08-pin
].freeze
WRAPPERS = %w[
  audit-api-contract-drift.rb audit-canonical-source-integrity.rb audit-configuration-environment-drift.rb
  audit-endpoint-callsite-linker.rb audit-frontend-interaction-contract.rb audit-frontend-route-surfaces.rb
  audit-frontend-stale-surfaces.rb audit-mapper-usage.rb audit-module-dependency-direction.rb
  audit-read-surface-inventory.rb audit-ui-entrypoints.rb
].freeze

master = YAML.load_file(File.join(ROOT, MASTER_PATH))
inventory = YAML.load_file(File.join(ROOT, INVENTORY_PATH))
plans = PLAN_PATHS.to_h { |plan_path| [plan_path, YAML.load_file(File.join(ROOT, plan_path))] }
failures = []

failures << "master kind or id is invalid" unless master["kind"] == "master" && master["id"] == "dora-v08-agent-first-operating-system-master"
failures << "master does not use the v0.8 inventory" unless master["execution_inventory"] == INVENTORY_PATH
failures << "master children differ from v0.8 plans" unless Array(master["children"]).sort == PLAN_PATHS.sort
failures << "master does not name this hardening verifier" unless master.dig("hardening_review", "verifier") == "ruby scripts/audits/audit-dora-v08-agent-first-plan.rb"
failures << "master has no agent-proof boundary" unless master.dig("hardening_review", "agent_proof_gate").to_s.include?("language-model response")
failures << "master has no voice safety boundary" unless master.dig("hardening_review", "voice_gate").to_s.include?("credential")

items = Array(inventory["items"])
failures << "inventory IDs differ from v0.8 contract" unless items.map { |item| item["id"] } == EXPECTED_IDS
failures << "inventory order is not contiguous" unless items.map { |item| item["order"] } == (1..EXPECTED_IDS.length).to_a
failures << "inventory verifier is missing" unless inventory.dig("hardening_contract", "verifier") == "ruby scripts/audits/audit-dora-v08-agent-first-plan.rb"

tasks_by_item = {}
plans.each do |plan_path, plan|
  failures << "plan does not use the global inventory: #{plan_path}" unless plan["execution_inventory"] == INVENTORY_PATH
  failures << "plan does not identify the v0.8 master: #{plan_path}" unless plan["master_plan"] == MASTER_PATH
  failures << "plan has no execution-readiness queue: #{plan_path}" unless plan.dig("execution_readiness", "queue_authority") == INVENTORY_PATH
  Array(plan["tasks"]).each do |task|
    item_id = task["inventory_item"]
    failures << "duplicate inventory task: #{item_id}" if tasks_by_item.key?(item_id)
    tasks_by_item[item_id] = [plan_path, task]
  end
end

items.each_with_index do |item, index|
  pair = tasks_by_item[item["id"]]
  unless pair
    failures << "inventory item has no task: #{item["id"]}"
    next
  end
  plan_path, task = pair
  failures << "inventory mapping differs for #{item["id"]}" unless item["plan"] == plan_path && item["task"] == task["id"]
  required = Array(task["required_paths"])
  failures << "task paths differ from exact required paths: #{task["id"]}" unless Array(task["paths"]) == required
  failures << "task lacks required paths: #{task["id"]}" if required.empty?
  failures << "task lacks observable outcome: #{task["id"]}" if task["observable_outcome"].to_s.empty?
  failures << "task lacks evidence boundary: #{task["id"]}" if Array(task["evidence_boundary"]).empty?
  failures << "task validation is recursive: #{task["id"]}" if task["validation"].to_s.match?(/work-verify|verify-work/)
  expected_dependencies = index.zero? ? [] : [items[index - 1]["id"]]
  failures << "task dependency is not directly serial: #{task["id"]}" unless Array(task["dependencies"]) == expected_dependencies
end

java_task = tasks_by_item.fetch("dora-v08-java-ast")[1]
failures << "Java AST task does not retire the MuffinMan source" unless Array(java_task["required_paths"]).include?("scripts/RepositoryJavaAstIndex.java")
typescript_task = tasks_by_item.fetch("dora-v08-ts-vue-ast")[1]
failures << "TypeScript/Vue AST task does not retire the MuffinMan source" unless Array(typescript_task["required_paths"]).include?("apps/themuffinman/frontend/scripts/repository-ast-index.mjs")
wrapper_task = tasks_by_item.fetch("dora-v08-retire-wrappers")[1]
missing_wrappers = WRAPPERS.reject { |name| Array(wrapper_task["required_paths"]).include?("scripts/audits/#{name}") }
failures << "wrapper retirement scope is incomplete: #{missing_wrappers.join(', ')}" unless missing_wrappers.empty?
voice_task = tasks_by_item.fetch("dora-v08-voice-blueprint")[1]
failures << "voice task does not use a neutral blueprint schema" unless Array(voice_task["required_paths"]).include?("dora/voice-capability-blueprint.schema.yaml")
%w[dora-v08-release muffinman-v08-pin].each do |item_id|
  failures << "release task is missing external approval gate: #{item_id}" unless tasks_by_item.fetch(item_id)[1]["requires_external_approval"] == true
end

abort "Dora v0.8 agent-first plan audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora v0.8 agent-first plan audit passed (#{items.length} exact serial tasks; physical extraction, agent, voice, and approval gates are explicit)."
