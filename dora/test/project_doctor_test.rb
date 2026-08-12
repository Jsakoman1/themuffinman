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

def complete_controls(root)
  controls = File.join(root, ".dora/controls")
  File.write(File.join(root, "docs/backlog.md"), "# Backlog\n")
  File.write(File.join(controls, "change-routing.yaml"), YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "commands" => ["test"]}]}))
  File.write(File.join(controls, "workspace-inventory.yaml"), YAML.dump({"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => "source", "path_prefixes" => ["src/"]}]}))
  File.write(File.join(controls, "documentation-evidence.yaml"), YAML.dump({"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "docs", "match" => "Backlog", "evidence" => ["docs/backlog.md"]}]}))
  File.write(File.join(controls, "system-map.yaml"), YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "source"}], "edges" => []}))
  File.write(File.join(controls, "backlog.yaml"), YAML.dump({"kind" => "dora_backlog", "version" => 1, "sources" => ["docs/backlog.md"]}))
end

def enable_work_artifact_audit(root)
  policy_path = File.join(root, ".dora/controls/artifact-policy.yaml")
  policy = YAML.load_file(policy_path)
  policy["work_artifact_audit"] = {"paths" => ["docs/work"]}
  File.write(policy_path, YAML.dump(policy))
end

def exclude_non_executable_record(root, path)
  policy_path = File.join(root, ".dora/controls/artifact-policy.yaml")
  policy = YAML.load_file(policy_path)
  policy.fetch("work_artifact_audit")["non_executable_records"] = [{"path" => path, "reason" => "Historical narrative record."}]
  File.write(policy_path, YAML.dump(policy))
end

Dir.mktmpdir("dora-doctor") do |sandbox|
  healthy_root = File.join(sandbox, "healthy")
  Dora::ProjectInitializer.initialize!(healthy_root, project_id: "healthy-project", manifest_path: INIT_MANIFEST)
  complete_controls(healthy_root)
  healthy_adapter = File.join(healthy_root, ".dora/project.yaml")
  healthy = report_for(healthy_adapter)
  abort "doctor did not accept a generated project" unless healthy.fetch("healthy")
  abort "new project did not report its scaffolded capability inventory" unless healthy.fetch("checks").any? { |check| check.fetch("id") == "control:capability_inventory" && check.fetch("status") == "passed" }

  adopted_control = YAML.load_file(File.join(healthy_root, ".dora/project-control.yaml"))
  adopted_control.fetch("controls")["capability_inventory"] = ".dora/controls/capability-inventory.yaml"
  File.write(File.join(healthy_root, ".dora/controls/capability-inventory.yaml"), YAML.dump({"kind" => "dora_capability_inventory", "version" => 1, "component" => {"id" => "healthy-project", "owner" => "Owner"}, "capabilities" => []}))
  File.write(File.join(healthy_root, ".dora/project-control.yaml"), YAML.dump(adopted_control))
  adopted = report_for(healthy_adapter)
  abort "doctor did not validate adopted capability controls" unless adopted.fetch("checks").any? { |check| check.fetch("id") == "control:capability_inventory" && check.fetch("status") == "passed" }

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
  abort "doctor rejected an absent declared generated output path" if failed.include?("path:audit_output")
  generated_output = unhealthy.fetch("checks").find { |check| check.fetch("id") == "path:audit_output" }
  abort "doctor did not identify the absent generated output path" unless generated_output && generated_output.fetch("status") == "passed" && generated_output.fetch("detail").include?("not materialized yet")
  abort "doctor did not report a missing executable" unless failed.include?("command:control_check")
  abort "doctor did not report incomplete generated controls" unless failed.include?("control:change_routing")

  adapter.fetch("paths")["docs"] = "docs/missing"
  File.write(unhealthy_adapter, YAML.dump(adapter))
  missing_canonical = report_for(unhealthy_adapter)
  missing_canonical_ids = missing_canonical.fetch("checks").select { |check| check.fetch("status") == "failed" }.map { |check| check.fetch("id") }
  abort "doctor accepted a missing non-generated canonical path" unless missing_canonical_ids.include?("path:docs")

  _output, unhealthy_status = Open3.capture2e(CLI, "doctor", unhealthy_adapter, chdir: ROOT)
  abort "doctor CLI accepted an unhealthy project" if unhealthy_status.success?

  incomplete_root = File.join(sandbox, "incomplete")
  Dora::ProjectInitializer.initialize!(incomplete_root, project_id: "incomplete-project", manifest_path: INIT_MANIFEST)
  incomplete_adapter = File.join(incomplete_root, ".dora/project.yaml")
  File.delete(File.join(incomplete_root, ".dora/controls/artifact-policy.yaml"))

  incomplete = report_for(incomplete_adapter)
  missing_policy = incomplete.fetch("checks").find { |check| check.fetch("id") == "work-artifact-audit" }
  abort "doctor did not diagnose a missing artifact policy" unless missing_policy && missing_policy.fetch("status") == "failed"
  abort "missing artifact policy diagnosis lacks remediation" unless missing_policy.fetch("detail").include?(".dora/controls/artifact-policy.yaml") && missing_policy.fetch("detail").include?("add")

  incomplete_output, incomplete_status = Open3.capture2e(CLI, "doctor", incomplete_adapter, chdir: ROOT)
  abort "doctor CLI accepted a project with a missing artifact policy" if incomplete_status.success?
  abort "doctor CLI emitted a stack trace for a missing artifact policy" if incomplete_output.include?("Errno::ENOENT") || incomplete_output.include?("project_doctor.rb:")
  abort "doctor CLI did not print the missing artifact policy diagnosis" unless incomplete_output.include?("FAILED work-artifact-audit: required artifact policy is missing: .dora/controls/artifact-policy.yaml")

  audited_root = File.join(sandbox, "audited")
  Dora::ProjectInitializer.initialize!(audited_root, project_id: "audited-project", manifest_path: INIT_MANIFEST)
  complete_controls(audited_root)
  enable_work_artifact_audit(audited_root)
  work_root = File.join(audited_root, "docs/work")
  FileUtils.mkdir_p(work_root)
  work_path = File.join(work_root, "verified-work.yaml")
  inventory_path = File.join(work_root, "inventory.yaml")
  master_path = File.join(work_root, "master.yaml")
  work = {"kind" => "work", "version" => 1, "id" => "verified-work", "title" => "Verified work", "status" => "verified", "baseline" => "abcdef0", "execution_inventory" => "docs/work/inventory.yaml", "tasks" => [{"id" => "task", "title" => "Task", "observable_outcome" => "Visible output", "dependencies" => [], "evidence_boundary" => ["test"], "paths" => ["lib/output.rb"], "required_paths" => ["lib/output.rb"], "validation" => "ruby test.rb"}]}
  master = {"kind" => "master", "version" => 1, "id" => "master", "title" => "Master", "status" => "verified", "children" => ["docs/work/verified-work.yaml"]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "id" => "items", "master_plan" => "docs/work/master.yaml", "state" => "active", "items" => [{"id" => "task", "order" => 1, "plan" => "docs/work/verified-work.yaml", "task" => "task", "status" => "verified"}]}
  File.write(work_path, YAML.dump(work))
  File.write(master_path, YAML.dump(master))
  File.write(inventory_path, YAML.dump(inventory))
  before = {work_path => File.binread(work_path), master_path => File.binread(master_path), inventory_path => File.binread(inventory_path)}

  audited = report_for(File.join(audited_root, ".dora/project.yaml"))
  advisory = audited.fetch("checks").find { |check| check.fetch("id") == "work-artifact:verified-work-active-inventory:docs/work/verified-work.yaml" }
  abort "doctor did not report the declared work-artifact contradiction" unless advisory
  abort "advisory provenance is incomplete" unless advisory.fetch("read_only") && advisory.fetch("disposition") == "advisory" && advisory.fetch("source_references") == ["docs/work/verified-work.yaml", "docs/work/inventory.yaml", "docs/work/master.yaml"] && !advisory.fetch("observed_at").empty?
  abort "advisory changed artifact state" unless before.all? { |path, content| File.binread(path) == content }
  abort "advisory made the project unhealthy" unless audited.fetch("healthy")

  historical_path = File.join(work_root, "historical-review.yaml")
  File.write(historical_path, "kind: [\n")
  exclude_non_executable_record(audited_root, "docs/work/historical-review.yaml")
  classified = report_for(File.join(audited_root, ".dora/project.yaml"))
  historical_advisory = classified.fetch("checks").find { |check| check.fetch("id").include?("historical-review.yaml") }
  abort "explicit non-executable historical record remained an advisory" if historical_advisory
  abort "historical record was changed by classification" unless File.binread(historical_path) == "kind: [\n"

  master["status"] = "active"
  File.write(master_path, YAML.dump(master))
  active_before = {work_path => File.binread(work_path), master_path => File.binread(master_path), inventory_path => File.binread(inventory_path)}
  active_master = report_for(File.join(audited_root, ".dora/project.yaml"))
  active_advisory = active_master.fetch("checks").find { |check| check.fetch("id") == "work-artifact:verified-work-active-inventory:docs/work/verified-work.yaml" }
  abort "doctor treated an active master inventory as contradictory" if active_advisory
  abort "active-master diagnostic changed artifact state" unless active_before.all? { |path, content| File.binread(path) == content }
end

puts "Dora project doctor test passed (healthy and unhealthy projects)."
