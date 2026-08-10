#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/work_verifier"

SCHEMA_PATH = File.expand_path("../project-adapter.schema.yaml", __dir__)

def create_project(root, id)
  %w[docs/work docs/audit-output docs/runtime-evidence .dora].each { |path| FileUtils.mkdir_p(File.join(root, path)) }
  adapter_path = File.join(root, ".dora", "project.yaml")
  adapter = {
    "kind" => "dora_project_adapter",
    "version" => 1,
    "project" => {"id" => id, "root" => ".."},
    "paths" => {"docs" => "docs", "work_plans" => "docs/work", "audit_output" => "docs/audit-output", "runtime_evidence" => "docs/runtime-evidence"},
    "commands" => {"work_start" => "tool start", "work_verify" => "tool verify", "control_check" => "tool check"},
    "extensions" => [{"id" => "local-audit", "category" => "audit", "paths" => ["docs"], "invocation" => "tool audit"}]
  }
  File.write(adapter_path, YAML.dump(adapter))
  plan = {
    "kind" => "work",
    "version" => 1,
    "id" => "#{id}-work",
    "status" => "draft",
    "baseline" => "1234567",
    "tasks" => [{"id" => "one", "title" => "One", "observable_outcome" => "One verified outcome", "dependencies" => [], "evidence_boundary" => ["test"], "paths" => ["src/item"], "required_paths" => ["src/item"], "validation" => "tool test"}]
  }
  File.write(File.join(root, "docs/work/example.yaml"), YAML.dump(plan))
  adapter_path
end

Dir.mktmpdir("dora-work-verifier") do |sandbox|
  alpha_adapter = create_project(File.join(sandbox, "alpha"), "alpha")
  beta_adapter = create_project(File.join(sandbox, "beta"), "beta")
  alpha = Dora::WorkVerifier.validate_plan!(alpha_adapter, "docs/work/example.yaml", SCHEMA_PATH)
  beta = Dora::WorkVerifier.validate_plan!(beta_adapter, "docs/work/example.yaml", SCHEMA_PATH)
  abort "alpha project was not validated" unless alpha == {"project" => "alpha", "kind" => "work", "id" => "alpha-work"}
  abort "beta project was not validated" unless beta == {"project" => "beta", "kind" => "work", "id" => "beta-work"}

  contract_path = File.join(File.dirname(alpha_adapter), "..", "docs/work/example.yaml")
  contract_plan = YAML.load_file(contract_path)
  contract_plan["title"] = "Contract work"
  contract_plan.fetch("tasks").first["implementation_contract"] = {"read_paths" => [], "write_paths" => ["src/item"], "permission_owner" => "service", "schema_paths" => [], "api_paths" => [], "ui_paths" => [], "test_paths" => [], "documentation_paths" => [], "runtime_evidence" => {"required" => false, "paths" => []}}
  File.write(contract_path, YAML.dump(contract_plan))
  Dora::WorkVerifier.validate_plan!(alpha_adapter, "docs/work/example.yaml", SCHEMA_PATH)
  contract_plan.fetch("tasks").first.fetch("implementation_contract").delete("permission_owner")
  File.write(contract_path, YAML.dump(contract_plan))
  begin
    Dora::WorkVerifier.validate_plan!(alpha_adapter, "docs/work/example.yaml", SCHEMA_PATH)
    abort "work verifier accepted a malformed declared implementation contract"
  rescue ArgumentError
    nil
  end

  begin
    Dora::WorkVerifier.validate_plan!(alpha_adapter, "../beta/docs/work/example.yaml", SCHEMA_PATH)
    abort "work verifier accepted a plan outside the adapter root"
  rescue ArgumentError
    # Expected: each adapter owns only its own declared project root.
  end
end

puts "Dora work verifier test passed (two independent adapter roots)."
