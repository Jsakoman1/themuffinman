#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/compiled_feature_renderers"

database = {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true, "default" => nil, "default_confirmed" => true, "unique" => false, "unique_confirmed" => true, "index" => true, "index_confirmed" => true, "foreign_key" => nil, "foreign_key_confirmed" => true}
feature = {"kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply", "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true}, "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => database, "confirmed" => true}], "confirmed" => true}, "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true}, "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true}, "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true}, "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true}
sources = Dora::CompiledFeatureRenderers.render_api_and_vue!(feature: feature)
abort "API contract is missing declared operation" unless sources.fetch("docs/api/record-supply.yaml").include?("operationId: create-supply")
abort "Vue API client is missing declared endpoint" unless sources.fetch("frontend/src/features/record_supply/api.js").include?("/api/supplies")
abort "Vue state surface is missing accessibility state" unless sources.fetch("frontend/src/features/record_supply/FeatureView.js").include?("aria-live")
abort "frontend test scaffold is missing" unless sources.key?("frontend/src/features/record_supply/feature.test.js")
puts "Dora compiled feature Vue renderer test passed (declared API, Vue client, UI states, and frontend test scaffold are aligned)."
