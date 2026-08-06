#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/compiled_feature_renderers"

database = {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true, "default" => nil, "default_confirmed" => true, "unique" => true, "unique_confirmed" => true, "index" => true, "index_confirmed" => true, "foreign_key" => nil, "foreign_key_confirmed" => true}
feature = {"kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply", "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true}, "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => database, "confirmed" => true}], "confirmed" => true}, "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true}, "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true}, "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true}, "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true}
sql = Dora::CompiledFeatureRenderers.render_flyway!(feature: feature, migration_version: "1")
abort "migration does not create the table" unless sql.include?("CREATE TABLE supply")
abort "migration lost declared unique/index behavior" unless sql.include?("UNIQUE") && sql.include?("CREATE INDEX idx_supply_name")
unsafe = Marshal.load(Marshal.dump(feature)); unsafe.dig("entity", "fields", 0, "database")["column"] = "name-drop"
begin
  Dora::CompiledFeatureRenderers.render_flyway!(feature: unsafe, migration_version: "1")
  abort "unsafe SQL column was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("SQL-safe")
end
puts "Dora compiled feature Flyway renderer test passed (explicit SQL mappings render append-only migration text and unsafe identifiers are rejected)."
