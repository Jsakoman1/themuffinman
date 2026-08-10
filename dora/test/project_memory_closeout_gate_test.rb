#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/work_execution"

ROOT = File.expand_path("..", __dir__)

def git!(root, *arguments)
  abort "git command failed: #{arguments.join(" ")}" unless system("git", "-C", root, *arguments, out: File::NULL, err: File::NULL)
end

def write_closeout_project(root, current_work: {"state" => "none"})
  %w[.dora docs/work docs/audit-output docs/runtime-evidence].each { |path| FileUtils.mkdir_p(File.join(root, path)) }
  adapter = {"kind" => "dora_project_adapter", "version" => 1, "project" => {"id" => "closeout", "root" => ".."}, "paths" => {"docs" => "docs", "work_plans" => "docs/work", "audit_output" => "docs/audit-output", "runtime_evidence" => "docs/runtime-evidence"}, "commands" => {"work_start" => "true", "work_verify" => "true", "control_check" => "true"}, "extensions" => [{"id" => "docs", "category" => "documentation", "paths" => ["docs"], "invocation" => "fixture documentation"}]}
  File.write(File.join(root, ".dora/project.yaml"), YAML.dump(adapter))
  memory = {"kind" => "dora_project_memory", "version" => 1, "project_intent" => {"product_brief" => "docs/product-brief.yaml", "domain_library" => "docs/domain-library.yaml"}, "canonical_knowledge" => [{"id" => "product", "path" => "docs/product-brief.yaml", "purpose" => "Product intent."}], "open_decisions" => [], "capability_intent" => [], "current_work" => current_work}
  File.write(File.join(root, "docs/project-memory.yaml"), YAML.dump(memory))
  child = {"kind" => "work", "version" => 1, "id" => "delivery-work", "title" => "Verified delivery work", "status" => "verified", "baseline" => "pending", "tasks" => [{"id" => "verify-delivery", "title" => "Verify delivery", "status" => "done", "observable_outcome" => "A delivery is verified.", "dependencies" => [], "paths" => ["docs/result.md"], "required_paths" => ["docs/result.md"], "validation" => "ruby -e 'exit 0'"}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "id" => "delivery", "master_plan" => "docs/work/delivery-master.yaml", "state" => "active", "items" => [{"id" => "delivery-closeout", "order" => 1, "plan" => "docs/work/delivery.yaml", "task" => "verify-delivery", "status" => "verified", "verified_at" => "2026-08-10T06:11:11Z"}]}
  master = {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Verified delivery", "status" => "active", "baseline" => "pending", "strict_verification" => true, "serial_task_execution" => true, "execution_inventory" => "docs/work/delivery-inventory.yaml", "children" => ["docs/work/delivery.yaml"], "evidence" => []}
  File.write(File.join(root, "docs/work/delivery.yaml"), YAML.dump(child))
  File.write(File.join(root, "docs/work/delivery-inventory.yaml"), YAML.dump(inventory))
  File.write(File.join(root, "docs/work/delivery-master.yaml"), YAML.dump(master))
  git!(root, "init", "-q")
  git!(root, "config", "user.email", "test@example.invalid")
  git!(root, "config", "user.name", "Dora Test")
  git!(root, "add", ".")
  git!(root, "commit", "-qm", "baseline")
  baseline = `git -C #{root} rev-parse HEAD`.strip
  master["baseline"] = baseline
  File.write(File.join(root, "docs/work/delivery-master.yaml"), YAML.dump(master))
  File.join(root, ".dora/project.yaml")
end

Dir.mktmpdir("dora-project-memory-closeout-invalid") do |root|
  adapter_path = write_closeout_project(root, current_work: {"plan" => "docs/work/delivery.yaml", "task" => nil, "state" => "verified"})
  begin
    Dora::WorkExecution.run(adapter_path: adapter_path, schema_path: File.join(ROOT, "project-adapter.schema.yaml"), arguments: ["plan=docs/work/delivery-master.yaml"])
    abort "closeout accepted invalid task-less verified memory"
  rescue Dora::WorkExecution::Error => error
    abort "closeout did not identify project-memory validation" unless error.message.include?("project memory closeout gate failed")
  end
  master = YAML.load_file(File.join(root, "docs/work/delivery-master.yaml"))
  abort "invalid memory still allowed closeout success" if master.fetch("status") == "verified"
end

Dir.mktmpdir("dora-project-memory-closeout-normal") do |root|
  adapter_path = write_closeout_project(root)
  Dora::WorkExecution.run(adapter_path: adapter_path, schema_path: File.join(ROOT, "project-adapter.schema.yaml"), arguments: ["plan=docs/work/delivery-master.yaml"])
  memory = YAML.load_file(File.join(root, "docs/project-memory.yaml"))
  inventory = YAML.load_file(File.join(root, "docs/work/delivery-inventory.yaml"))
  master = YAML.load_file(File.join(root, "docs/work/delivery-master.yaml"))
  abort "normal closeout lost explicit idle memory" unless memory.dig("current_work") == {"state" => "none"}
  abort "normal closeout left the inventory active" unless inventory.fetch("state") == "verified"
  abort "normal closeout did not verify the master" unless master.fetch("status") == "verified"
end

puts "Dora project-memory closeout gate test passed (invalid terminal memory blocks closeout; verified delivery reconciles to explicit idle navigation)."
