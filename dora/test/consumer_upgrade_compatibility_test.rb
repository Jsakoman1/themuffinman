#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/consumer_upgrade_compatibility"

preview = {
  "kind" => "dora_project_upgrade_preview",
  "version" => 1,
  "read_only" => true,
  "consumer" => {"current_ref" => "a" * 40},
  "target" => {"ref" => "b" * 40},
  "migrations" => {
    "added" => ["templates/new-control.yaml"],
    "changed" => ["bin/dora", "lib/dora/plugin_contract.rb", "work-artifact-schema.yaml"],
    "removed" => ["lib/dora/retired.rb", "unknown/retired.txt"]
  }
}

before = Marshal.load(Marshal.dump(preview))
report = Dora::ConsumerUpgradeCompatibility.report!(preview: preview)
abort "compatibility report is not advisory provenance" unless report.fetch("read_only") && report.fetch("disposition") == "advisory" && report.fetch("observed_at").match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && report.fetch("source_references") == ["project_upgrade_preview", "current:#{'a' * 40}", "target:#{'b' * 40}"]
abort "compatibility report mutated preview" unless preview == before
abort "compatibility report claimed consumer proof" unless report.fetch("consumer_compatibility") == "not_proven"
abort "template addition was not additive" unless report.dig("dimensions", "template", "classification") == "additive" && report.dig("dimensions", "template", "required_retests") == ["template-freshness"]
abort "command change was not conservative" unless report.dig("dimensions", "command", "classification") == "unknown" && report.dig("dimensions", "command", "required_retests") == ["command-surface"]
abort "plugin change was not routed" unless report.dig("dimensions", "plugin", "required_retests") == ["plugin-contract"]
abort "schema change was not routed" unless report.dig("dimensions", "schema", "required_retests") == ["work-artifact-schema"]
abort "removed contract was not breaking" unless report.dig("dimensions", "core_implementation", "classification") == "breaking" && report.dig("dimensions", "unknown", "classification") == "breaking"

unsafe = Marshal.load(Marshal.dump(preview))
unsafe.fetch("migrations").fetch("added") << "../outside"
begin
  Dora::ConsumerUpgradeCompatibility.report!(preview: unsafe)
  abort "compatibility report accepted an unsafe path"
rescue ArgumentError => error
  abort "wrong unsafe-preview rejection: #{error.message}" unless error.message.include?("invalid")
end

domain_library = YAML.load_file(File.expand_path("../docs/domain-library.yaml", __dir__))
abort "domain library omits upgrade compatibility read model" unless domain_library.fetch("vocabulary").any? { |item| item.fetch("id") == "consumer-upgrade-compatibility-report" && item.fetch("description").include?("ProjectUpgrade preview") }
abort "domain library omits compatibility authority boundary" unless domain_library.fetch("invariants").any? { |item| item.fetch("id") == "upgrade-compatibility-advisory-boundary" && item.fetch("description").include?("no classification can apply") }

puts "Dora consumer upgrade compatibility test passed (advisory dimensions, conservative unknowns, and no consumer proof)."
