#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_control"

SCHEMA_PATH = File.expand_path("../project-control.schema.yaml", __dir__)
CONTROL_KEYS = %w[tool_catalog change_routing context_search workspace_inventory documentation_evidence system_map artifact_policy backlog].freeze

def create_project(root, prefix)
  control_root = File.join(root, ".dora/controls")
  FileUtils.mkdir_p(control_root)
  controls = CONTROL_KEYS.to_h do |key|
    relative = ".dora/controls/#{prefix}-#{key}.yaml"
    File.write(File.join(root, relative), "kind: #{key}\n")
    [key, relative]
  end
  control_path = File.join(root, ".dora/project-control.yaml")
  File.write(control_path, YAML.dump({"kind" => "dora_project_control", "version" => 1, "controls" => controls}))
  control_path
end

Dir.mktmpdir("dora-project-control") do |sandbox|
  alpha_root = File.join(sandbox, "alpha")
  beta_root = File.join(sandbox, "beta")
  alpha = Dora::ProjectControl.load!(create_project(alpha_root, "alpha"), schema_path: SCHEMA_PATH, project_root: alpha_root)
  beta = Dora::ProjectControl.load!(create_project(beta_root, "beta"), schema_path: SCHEMA_PATH, project_root: beta_root)
  abort "alpha control escaped its root" unless alpha.fetch("tool_catalog").start_with?("#{alpha_root}/")
  abort "beta control escaped its root" unless beta.fetch("system_map").start_with?("#{beta_root}/")
  capability = ".dora/controls/alpha-capability-inventory.yaml"
  File.write(File.join(alpha_root, capability), YAML.dump({"kind" => "dora_capability_inventory", "version" => 1, "component" => {"id" => "alpha", "owner" => "Owner"}, "capabilities" => []}))
  control = YAML.load_file(create_project(alpha_root, "alpha")); control.fetch("controls")["capability_inventory"] = capability; control_path = File.join(alpha_root, ".dora/project-control.yaml"); File.write(control_path, YAML.dump(control))
  abort "optional capability inventory was not resolved" unless Dora::ProjectControl.load!(control_path, schema_path: SCHEMA_PATH, project_root: alpha_root).key?("capability_inventory")
end

puts "Dora project control test passed (two isolated control bundles)."
