#!/usr/bin/env ruby
# frozen_string_literal: true
require_relative "../lib/dora/capability_inventory"

valid = {"kind" => "dora_capability_inventory", "version" => 1, "component" => {"id" => "dora", "owner" => "Dora"}, "capabilities" => [{"id" => "read-model", "title" => "Read model", "status" => "verified", "owner" => "Dora", "documentation_references" => ["docs/product-brief.yaml"], "evidence_references" => ["test/project_read_model_test.rb"], "gaps" => []}]}
Dora::CapabilityInventory.validate!(valid)
Dora::CapabilityInventory.validate!(YAML.load_file(File.expand_path("../templates/capability-inventory.yaml", __dir__)))
Dora::CapabilityInventory.validate!(YAML.load_file(File.expand_path("../docs/capability-inventory.yaml", __dir__)))
begin
  Dora::CapabilityInventory.validate!(valid.merge("capabilities" => [valid.fetch("capabilities").first.merge("evidence_references" => [])]))
  abort "verified capability without evidence was accepted"
rescue ArgumentError
  nil
end
puts "Dora capability inventory test passed (explicit status and verified-evidence boundary)."
