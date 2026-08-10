#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../../lib/dora/handoff"

ROOT = File.expand_path("../..", __dir__)
RESOLVER = File.join(ROOT, "bridge/bin/dora-bridge-codex-both-contract")

def write_registry(root)
  path = File.join(root, "projects.yaml")
  File.write(path, YAML.dump({"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "dora", "adapter_path" => "unused/.dora/project.yaml", "capabilities" => {"handoff_write" => true}}, {"id" => "doomsday-storage", "adapter_path" => "unused/.dora/project.yaml", "capabilities" => {"handoff_write" => true}}, {"id" => "read-only", "adapter_path" => "unused/.dora/project.yaml"}]}))
  path
end

def resolve(registry, state_root, *projects)
  output, status = Open3.capture2e(RbConfig.ruby, RESOLVER, registry, state_root, *projects)
  [JSON.parse(output), status]
end

Dir.mktmpdir("dora-codex-both-contract") do |root|
  registry = write_registry(root)
  state_root = File.join(root, "private-state")
  payload, status = resolve(registry, state_root, "dora", "doomsday-storage")
  abort "no-ready resolver failed" unless status.success? && payload == {"status" => "no_ready_handoff"}
  clock = Time.utc(2026, 8, 10, 12, 0, 0)
  store = Dora::Handoff.new(state_root: state_root, now: -> { clock })
  later = store.create!(project: "doomsday-storage", title: "Later", objective: "Later work.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "later")
  clock += 1
  earlier = store.create!(project: "dora", title: "Earlier", objective: "Earlier work.", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "earlier")
  payload, status = resolve(registry, state_root, "dora", "doomsday-storage")
  abort "resolver did not choose deterministic ID" unless status.success? && payload == {"status" => "ready_handoff", "project" => later.fetch("project"), "handoff_id" => later.fetch("id")} && payload.fetch("handoff_id") != earlier.fetch("id")
  output, denied = Open3.capture2e(RbConfig.ruby, RESOLVER, registry, state_root, "read-only")
  abort "read-only project was accepted by owner resolver" if denied.success? || output.include?(state_root)
end

puts "Dora codex-both contract test passed (no-ready normal path, deterministic pending selection, and project capability gate)."
