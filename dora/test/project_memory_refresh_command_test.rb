#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-memory-refresh-command") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "memory-refresh", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  memory = {"kind" => "dora_project_memory", "version" => 1, "project_intent" => {"product_brief" => "docs/product-brief.yaml", "domain_library" => "docs/domain-library.yaml"}, "canonical_knowledge" => [{"id" => "product", "path" => "docs/product-brief.yaml", "purpose" => "intent"}], "open_decisions" => [], "capability_intent" => [], "current_work" => {"plan" => "docs/work/missing.yaml", "task" => "first", "state" => "planned"}}
  path = File.join(root, "docs/project-memory.yaml"); File.write(path, YAML.dump(memory)); before = File.read(path)
  output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "memory-refresh", root)
  proposal = YAML.safe_load(output)
  abort "memory refresh was not read-only" unless status.success? && proposal.fetch("kind") == "dora_memory_refresh_proposal" && proposal.fetch("apply").include?("not available") && File.read(path) == before
end
puts "Dora memory refresh command test passed (reviewable proposal without memory mutation)."
