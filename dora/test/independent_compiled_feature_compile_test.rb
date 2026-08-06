#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/compiled_feature_apply"
require_relative "../lib/dora/compiled_feature_evidence"
require_relative "../lib/dora/compiled_feature_renderers"
require_relative "../lib/dora/generated_feature_manifest"

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

Dir.mktmpdir("dora-independent-compiled-feature") do |root|
  consumer = File.join(root, "consumer")
  FileUtils.mkdir_p(consumer)
  FileUtils.cp_r(File.join(ROOT, "templates/starters/spring-vue-postgres/."), consumer)
  Dir.chdir(root) { Dora::CompiledFeatureApply.apply!(manifest: manifest, destination: "consumer", rendered_files: rendered, dry_run: false) }
  backend, backend_status = Open3.capture2e("mvn", "-q", "-f", "backend/pom.xml", "test", chdir: consumer)
  abort "generated backend did not compile: #{backend}" unless backend_status.success?
  %w[api.js FeatureView.js].each do |name|
    path = File.join("frontend/src/features/record_supply", name)
    output, status = Open3.capture2e("node", "--check", path, chdir: consumer)
    abort "generated frontend syntax is invalid: #{output}" unless status.success?
  end
  frontend, frontend_status = Open3.capture2e("node", "--test", "frontend/src/features/record_supply/feature.test.js", chdir: consumer)
  abort "generated frontend test did not pass: #{frontend}" unless frontend_status.success?
end

puts "Dora independent compiled feature compile test passed (fresh Spring JDBC source compiles and generated Vue module syntax and test pass without Docker, PostgreSQL, or a browser)."
