#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/change_impact"
require_relative "../lib/dora/plugin_runner"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
FIXTURE = File.join(ROOT, "test/fixtures/evidence-pipeline-project.yaml")

def command(root, *arguments)
  output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), *arguments, chdir: root)
  abort "evidence pipeline command failed: #{arguments.join(" ")}\n#{output}" unless status.success?
  YAML.safe_load(output)
end

Dir.mktmpdir("dora-evidence-pipeline-consumer") do |root|
  project = YAML.load_file(FIXTURE)
  Dora::ProjectInitializer.initialize!(root, project_id: project.fetch("project_id"), manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  FileUtils.mkdir_p(File.join(root, "plugins"))
  FileUtils.mkdir_p(File.join(root, "src"))
  File.write(File.join(root, "src/note.rb"), "class Note; end\n")
  File.write(File.join(root, "plugins/evidence.rb"), "puts 'evidence plugin ran'\n")
  brief_path = File.join(root, "docs/product-brief.yaml")
  File.write(brief_path, YAML.dump(YAML.load_file(brief_path).merge("product" => project.fetch("product"), "unanswered_decisions" => [project.fetch("open_decision")])))
  plan_path = "docs/work/first.yaml"
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => project.fetch("task_id"), "title" => "Record an evidence note", "observable_outcome" => "An evidence note is recorded.", "dependencies" => [], "required_paths" => ["src/note.rb"], "validation" => "true", "evidence_boundary" => ["evidence-plugin"]}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "items" => [{"id" => "first", "plan" => plan_path, "task" => project.fetch("task_id"), "status" => "pending"}]}
  impact_config = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "note", "path_prefixes" => ["src/"], "validations" => ["true"], "documentation" => ["docs/domain-library.yaml"], "runtime_evidence" => ["evidence-plugin"], "decisions" => ["DEC-EVIDENCE"]}]}
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => project.fetch("plugin_id"), "entrypoint" => "plugins/evidence.rb", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "fixture"}, "output" => {"kind" => "static-analysis-report", "path" => "docs/audit-output/evidence-plugin.json"}}]}
  File.write(File.join(root, plan_path), YAML.dump(plan))
  File.write(File.join(root, "docs/work/inventory.yaml"), YAML.dump(inventory))
  impact_path = File.join(root, ".dora/change-impact.yaml")
  File.write(impact_path, YAML.dump(impact_config))
  manifest_path = File.join(root, ".dora/plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))

  plugin = Dora::PluginRunner.run!(manifest_path, plugin_id: project.fetch("plugin_id"), project_root: root)
  impact = Dora::ChangeImpact.assess!(impact_path, ["src/note.rb"])
  status = command(root, "status", File.join(root, ".dora/project.yaml"), "docs/work/inventory.yaml")
  export = command(root, "findings-export", File.join(root, "docs/audit-output/evidence-plugin.json"))
  closeout = command(root, "agent-closeout", File.join(root, ".dora/project.yaml"), plan_path, project.fetch("task_id"), ".dora/change-impact.yaml", "src/note.rb")

  abort "pipeline plugin did not produce standard findings" unless plugin.dig("findings", 0, "kind") == "dora_finding"
  abort "pipeline impact lost declared evidence" unless impact.fetch("runtime_evidence") == ["evidence-plugin"]
  abort "pipeline status lost plugin finding" unless status.fetch("findings").length == 1 && status.fetch("open_decisions") == [project.fetch("open_decision")]
  abort "pipeline export is not read-only guidance" unless export.fetch("read_only") && export.fetch("diagnostic_boundary").include?("do not prove")
  abort "pipeline closeout made a completion claim" if closeout.dig("completion", "verified") || closeout.dig("completion", "status_mutation")
end

puts "Dora independent evidence pipeline consumer test passed (a fresh project traces diagnostic findings through impact, status, export, and closeout without completion claims)."
