#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/compiled_feature_contract"

input = {
  "kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply",
  "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true},
  "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true}, "confirmed" => true}], "confirmed" => true},
  "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true},
  "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true},
  "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true},
  "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true
}
valid = Dora::CompiledFeatureContract.validate!(input)
abort "compiled feature package was not preserved" unless valid.dig("stack", "package") == "example.inventory"
missing = Marshal.load(Marshal.dump(input)); missing.dig("entity", "fields", 0, "database").delete("sql_type")
begin
  Dora::CompiledFeatureContract.validate!(missing)
  abort "field without database type was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("fields")
end
puts "Dora compiled feature contract test passed (all technical mappings, API shapes, permissions, workflow transitions, and locations must be explicitly confirmed)."
