#!/usr/bin/env ruby
# frozen_string_literal: true

# Goal-pursuit preflight reviewed 2026-07-24; this audit remains the atom-to-queue control.
# Atom-to-inventory reconciliation reviewed 2026-07-24.

require "yaml"

matrix_path = ARGV.fetch(0)
inventory_path = ARGV.fetch(1)
matrix = YAML.load_file(matrix_path)
inventory = YAML.load_file(inventory_path)
failures = []

atoms = Array(matrix["groups"]).flat_map { |group| Array(group["atoms"]) }
inventory_items = Array(inventory["items"])
inventory_ids = inventory_items.map { |item| item.fetch("id") }
atom_ids = atoms.map { |atom| atom.fetch("id") }
reference_mapping = inventory.fetch("reference_mapping", {})

failures << "atom ids are duplicated" unless atom_ids.uniq.length == atom_ids.length
failures << "atomization matrix has no atoms" if atoms.empty?
failures << "required atom fields are not declared" unless Array(matrix["required_atom_fields"]).sort == %w[dependencies evidence_boundary execution_item id observable_outcome required_paths validation].sort

atoms.each do |atom|
  id = atom["id"] || "unknown"
  failures << "#{id}: execution item is missing" if atom["execution_item"].to_s.strip.empty?
  failures << "#{id}: execution item is not in inventory" unless inventory_ids.include?(atom["execution_item"])
  failures << "#{id}: observable outcome is missing" if atom["observable_outcome"].to_s.strip.empty?
  failures << "#{id}: required paths are missing" if Array(atom["required_paths"]).empty?
  failures << "#{id}: leaf validation is missing" if atom["validation"].to_s.strip.empty?
  failures << "#{id}: evidence boundary is missing" if atom["evidence_boundary"].to_s.strip.empty?
  Array(atom["dependencies"]).each do |dependency|
    failures << "#{id}: unknown dependency #{dependency}" unless atom_ids.include?(dependency)
  end
end

mapped_counts = Hash.new(0)
atoms.each { |atom| mapped_counts[atom["execution_item"]] += 1 }
failures << "atomization does not map to all executable inventory items" unless inventory_ids.all? { |id| mapped_counts.key?(id) || id == "apple-product-polish-atomic-hardening" || id == "apple-product-polish-deep-atomization" }
failures << "inventory is missing the canonical design authority" if inventory["design_authority"].to_s.strip.empty?
failures << "design reference mapping does not cover every inventory item" unless reference_mapping.keys.sort == inventory_ids.sort
reference_mapping.each do |id, mapping|
  failures << "#{id}: reference sections are missing" if Array(mapping["reference_sections"]).empty?
  failures << "#{id}: reference acceptance contract is missing" if mapping["acceptance_contract"].to_s.strip.empty?
end

sequence_by_id = inventory_items.to_h { |item| [item.fetch("id"), item.fetch("sequence")] }
inventory_items.each do |item|
  item.fetch("depends_on", []).each do |dependency|
    failures << "#{item.fetch("id")}: unknown inventory dependency #{dependency}" unless sequence_by_id.key?(dependency)
    failures << "#{item.fetch("id")}: dependency #{dependency} runs after the item" if sequence_by_id.key?(dependency) && sequence_by_id.fetch(dependency) >= item.fetch("sequence")
  end
  plan = YAML.load_file(item.fetch("plan"))
  failures << "#{item.fetch("id")}: owning plan is missing canonical design authority" unless plan["design_authority"] == inventory["design_authority"]
end

abort "Apple desktop atomization audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Apple desktop atomization audit passed (#{atoms.length} atoms, #{mapped_counts.length} mapped execution items)."
