#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "tmpdir"
require_relative "../lib/dora/generated_feature_safety"

digests = %w[flyway-migration spring-jdbc-feature api-contract vue-feature manifest evidence-obligations].to_h { |id| [id, Digest::SHA256.hexdigest(id)] }
database = {"column" => "name", "sql_type" => "varchar", "nullable" => false, "confirmed" => true, "default" => nil, "default_confirmed" => true, "unique" => false, "unique_confirmed" => true, "index" => true, "index_confirmed" => true, "foreign_key" => nil, "foreign_key_confirmed" => true}
feature = {"kind" => "dora_compiled_feature", "version" => 1, "capability" => "record-supply", "stack" => {"id" => "spring-vue-postgres-buildable", "package" => "example.inventory", "backend_root" => "backend", "frontend_root" => "frontend", "migration_directory" => "backend/src/main/resources/db/migration", "confirmed" => true}, "entity" => {"id" => "supply", "table" => "supply", "fields" => [{"id" => "name", "java_type" => "String", "database" => database, "confirmed" => true}], "confirmed" => true}, "permission" => {"id" => "household-write", "enforcement" => "service", "rule" => "Only an explicitly authorized actor may create a supply.", "confirmed" => true}, "workflow" => {"id" => "supply-lifecycle", "initial_state" => "active", "states" => %w[active archived], "transitions" => [{"from" => "active", "to" => "archived", "action" => "archive", "confirmed" => true}], "confirmed" => true}, "api" => {"base_path" => "/api/supplies", "operations" => [{"id" => "create-supply", "method" => "POST", "path" => "/api/supplies", "request_fields" => ["name"], "response_fields" => ["name"], "confirmed" => true}], "confirmed" => true}, "ui" => {"blueprint" => "list-detail", "states" => %w[loading empty error populated], "confirmed" => true}, "confirmation" => true}
path = "docs/api/record-supply.yaml"
manifest = {"kind" => "dora_generated_feature_manifest", "version" => 1, "generator_version" => "1.6", "confirmed_input_digest" => Digest::SHA256.hexdigest("feature"), "migration_version" => "1", "template_digests" => digests, "outputs" => [{"id" => "api-contract", "path" => path, "template" => "api-contract", "template_digest" => digests.fetch("api-contract")}], "unresolved_obligations" => [{"id" => "compile", "reason" => "Not compiled."}]}
Dir.mktmpdir("dora-generated-feature-safety") do |root|
  target = File.join(root, path); FileUtils.mkdir_p(File.dirname(target)); File.write(target, "contract\n")
  expected = {path => Digest::SHA256.file(target).hexdigest}
  trace = {"dto_fields" => ["name"], "api_operations" => ["create-supply"], "workflow_id" => "supply-lifecycle", "permission_id" => "household-write"}
  safe = Dora::GeneratedFeatureSafety.inspect!(manifest: manifest, feature: feature, project_root: root, expected_digests: expected, trace: trace)
  abort "safe generated output was rejected" unless safe.fetch("safe_to_continue") == true
  File.write(target, "changed\n")
  findings = Dora::GeneratedFeatureSafety.inspect!(manifest: manifest, feature: feature, project_root: root, expected_digests: expected, trace: trace, historic_migrations: [path]).fetch("findings").map { |finding| finding.fetch("id") }
  abort "digest drift was not found" unless findings.include?("content_digest_mismatch")
  trace["permission_id"] = "different"
  gaps = Dora::GeneratedFeatureSafety.inspect!(manifest: manifest, feature: feature, project_root: root, expected_digests: expected, trace: trace).fetch("findings").map { |finding| finding.fetch("id") }
  abort "permission trace gap was not found" unless gaps.include?("permission_trace_gap")
end
puts "Dora generated feature safety test passed (actual output hashes, historic migration targets, and DTO/API/workflow/permission traces are inspected statically)."
