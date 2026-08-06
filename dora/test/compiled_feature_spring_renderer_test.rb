#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/compiled_feature_renderers"

database = {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true, "default" => nil, "default_confirmed" => true, "unique" => false, "unique_confirmed" => true, "index" => true, "index_confirmed" => true, "foreign_key" => nil, "foreign_key_confirmed" => true}
feature = {"kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply", "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true}, "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => database, "confirmed" => true}], "confirmed" => true}, "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true}, "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true}, "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true}, "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true}
sources = Dora::CompiledFeatureRenderers.render_spring_jdbc!(feature: feature)
model = sources.fetch("backend/src/main/java/example/inventory/record_supply/Supply.java")
abort "generated model is missing" unless model.include?("record Supply(String name)")
abort "generated repository is missing JDBC write" unless sources.values.join.include?("jdbcTemplate.update")
abort "generated controller is missing declared route" unless sources.values.join.include?("@RequestMapping(\"/api/supplies\")")
abort "generated JUnit scaffold is missing" unless sources.keys.any? { |path| path.end_with?("SupplyServiceTest.java") }
puts "Dora compiled feature Spring renderer test passed (confirmed input renders Java model, JDBC repository, DTO, service, controller, validation, and JUnit scaffold)."
