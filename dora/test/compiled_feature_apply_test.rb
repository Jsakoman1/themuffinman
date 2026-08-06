#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "tmpdir"
require_relative "../lib/dora/compiled_feature_apply"

digests = %w[flyway-migration spring-jdbc-feature api-contract vue-feature manifest evidence-obligations].to_h { |id| [id, Digest::SHA256.hexdigest(id)] }
manifest = {"kind" => "dora_generated_feature_manifest", "version" => 1, "generator_version" => "1.6", "confirmed_input_digest" => Digest::SHA256.hexdigest("feature"), "migration_version" => "1", "template_digests" => digests, "outputs" => [{"id" => "flyway-migration", "path" => "backend/src/main/resources/db/migration/V1__create_supply.sql", "template" => "flyway-migration", "template_digest" => digests.fetch("flyway-migration")}, {"id" => "generated-manifest", "path" => ".dora/generated-features/record-supply.yaml", "template" => "manifest", "template_digest" => digests.fetch("manifest")}], "unresolved_obligations" => [{"id" => "compile", "reason" => "Not compiled."}]}
rendered = {"backend/src/main/resources/db/migration/V1__create_supply.sql" => "CREATE TABLE supply ();\n", ".dora/generated-features/record-supply.yaml" => "kind: generated\n"}

Dir.mktmpdir("dora-compiled-feature-apply") do |root|
  Dir.chdir(root) do
    dry_run = Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "consumer", rendered_files: rendered, dry_run: true)
    abort "dry-run wrote a consumer" if File.exist?("consumer") || dry_run.fetch("mode") != "dry_run"
    result = Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "consumer", rendered_files: rendered, dry_run: false)
    abort "apply did not write migration" unless result.fetch("mode") == "applied" && File.file?("consumer/backend/src/main/resources/db/migration/V1__create_supply.sql")
    begin
      Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "consumer", rendered_files: rendered, dry_run: false)
      abort "historic migration collision was accepted"
    rescue ArgumentError => error
      abort error.message unless error.message.include?("historic migration")
    end
  end
end

begin
  Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "../unsafe", rendered_files: rendered, dry_run: true)
  abort "traversal destination was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("relative")
end
puts "Dora compiled feature apply test passed (manifest-only dry-run and apply reject traversal and historical migration collisions)."
