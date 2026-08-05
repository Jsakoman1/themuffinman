#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/adapter"

SCHEMA_PATH = File.expand_path("../project-adapter.schema.yaml", __dir__)

def create_project(root, id, generated_output_paths)
  %w[docs/work docs/audit-output docs/runtime-evidence .dora].each { |path| FileUtils.mkdir_p(File.join(root, path)) }
  adapter = {
    "kind" => "dora_project_adapter",
    "version" => 1,
    "project" => {"id" => id, "root" => ".."},
    "paths" => {"docs" => "docs", "work_plans" => "docs/work", "audit_output" => "docs/audit-output", "runtime_evidence" => "docs/runtime-evidence"},
    "commands" => {"work_start" => "tool start", "work_verify" => "tool verify", "control_check" => "tool check"},
    "context" => {"generated_output_paths" => generated_output_paths},
    "extensions" => [{"id" => "local-audit", "category" => "audit", "paths" => ["docs"], "invocation" => "tool audit"}]
  }
  adapter_path = File.join(root, ".dora", "project.yaml")
  File.write(adapter_path, YAML.dump(adapter))
  adapter_path
end

Dir.mktmpdir("dora-project-context") do |sandbox|
  alpha_root = File.join(sandbox, "alpha")
  beta_root = File.join(sandbox, "beta")
  alpha = Dora::Adapter.load_context!(create_project(alpha_root, "alpha", ["audit_output", "runtime_evidence"]), SCHEMA_PATH)
  beta = Dora::Adapter.load_context!(create_project(beta_root, "beta", ["audit_output"]), SCHEMA_PATH)

  abort "alpha context lost its project identity" unless alpha.id == "alpha" && alpha.root == alpha_root
  abort "alpha docs root is incorrect" unless alpha.docs_root == File.join(alpha_root, "docs")
  abort "beta work-plan root is incorrect" unless beta.work_plans_root == File.join(beta_root, "docs/work")
  abort "generated output roots ignored adapter declaration" unless alpha.generated_output_roots == [File.join(alpha_root, "docs/audit-output"), File.join(alpha_root, "docs/runtime-evidence")] && beta.generated_output_roots == [File.join(beta_root, "docs/audit-output")]
  abort "declared command was not exposed" unless alpha.command("work_verify") == "tool verify"

  begin
    alpha.command("deploy")
    abort "context exposed an undeclared command"
  rescue ArgumentError
    # Expected: Dora core cannot call an undeclared project command.
  end

  begin
    alpha.send(:absolute_path, "src")
    abort "context exposed an undeclared project path"
  rescue ArgumentError
    # Expected: Dora core cannot reach an undeclared project root.
  end
end

puts "Dora project context test passed (two declared project contexts)."
