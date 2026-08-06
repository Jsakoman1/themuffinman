#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/compiled_feature_contract"

database = {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true, "default" => nil, "default_confirmed" => true, "unique" => false, "unique_confirmed" => true, "index" => true, "index_confirmed" => true, "foreign_key" => nil, "foreign_key_confirmed" => true}
input = {"kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply", "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true}, "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => database, "confirmed" => true}], "confirmed" => true}, "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true}, "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true}, "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true}, "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true}
abort "explicit JDBC mapping was rejected" unless Dora::CompiledFeatureContract.validate_type_mappings!(input).dig("entity", "fields", 0, "database", "index") == true
unsupported = Marshal.load(Marshal.dump(input)); unsupported.dig("entity", "fields", 0, "database")["sql_type"] = "jsonb"
begin
  Dora::CompiledFeatureContract.validate_type_mappings!(unsupported)
  abort "unsupported SQL type was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("unsupported")
end
missing = Marshal.load(Marshal.dump(input)); missing.dig("entity", "fields", 0, "database").delete("foreign_key_confirmed")
begin
  Dora::CompiledFeatureContract.validate_type_mappings!(missing)
  abort "undeclared foreign key control was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("foreign_key")
end
puts "Dora compiled feature type map test passed (supported JDBC/PostgreSQL mappings and every constraint control must be explicit)."
