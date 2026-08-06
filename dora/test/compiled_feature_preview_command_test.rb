#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
templates = %w[flyway-migration spring-jdbc-feature api-contract vue-feature manifest evidence-obligations].to_h { |id| [id, Digest::SHA256.hexdigest(id)] }
database = {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true, "default" => nil, "default_confirmed" => true, "unique" => false, "unique_confirmed" => true, "index" => true, "index_confirmed" => true, "foreign_key" => nil, "foreign_key_confirmed" => true}
feature = {"kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply", "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true}, "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => database, "confirmed" => true}], "confirmed" => true}, "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true}, "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true}, "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true}, "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true}

Dir.mktmpdir("dora-compiled-feature-preview") do |root|
  input_path = File.join(root, "compiled-feature.yaml")
  File.write(input_path, YAML.dump({"kind" => "dora_compiled_feature_preview_input", "version" => 1, "feature" => feature, "migration_version" => "1", "template_digests" => templates}))
  output, status = Open3.capture2e(CLI, "compiled-feature-preview", input_path, "--format", "json", chdir: ROOT)
  abort "compiled feature preview failed: #{output}" unless status.success?
  paths = JSON.parse(output).dig("payload", "manifest", "outputs").map { |entry| entry.fetch("path") }
  abort "compiled preview has no Java model" unless paths.include?("backend/src/main/java/example/inventory/record_supply/Supply.java")
  abort "compiled preview wrote a project file" unless Dir.children(root) == ["compiled-feature.yaml"]
end

puts "Dora compiled feature preview command test passed (a read-only envelope reports deterministic generated paths without writing a consumer)."
