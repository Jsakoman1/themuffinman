#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/compiled_feature_apply"
require_relative "../lib/dora/compiled_feature_evidence"
require_relative "../lib/dora/compiled_feature_renderers"
require_relative "../lib/dora/generated_feature_manifest"
require_relative "../lib/dora/generated_feature_safety"

ROOT = File.expand_path("..", __dir__)
feature = YAML.load_file(File.join(ROOT, "test/fixtures/compiled-feature-consumer.yaml"))
template_root = File.join(ROOT, "templates/compiled-feature")
template_paths = {"flyway-migration" => "flyway-migration.sql.erb", "spring-jdbc-feature" => "spring-jdbc-feature.java.erb", "api-contract" => "api-contract.yaml.erb", "vue-feature" => "vue-feature.js.erb", "manifest" => "manifest.yaml.erb", "evidence-obligations" => "evidence-obligations.yaml.erb"}
digests = template_paths.to_h { |id, path| [id, Digest::SHA256.file(File.join(template_root, path)).hexdigest] }
manifest = Dora::GeneratedFeatureManifest.build!(feature: feature, migration_version: "1", template_digests: digests)
rendered = Dora::CompiledFeatureRenderers.render_spring_jdbc!(feature: feature).merge(Dora::CompiledFeatureRenderers.render_api_and_vue!(feature: feature))
rendered["backend/src/main/resources/db/migration/V1__create_supply.sql"] = Dora::CompiledFeatureRenderers.render_flyway!(feature: feature, migration_version: "1")
rendered["docs/capabilities/record-supply.yaml"] = YAML.dump({"kind" => "dora_generated_capability", "version" => 1, "capability" => "record-supply"})
rendered["docs/capabilities/record-supply-evidence.yaml"] = YAML.dump(Dora::CompiledFeatureEvidence.declare!(manifest))
rendered[".dora/generated-features/record-supply.yaml"] = YAML.dump(manifest)
expected_paths = manifest.fetch("outputs").map { |output| output.fetch("path") }
abort "rendered paths differ from manifest: missing=#{expected_paths - rendered.keys} extra=#{rendered.keys - expected_paths}" unless rendered.keys.sort == expected_paths.sort

Dir.mktmpdir("dora-independent-compiled-consumer") do |root|
  Dir.chdir(root) do
    result = Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "consumer", rendered_files: rendered, dry_run: false)
    abort "consumer did not receive every manifest file" unless result.fetch("files").sort == rendered.keys.sort
    project = File.join(root, "consumer")
    expected_digests = rendered.to_h { |path, content| [path, Digest::SHA256.hexdigest(content)] }
    trace = {"dto_fields" => ["name"], "api_operations" => ["create-supply"], "workflow_id" => "supply-lifecycle", "permission_id" => "household-write"}
    report = Dora::GeneratedFeatureSafety.inspect!(manifest: manifest, feature: feature, project_root: project, expected_digests: expected_digests, trace: trace)
    abort "fresh consumer safety report has gaps: #{report.fetch("findings")}" unless report.fetch("safe_to_continue") == true
    obligations = YAML.load_file(File.join(project, "docs/capabilities/record-supply-evidence.yaml"))
    abort "consumer acceptance was fabricated" unless obligations.fetch("acceptance_status") == "unresolved"
    begin
      Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "consumer", rendered_files: rendered, dry_run: false)
      abort "consumer collision was accepted"
    rescue ArgumentError => error
      abort error.message unless error.message.include?("existing paths") || error.message.include?("historic migration")
    end
  end
end

puts "Dora independent compiled feature consumer test passed (a fresh consumer receives the full manifest output, unresolved evidence, static safety trace, and collision protection)."
