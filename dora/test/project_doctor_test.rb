#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_doctor"

ROOT = File.expand_path("..", __dir__)
ADAPTER_SCHEMA = File.join(ROOT, "project-adapter.schema.yaml")
CONTROL_SCHEMA = File.join(ROOT, "project-control.schema.yaml")
INIT_MANIFEST = File.join(ROOT, "templates/init-manifest.yaml")
CLI = File.join(ROOT, "bin/dora")

def report_for(adapter_path)
  Dora::ProjectDoctor.run(adapter_path, schema_path: ADAPTER_SCHEMA, control_schema_path: CONTROL_SCHEMA)
end

Dir.mktmpdir("dora-doctor") do |sandbox|
  healthy_root = File.join(sandbox, "healthy")
  Dora::ProjectInitializer.initialize!(healthy_root, project_id: "healthy-project", manifest_path: INIT_MANIFEST)
  healthy_adapter = File.join(healthy_root, ".dora/project.yaml")
  healthy = report_for(healthy_adapter)
  abort "doctor did not accept a generated project" unless healthy.fetch("healthy")

  _output, healthy_status = Open3.capture2e(CLI, "doctor", healthy_adapter, chdir: ROOT)
  abort "doctor CLI rejected a healthy project" unless healthy_status.success?

  unhealthy_root = File.join(sandbox, "unhealthy")
  Dora::ProjectInitializer.initialize!(unhealthy_root, project_id: "unhealthy-project", manifest_path: INIT_MANIFEST)
  unhealthy_adapter = File.join(unhealthy_root, ".dora/project.yaml")
  adapter = YAML.load_file(unhealthy_adapter)
  adapter.fetch("commands")["control_check"] = "dora-not-installed check"
  File.write(unhealthy_adapter, YAML.dump(adapter))
  FileUtils.rm_rf(File.join(unhealthy_root, "docs/audit-output"))

  unhealthy = report_for(unhealthy_adapter)
  failed = unhealthy.fetch("checks").select { |check| check.fetch("status") == "failed" }.map { |check| check.fetch("id") }
  abort "doctor did not report a missing path" unless failed.include?("path:audit_output")
  abort "doctor did not report a missing executable" unless failed.include?("command:control_check")

  _output, unhealthy_status = Open3.capture2e(CLI, "doctor", unhealthy_adapter, chdir: ROOT)
  abort "doctor CLI accepted an unhealthy project" if unhealthy_status.success?
end

puts "Dora project doctor test passed (healthy and unhealthy projects)."
