#!/usr/bin/env ruby

require "yaml"

catalog = YAML.load_file("docs/target-capability-catalog.yaml")
schema = YAML.load_file("docs/target-capability-catalog.schema.yaml")
inventory = YAML.load_file("docs/capability-inventory.yaml")
abort "invalid catalog kind" unless catalog["kind"] == "target_product_capability_catalog"
abort "invalid schema kind" unless schema["kind"] == "target_product_capability_schema"

modules = catalog.fetch("modules")
capabilities = catalog.fetch("capabilities")
module_ids = modules.map { |mod| mod.fetch("id") }
abort "catalog modules must be non-empty" unless modules.is_a?(Array) && !modules.empty?
abort "catalog capabilities must be non-empty" unless capabilities.is_a?(Array) && !capabilities.empty?
abort "duplicate module ids" unless module_ids.uniq.length == module_ids.length

object_keys = []
indexed_ids = []
modules.each do |mod|
  mod.fetch("objects").each do |object|
    key = [mod.fetch("id"), object.fetch("id")]
    abort "duplicate module/object: #{key.join(".")}" if object_keys.include?(key)
    object_keys << key
    indexed_ids.concat(object.fetch("capability_ids"))
  end
end

inventory_ids = inventory.fetch("modules").flat_map { |mod| mod.fetch("capabilities").map { |cap| cap.fetch("id") } }
required = %w[id module object action type name actor priority target_surfaces access outcome failure_behavior acceptance current_inventory_ids]
surfaces = %w[vision web_ui mobile watch api]
types = %w[query mutation interaction lifecycle system]
priorities = %w[critical high medium low]
ids = []

capabilities.each do |capability|
  id = capability["id"] || "unknown"
  missing = required.reject { |field| capability.key?(field) }
  abort "#{id} missing fields: #{missing.join(', ')}" unless missing.empty?
  abort "duplicate capability: #{id}" if ids.include?(id)
  ids << id
  abort "#{id} has invalid id format" unless id.start_with?("#{capability['module']}.#{capability['object']}.")
  abort "#{id} references unknown module" unless module_ids.include?(capability["module"])
  abort "#{id} references unknown object" unless object_keys.include?([capability["module"], capability["object"]])
  abort "#{id} has invalid type" unless types.include?(capability["type"])
  abort "#{id} has invalid priority" unless priorities.include?(capability["priority"])
  target = capability.fetch("target_surfaces")
  abort "#{id} has invalid target surfaces" unless target.keys.sort == surfaces.sort && target.values.all? { |value| value == true || value == false }
  abort "#{id} targets no product surface" unless target.values.any?
  access = capability.fetch("access")
  abort "#{id} has incomplete access metadata" unless access["scope"] && access.key?("consent_required") && access["privacy_rule"]
  abort "#{id} has no acceptance criteria" unless capability["acceptance"].is_a?(Array) && !capability["acceptance"].empty?
  abort "#{id} current_inventory_ids must be an array" unless capability["current_inventory_ids"].is_a?(Array)
  unknown = capability["current_inventory_ids"] - inventory_ids
  abort "#{id} links unknown inventory IDs: #{unknown.join(', ')}" unless unknown.empty?
end

missing_from_index = ids - indexed_ids
unknown_index_ids = indexed_ids - ids
abort "capabilities missing from module/object index: #{missing_from_index.join(', ')}" unless missing_from_index.empty?
abort "module/object index references missing capabilities: #{unknown_index_ids.join(', ')}" unless unknown_index_ids.empty?
missing_modules = inventory.fetch("modules").map { |mod| mod.fetch("id") } - module_ids
abort "inventory modules missing from target catalog: #{missing_modules.join(', ')}" unless missing_modules.empty?

puts "Target capability catalog audit passed (#{ids.length} capabilities, #{modules.length} modules, #{inventory_ids.length} inventory IDs available)."
