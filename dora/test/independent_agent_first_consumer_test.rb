#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
IDEA = File.join(ROOT, "test/fixtures/agent-first-project-idea.yaml")

def run!(root, *command)
  output, status = Open3.capture2e(*command, chdir: root)
  abort "agent-first consumer command failed: #{command.join(' ')}\n#{output}" unless status.success?
  output
end

Dir.mktmpdir("dora-agent-first-consumer") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  descriptor = File.join(sandbox, "source.yaml")
  File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => source, "ref" => "8" * 40}}))
  project = File.join(sandbox, "shared-note-board")
  run!(sandbox, File.join(source, "bin/dora"), "bootstrap", project, "--project", "shared-note-board", "--source", descriptor, "--starter", "spring-vue-buildable")

  idea = YAML.load_file(IDEA)
  brief = YAML.load_file(File.join(project, "docs/product-brief.yaml"))
  brief["product"] = idea.fetch("product")
  brief["user_problem"] = idea.fetch("user_problem")
  brief["primary_users"] = idea.fetch("primary_users")
  brief["intended_outcomes"] = ["A member can find a shared decision."]
  brief["non_goals"] = [idea.fetch("non_goal")]
  brief["assumptions"] = ["Members consent to sharing notes."]
  brief["risks"] = ["The group may not form a note-writing habit."]
  brief["unanswered_decisions"] = ["Who can archive a shared note?"]
  File.write(File.join(project, "docs/product-brief.yaml"), YAML.dump(brief))

  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "source-boundary", "builtin" => "architecture-integrity", "source_roots" => [{"id" => "backend", "path" => "backend"}], "inputs" => {"paths" => ["backend"], "rules" => []}, "output" => {"kind" => "static-analysis-report", "path" => "docs/audit-output/source-boundary.json"}}]}
  File.write(File.join(project, ".dora/plugins.yaml"), YAML.dump(manifest))
  run!(project, "./bin/dora", "doctor", ".dora/project.yaml")
  run!(project, "./bin/dora", "plugin-run", ".dora/plugins.yaml", "source-boundary")
  abort "built-in analysis report is missing" unless File.file?(File.join(project, "docs/audit-output/source-boundary.json"))

  run!(project, "git", "init")
  run!(project, "git", "config", "user.email", "fixture@example.invalid")
  run!(project, "git", "config", "user.name", "Dora Fixture")
  run!(project, "git", "add", ".")
  run!(project, "git", "commit", "-m", "bootstrap agent-first fixture")
  baseline = run!(project, "git", "rev-parse", "HEAD").strip
  inventory = {"kind" => "execution_inventory", "version" => 1, "master_plan" => "docs/work/fixture-master.yaml", "items" => [{"id" => "first-note", "plan" => "docs/work/first-note.yaml", "task" => "write-first-note", "status" => "pending"}]}
  File.write(File.join(project, "docs/work/inventory.yaml"), YAML.dump(inventory))
  plan = {"kind" => "work", "version" => 1, "id" => "first-note", "baseline" => baseline, "strict_verification" => true, "serial_task_execution" => true, "execution_inventory" => "docs/work/inventory.yaml", "tasks" => [{"id" => "write-first-note", "inventory_item" => "first-note", "type" => "implementation", "observable_outcome" => "The first capability note is recorded.", "paths" => ["docs/first-capability.md"], "required_paths" => ["docs/first-capability.md"], "validation" => "ruby -e 'abort unless File.file?(\"docs/first-capability.md\")'"}]}
  File.write(File.join(project, "docs/work/first-note.yaml"), YAML.dump(plan))
  run!(project, "./bin/dora", "work-start", ".dora/project.yaml", "plan=docs/work/first-note.yaml", "task=write-first-note")
  File.write(File.join(project, "docs/first-capability.md"), "# Shared note\n\nCreate and read a shared note.\n")
  run!(project, "./bin/dora", "work-verify", ".dora/project.yaml", "plan=docs/work/first-note.yaml", "task=write-first-note")
  verified = YAML.load_file(File.join(project, "docs/work/first-note.yaml"))
  abort "atomic plan was not verified" unless verified["status"] == "verified"

  content = Dir[File.join(project, "{.dora,backend,bin,docs,frontend}/**/*")].select { |path| File.file?(path) }.map { |path| File.binread(path).force_encoding(Encoding::UTF_8).scrub }.join("\n")
  abort "independent agent-first consumer refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent agent-first consumer test passed (idea, bootstrap, knowledge, analysis, atomic plan, and verified evidence)."
