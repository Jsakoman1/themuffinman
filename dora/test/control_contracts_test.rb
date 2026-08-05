#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_doctor"

ROOT = File.expand_path("..", __dir__)
ADAPTER_SCHEMA = File.join(ROOT, "project-adapter.schema.yaml")
CONTROL_SCHEMA = File.join(ROOT, "project-control.schema.yaml")
MANIFEST = File.join(ROOT, "templates/init-manifest.yaml")

Dir.mktmpdir("dora-control-contracts") do |sandbox|
  root = File.join(sandbox, "project")
  Dora::ProjectInitializer.initialize!(root, project_id: "contract-project", manifest_path: MANIFEST)
  report = Dora::ProjectDoctor.run(File.join(root, ".dora/project.yaml"), schema_path: ADAPTER_SCHEMA, control_schema_path: CONTROL_SCHEMA)
  failed = report.fetch("checks").select { |check| check.fetch("status") == "failed" }.map { |check| check.fetch("id") }
  abort "doctor accepted an incomplete control bundle" unless failed.include?("control:change_routing") && failed.include?("control:backlog")
end

puts "Dora control-contract test passed (incomplete generated controls are explicit)."
