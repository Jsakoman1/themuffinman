#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)

def create_project(root, project_id, product, task_id)
  Dora::ProjectInitializer.initialize!(root, project_id: project_id, manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief_path = File.join(root, "docs/product-brief.yaml")
  brief = YAML.load_file(brief_path).merge("product" => product)
  File.write(brief_path, YAML.dump(brief))
  plan = {"kind" => "work", "version" => 1, "tasks" => [{"id" => task_id, "title" => "Record a note", "observable_outcome" => "A note is recorded.", "dependencies" => [], "required_paths" => ["docs/note.md"], "validation" => "true", "evidence_boundary" => ["note fixture"]}]}
  File.write(File.join(root, "docs/work/first.yaml"), YAML.dump(plan))
end

Dir.mktmpdir("dora-agent-context-command") do |sandbox|
  alpha = File.join(sandbox, "alpha")
  beta = File.join(sandbox, "beta")
  create_project(alpha, "alpha-project", "Alpha journal", "record-alpha")
  create_project(beta, "beta-project", "Beta journal", "record-beta")

  [[alpha, "record-alpha", "Alpha journal", "Beta journal"], [beta, "record-beta", "Beta journal", "Alpha journal"]].each do |root, task_id, expected_product, excluded_product|
    output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "agent-context", File.join(root, ".dora/project.yaml"), "docs/work/first.yaml", task_id)
    abort "agent-context command failed: #{output}" unless status.success?
    context = YAML.safe_load(output)
    abort "agent-context command lost selected task" unless context.dig("task", "id") == task_id
    abort "agent-context command crossed project knowledge" unless context.dig("knowledge", "product", "product") == expected_product && !output.include?(excluded_product)
    abort "agent-context command omitted citations" unless context.fetch("citations").map { |citation| citation.fetch("path") }.include?("docs/work/first.yaml")
  end
end

puts "Dora agent context command test passed (two projects receive bounded cited context without cross-project knowledge)."
