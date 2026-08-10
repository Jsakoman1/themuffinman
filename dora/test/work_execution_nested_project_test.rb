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

Dir.mktmpdir("dora-nested-work-execution") do |root|
  project = File.join(root, "dora")
  FileUtils.mkdir_p(File.join(project, ".dora"))
  %w[docs/work docs/audit-output docs/runtime-evidence].each { |path| FileUtils.mkdir_p(File.join(project, path)) }
  adapter = {"kind" => "dora_project_adapter", "version" => 1, "project" => {"id" => "nested", "root" => ".."}, "paths" => {"docs" => "docs", "work_plans" => "docs/work", "audit_output" => "docs/audit-output", "runtime_evidence" => "docs/runtime-evidence"}, "commands" => {"work_start" => "true", "work_verify" => "true", "control_check" => "true"}, "extensions" => [{"id" => "docs", "category" => "documentation", "paths" => ["docs"], "invocation" => "fixture documentation"}]}
  File.write(File.join(project, ".dora/project.yaml"), YAML.dump(adapter))
  plan_path = "docs/work/nested.yaml"
  inventory_path = "docs/work/nested-inventory.yaml"
  inventory = {"kind" => "execution_inventory", "version" => 1, "id" => "nested", "master_plan" => plan_path, "items" => [{"id" => "nested-change", "plan" => plan_path, "task" => "record-change", "status" => "pending"}]}
  plan = {"kind" => "work", "version" => 1, "id" => "nested", "title" => "Nested project", "status" => "draft", "baseline" => "pending", "strict_verification" => true, "serial_task_execution" => true, "execution_inventory" => inventory_path, "tasks" => [{"id" => "record-change", "title" => "Record nested change", "type" => "docs-only", "status" => "pending", "inventory_item" => "nested-change", "observable_outcome" => "A nested plan verifies a project-relative file.", "dependencies" => [], "evidence_boundary" => ["nested path normalization"], "paths" => ["docs/changed.md"], "required_paths" => ["docs/changed.md"], "validation" => "ruby -e 'exit 0'"}], "evidence" => []}
  File.write(File.join(project, inventory_path), YAML.dump(inventory))
  File.write(File.join(project, plan_path), YAML.dump(plan))
  git!(root, "init", "-q")
  git!(root, "config", "user.email", "test@example.invalid")
  git!(root, "config", "user.name", "Dora Test")
  git!(root, "add", ".")
  git!(root, "commit", "-qm", "baseline")
  worktree_root = `git -C #{project} rev-parse --show-toplevel`.strip
  abort "nested fixture Git root is wrong: #{worktree_root}" unless File.realpath(worktree_root) == File.realpath(root)
  baseline = `git -C #{root} rev-parse HEAD`.strip
  plan["baseline"] = baseline
  File.write(File.join(project, plan_path), YAML.dump(plan))

  adapter_path = File.join(project, ".dora/project.yaml")
  schema_path = File.join(ROOT, "project-adapter.schema.yaml")
  Dora::WorkExecution.run(adapter_path: adapter_path, schema_path: schema_path, arguments: ["action=start", "plan=#{plan_path}", "task=record-change"])
  File.write(File.join(project, "docs/changed.md"), "nested change\n")
  execution = Dora::WorkExecution.new(adapter_path: adapter_path, schema_path: schema_path, arguments: [])
  visible_paths = execution.send(:changed_files, baseline)
  abort "nested path was not visible from #{execution.instance_variable_get(:@context).root}: #{visible_paths.inspect}" unless visible_paths.include?("docs/changed.md")
  Dora::WorkExecution.run(adapter_path: adapter_path, schema_path: schema_path, arguments: ["plan=#{plan_path}", "task=record-change"])

  evidence = YAML.load_file(File.join(project, plan_path)).fetch("evidence").first
  abort "nested path was not normalized" unless evidence.fetch("changedFiles") == ["docs/changed.md"]
end

puts "Dora nested project work execution test passed (Git paths normalize to the declared project root)."
