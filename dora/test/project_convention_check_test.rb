#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_convention_check"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
profile = YAML.load_file(File.join(ROOT, "templates/project-convention-profile.yaml"))
profile.merge!("project_id" => "consumer", "java_package" => "example.inventory", "confirmation" => true, "test_commands" => [{"id" => "backend-test", "command" => "mvn test"}])
manifest = {"outputs" => [{"id" => "flyway-migration", "path" => "backend/src/main/resources/db/migration/V1__create_item.sql"}, {"id" => "java-model", "path" => "backend/src/main/java/example/inventory/item/Item.java"}, {"id" => "junit", "path" => "backend/src/test/java/example/inventory/item/ItemTest.java"}, {"id" => "api-contract", "path" => "docs/api/record-item.yaml"}, {"id" => "vue-client", "path" => "frontend/src/features/item/api.js"}, {"id" => "capability-doc", "path" => "docs/capabilities/record-item.yaml"}, {"id" => "generated-manifest", "path" => ".dora/generated-features/record-item.yaml"}]}

report = Dora::ProjectConventionCheck.inspect!(profile: profile, manifest: manifest)
abort "compatible manifest was rejected" unless report.fetch("compatible") && report.fetch("findings").empty?

invalid = Marshal.load(Marshal.dump(manifest)); invalid.fetch("outputs")[4]["path"] = "frontend/src/modules/item/api.js"
report = Dora::ProjectConventionCheck.inspect!(profile: profile, manifest: invalid)
abort "convention mismatch was accepted" if report.fetch("compatible")
abort "frontend convention mismatch was not reported" unless report.fetch("findings").any? { |finding| finding.fetch("output_id") == "vue-client" }

Dir.mktmpdir("dora-convention-check") do |root|
  profile_path = File.join(root, "profile.yaml"); manifest_path = File.join(root, "manifest.yaml")
  File.write(profile_path, YAML.dump(profile)); File.write(manifest_path, YAML.dump(manifest))
  output, status = Open3.capture2e(CLI, "convention-check", profile_path, manifest_path, "--format", "json", chdir: ROOT)
  abort "convention-check command failed: #{output}" unless status.success?
  abort "convention-check command did not report compatibility" unless JSON.parse(output).dig("payload", "compatible") == true
end

puts "Dora project convention check test passed (declared additive output roots are accepted and mismatches fail closed)."
