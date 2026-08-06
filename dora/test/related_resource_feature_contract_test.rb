#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/compiled_feature_contract"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "related-resource-feature.schema.yaml"))
document = YAML.load_file(File.join(ROOT, "test/fixtures/related-resource-feature.yaml"))

abort "related resource schema is invalid" unless schema["kind"] == "dora_related_resource_feature_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| document.key?(field) }
abort "related resource fixture is missing #{missing.join(", ")}" unless missing.empty?
abort "wrapped feature is not confirmed" unless Dora::CompiledFeatureContract.validate_type_mappings!(document.fetch("feature")).dig("entity", "fields", 0, "database", "foreign_key", "confirmed") == true
relation_missing = schema.fetch("relation_required_fields").reject { |field| document.fetch("relation").key?(field) }
abort "relation fixture is incomplete" unless relation_missing.empty?
query_missing = schema.fetch("query_required_fields").reject { |field| document.fetch("query").key?(field) }
abort "query fixture is incomplete" unless query_missing.empty?
abort "related resource fixture lacks a confirmed convention profile" unless document.fetch("convention_profile") == "spring-vue-postgres-profile" && document.fetch("confirmation") == true

puts "Dora related resource feature contract test passed (explicit relation, query, states, convention, and additive boundary are declared)."
