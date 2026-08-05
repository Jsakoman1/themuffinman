#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/work_execution"

SCHEMA_PATH = File.expand_path("../project-adapter.schema.yaml", __dir__)
FIXTURE_ROOT = File.expand_path("../fixtures/standalone-project", __dir__)

def run!(*command, chdir:)
  _stdout, stderr, status = Open3.capture3(*command, chdir: chdir)
  abort "command failed: #{command.join(" ")}\n#{stderr}" unless status.success?
end

Dir.mktmpdir("dora-work-execution") do |sandbox|
  project_root = File.join(sandbox, "project")
  FileUtils.cp_r(FIXTURE_ROOT, project_root)
  FileUtils.mkdir_p(File.join(project_root, "docs/audit-output"))
  FileUtils.mkdir_p(File.join(project_root, "docs/runtime-evidence"))
  run!("git", "init", "-q", chdir: project_root)
  run!("git", "config", "user.email", "dora@example.test", chdir: project_root)
  run!("git", "config", "user.name", "Dora Test", chdir: project_root)
  run!("git", "add", ".", chdir: project_root)
  run!("git", "commit", "-qm", "baseline", chdir: project_root)
  baseline, = Open3.capture2("git", "rev-parse", "--short", "HEAD", chdir: project_root)

  plan_path = File.join(project_root, "docs/work/execution.yaml")
  plan = YAML.load_file(plan_path)
  plan["strict_verification"] = true
  plan["serial_task_execution"] = true
  plan["execution_inventory"] = "docs/work/inventory.yaml"
  plan["tasks"].first["inventory_item"] = "fixture-execution"
  File.write(plan_path, YAML.dump(plan))
  inventory = {"kind" => "execution_inventory", "version" => 1, "id" => "fixture-inventory", "master_plan" => "docs/work/master.yaml", "items" => [{"id" => "fixture-execution", "order" => 1, "plan" => "docs/work/execution.yaml", "task" => "verify-local-contract", "status" => "pending"}]}
  File.write(File.join(project_root, "docs/work/inventory.yaml"), YAML.dump(inventory))
  File.write(plan_path, YAML.dump(plan.merge("baseline" => baseline.strip)))
  run!("git", "add", ".", chdir: project_root)
  run!("git", "commit", "-qm", "execution-plan", chdir: project_root)

  adapter_path = File.join(project_root, ".dora/project.yaml")
  started = Dora::WorkExecution.run(adapter_path: adapter_path, schema_path: SCHEMA_PATH, arguments: ["action=start", "plan=docs/work/execution.yaml", "task=verify-local-contract"])
  abort "serial task did not start" unless started.include?("Work task started")
  File.write(File.join(project_root, "docs/product.md"), "# Updated standalone fixture\n")
  verified = Dora::WorkExecution.run(adapter_path: adapter_path, schema_path: SCHEMA_PATH, arguments: ["plan=docs/work/execution.yaml", "task=verify-local-contract"])
  abort "serial task did not verify" unless verified == "Work verified: docs/work/execution.yaml"

  result = YAML.load_file(plan_path)
  abort "execution plan did not become verified" unless result["status"] == "verified" && result.dig("tasks", 0, "status") == "done"
  abort "execution evidence is missing" unless result.fetch("evidence").last.fetch("result") == "passed"
end

puts "Dora work execution test passed (standalone serial fixture)."
