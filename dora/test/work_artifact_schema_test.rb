#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/work_artifact_schema"

ROOT = File.expand_path("..", __dir__)
SCHEMA = File.join(ROOT, "templates/work-artifact-schema.yaml")

def write_fixture(root, name, value)
  path = File.join(root, name)
  File.write(path, YAML.dump(value))
  path
end

Dir.mktmpdir("dora-work-artifact-schema") do |sandbox|
  master = write_fixture(sandbox, "master.yaml", {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Delivery", "status" => "draft", "children" => ["docs/work/delivery.yaml"]})
  work = write_fixture(sandbox, "work.yaml", {"kind" => "work", "version" => 1, "id" => "delivery", "title" => "Delivery", "status" => "draft", "baseline" => "abcdef0", "tasks" => [{"id" => "task", "title" => "Task", "observable_outcome" => "Visible output", "dependencies" => [], "evidence_boundary" => ["test"], "paths" => ["lib/output.rb"], "required_paths" => ["lib/output.rb"], "validation" => "ruby test.rb"}]})
  inventory = write_fixture(sandbox, "inventory.yaml", {"kind" => "execution_inventory", "version" => 1, "id" => "delivery-items", "master_plan" => "docs/work/master.yaml", "items" => [{"id" => "item", "order" => 1, "plan" => "docs/work/delivery.yaml", "task" => "task", "status" => "pending"}]})
  [master, work, inventory].each { |path| Dora::WorkArtifactSchema.validate!(path, schema_path: SCHEMA) }
  contract_work = {"kind" => "work", "version" => 1, "id" => "contract", "title" => "Contract", "status" => "draft", "baseline" => "abcdef0", "tasks" => [{"id" => "api-task", "title" => "API task", "observable_outcome" => "API is declared.", "dependencies" => [], "evidence_boundary" => ["fixture"], "paths" => ["lib/api.rb"], "required_paths" => ["lib/api.rb"], "validation" => "ruby test.rb", "implementation_contract" => {"read_paths" => ["lib/read.rb"], "write_paths" => ["lib/api.rb"], "permission_owner" => "service", "schema_paths" => [], "api_paths" => ["docs/api.yaml"], "ui_paths" => [], "test_paths" => ["test/api_test.rb"], "documentation_paths" => ["docs/api.md"], "runtime_evidence" => {"required" => false, "paths" => []}}}]}
  contract = write_fixture(sandbox, "contract.yaml", contract_work)
  Dora::WorkArtifactSchema.validate!(contract, schema_path: SCHEMA)
  invalid_contract = Marshal.load(Marshal.dump(contract_work))
  invalid_contract.fetch("tasks").first.fetch("implementation_contract").delete("permission_owner")
  invalid_contract_path = write_fixture(sandbox, "invalid-contract.yaml", invalid_contract)
  begin
    Dora::WorkArtifactSchema.validate!(invalid_contract_path, schema_path: SCHEMA)
    abort "incomplete implementation contract passed"
  rescue ArgumentError
    nil
  end
  invalid = write_fixture(sandbox, "invalid.yaml", {"kind" => "execution_inventory", "version" => 1, "id" => "missing-items", "master_plan" => "docs/work/master.yaml", "items" => [{}]})
  begin
    Dora::WorkArtifactSchema.validate!(invalid, schema_path: SCHEMA)
    abort "incomplete execution inventory passed"
  rescue ArgumentError
    nil
  end
end

puts "Dora work-artifact schema test passed (portable master, work, and inventory fixtures)."
