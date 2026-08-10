#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/capability_scenario_catalog"

def scenario(id, scenario_class, evidence_class)
  {"id" => id, "class" => scenario_class, "evidence_class" => evidence_class, "expected" => "The declared #{scenario_class.tr("_", " ")} outcome is demonstrated by project-owned evidence.", "source_references" => ["docs/domain-library.yaml##{id}"]}
end

catalog = {
  "kind" => "dora_capability_scenario_catalog",
  "version" => 1,
  "capability" => "record-note",
  "source_references" => ["docs/product-brief.yaml#record-note"],
  "scenarios" => [
    scenario("record-happy", "happy_path", "unit"),
    scenario("record-forbidden", "permission", "api"),
    scenario("record-retry", "recovery", "acceptance"),
    scenario("record-regression", "regression", "browser_runtime")
  ]
}

before = Marshal.load(Marshal.dump(catalog))
result = Dora::CapabilityScenarioCatalog.build!(catalog)
abort "scenario catalog is not advisory provenance" unless result.fetch("read_only") && result.fetch("disposition") == "advisory" && result.fetch("observed_at").match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && result.fetch("source_references") == ["docs/product-brief.yaml#record-note"]
abort "scenario catalog changed its declaration" unless catalog == before
abort "scenario catalog did not preserve all scenario classes" unless result.fetch("scenarios").map { |item| item.fetch("class") }.sort == Dora::CapabilityScenarioCatalog::SCENARIO_CLASSES.sort
abort "scenario catalog invented evidence state" if result.fetch("scenarios").any? { |item| item.key?("status") }

with_status = Marshal.load(Marshal.dump(catalog))
with_status.fetch("scenarios").first["status"] = "verified"
begin
  Dora::CapabilityScenarioCatalog.build!(with_status)
  abort "scenario catalog accepted a duplicate evidence status"
rescue ArgumentError => error
  abort "wrong duplicate-state rejection: #{error.message}" unless error.message.include?("invalid")
end

missing_class = Marshal.load(Marshal.dump(catalog))
missing_class.fetch("scenarios").pop
begin
  Dora::CapabilityScenarioCatalog.build!(missing_class)
  abort "scenario catalog accepted a missing scenario class"
rescue ArgumentError => error
  abort "wrong missing-class rejection: #{error.message}" unless error.message.include?("missing classes")
end

template = YAML.load_file(File.expand_path("../templates/capability-scenario-catalog.yaml", __dir__))
template_result = Dora::CapabilityScenarioCatalog.build!(template)
abort "scenario catalog template is not reusable" unless template_result.fetch("capability") == "replace-with-capability" && template_result.fetch("scenarios").map { |item| item.fetch("class") }.sort == Dora::CapabilityScenarioCatalog::SCENARIO_CLASSES.sort

puts "Dora capability scenario catalog test passed (advisory taxonomy, provenance, and no duplicate evidence state)."
