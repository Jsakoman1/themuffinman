#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/explain"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-explain") do |project|
  %w[docs .dora].each { |path| FileUtils.mkdir_p(File.join(project, path)) }
  FileUtils.cp(File.join(ROOT, "templates/product-brief.yaml"), File.join(project, "docs/product-brief.yaml"))
  FileUtils.cp(File.join(ROOT, "templates/domain-library.yaml"), File.join(project, "docs/domain-library.yaml"))
  FileUtils.cp(File.join(ROOT, "templates/agent-project-profile.yaml"), File.join(project, ".dora/agent-project-profile.yaml"))
  File.write(File.join(project, "AGENTS.md"), "Read declared sources.\n")
  explanation = Dora::Explain.project!(project)
  abort "project explanation lacks citations" unless explanation.fetch("citations").include?("docs/product-brief.yaml")
  abort "project explanation invented missing decisions" unless explanation.fetch("omissions").any?
end
package = {"kind" => "dora_capability_package", "version" => 1, "id" => "record-note", "title" => "Record note", "intent" => {"problem" => "Notes are lost.", "capability" => "Record note.", "source_reference" => "docs/idea.yaml"}, "domain" => {"entity_ids" => ["note"], "invariant_ids" => ["owner"], "permission_rule_ids" => ["record-note"], "workflow_id" => "note-lifecycle"}, "api" => {"service_owner" => "notes", "operations" => [{"id" => "record", "purpose" => "Record note."}]}, "tests" => {"scenarios" => [{"id" => "test", "action" => "Test.", "expected" => "Pass.", "status" => "recorded"}]}, "runtime" => {"scenarios" => [{"id" => "runtime", "action" => "Run.", "expected" => "Observe.", "status" => "unresolved"}]}, "work" => {"plan" => "docs/work/note.yaml", "task" => "implement-note"}, "unresolved" => [{"id" => "runtime", "reason" => "Runtime has not run."}]}
explanation = Dora::Explain.capability!(package)
abort "capability explanation lacks explicit omission" unless explanation.fetch("omissions") == ["Runtime has not run."]
abort "capability explanation made a completion claim" unless explanation.fetch("completion_boundary").include?("does not prove")
puts "Dora explain test passed (plain-language project and capability explanations preserve citations and omissions)."
