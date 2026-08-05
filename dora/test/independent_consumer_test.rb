#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)

def run!(project_root, *command)
  output, status = Open3.capture2e(*command, chdir: project_root)
  abort "command failed: #{command.join(" ")}: #{output}" unless status.success?
  output
end

def control_source(root, name, document)
  path = File.join(root, "#{name}.yaml")
  File.write(path, YAML.dump(document))
  path
end

Dir.mktmpdir("dora-independent-consumer") do |sandbox|
  project_root = File.join(sandbox, "consumer")
  FileUtils.mkdir_p(project_root)
  FileUtils.cp_r(ROOT, File.join(project_root, "dora"))
  commands = control_source(sandbox, "commands", {"kind" => "dora_project_commands", "version" => 1, "commands" => {"setup" => "true", "test" => "true", "build" => "true"}})
  run!(project_root, "./dora/bin/dora", "init", ".", "--project", "independent-consumer", "--ci", "github-actions", "--commands", commands)
  File.write(File.join(project_root, "docs/backlog.md"), "# Backlog\n")
  controls = {
    "change_routing" => {"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "docs", "path_prefixes" => ["docs/"], "commands" => ["true"]}]},
    "context_search" => {"kind" => "dora_context_search", "version" => 1, "roots" => ["docs"], "exclusions" => []},
    "workspace_inventory" => {"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => "docs", "path_prefixes" => ["docs/"]}]},
    "documentation_evidence" => {"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "backlog", "match" => "Backlog", "evidence" => ["docs/backlog.md"]}]},
    "system_map" => {"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "docs"}], "edges" => []},
    "backlog" => {"kind" => "dora_backlog", "version" => 1, "sources" => ["docs/backlog.md"]}
  }
  controls.each do |id, document|
    source = control_source(sandbox, id, document)
    run!(project_root, "./bin/dora", "configure", ".dora/project.yaml", "--control", id, "--from", source)
  end
  doctor = run!(project_root, "./bin/dora", "doctor", ".dora/project.yaml")
  abort "independent consumer doctor is not healthy" unless doctor.include?("PASSED control:backlog")
  run!(project_root, "git", "init", "-q")
  run!(project_root, "git", "add", ".")
  run!(project_root, "git", "-c", "user.name=Dora", "-c", "user.email=dora@example.test", "commit", "-qm", "baseline")
  baseline = run!(project_root, "git", "rev-parse", "HEAD").strip
  File.write(File.join(project_root, "docs/work/example.yaml"), YAML.dump({"kind" => "work", "version" => 1, "id" => "example", "title" => "Example", "status" => "draft", "baseline" => baseline, "strict_verification" => true, "serial_task_execution" => true, "execution_inventory" => "docs/work/inventory.yaml", "tasks" => [{"id" => "write-result", "title" => "Write result", "type" => "implementation", "status" => "pending", "inventory_item" => "result", "observable_outcome" => "A result file exists.", "dependencies" => [], "evidence_boundary" => ["result file test"], "paths" => ["docs/result.txt"], "required_paths" => ["docs/result.txt"], "validation" => "ruby -e 'abort unless File.file?(\"docs/result.txt\")'"}]}))
  File.write(File.join(project_root, "docs/work/inventory.yaml"), YAML.dump({"kind" => "execution_inventory", "version" => 1, "id" => "example-inventory", "master_plan" => "docs/work/example.yaml", "items" => [{"id" => "result", "order" => 1, "plan" => "docs/work/example.yaml", "task" => "write-result", "status" => "pending"}]}))
  run!(project_root, "./bin/dora", "work-start", ".dora/project.yaml", "plan=docs/work/example.yaml", "task=write-result")
  File.write(File.join(project_root, "docs/result.txt"), "done\n")
  run!(project_root, "./bin/dora", "work-verify", ".dora/project.yaml", "plan=docs/work/example.yaml", "task=write-result")
  workflow = File.read(File.join(project_root, ".github/workflows/dora-control.yml"))
  abort "independent consumer CI is not local" unless workflow.include?("./bin/dora doctor")
  content = Dir[File.join(project_root, "{bin,.dora,docs,.github}/**/*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "independent consumer refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent consumer test passed (local launcher, configured controls, work verification, and CI)."
