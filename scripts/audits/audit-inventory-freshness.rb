#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

inventory = YAML.load_file("docs/capability-inventory.yaml")
modules = inventory.fetch("modules")
module_ids = modules.map { |item| item.fetch("id") }
raise "Duplicate inventory module IDs" unless module_ids.uniq.length == module_ids.length

capabilities = modules.flat_map do |mod|
  mod.fetch("capabilities").map { |capability| [mod.fetch("id"), capability] }
end
ids = capabilities.map { |_module_id, capability| capability.fetch("id") }
duplicates = ids.group_by(&:itself).select { |_id, entries| entries.length > 1 }.keys
raise "Duplicate capability IDs: #{duplicates.join(', ')}" unless duplicates.empty?

namespace_modules = {
  "platform" => ["platform"],
  "vision" => ["vision"],
  "work" => ["work"],
  "chat" => ["chat"],
  "business" => ["business"],
  "circles" => ["circles"],
  "sharing" => ["sharing", "things", "rides"],
  "cross_module" => ["cross_module"]
}
namespace_errors = capabilities.each_with_object([]) do |(module_id, capability), errors|
  namespace = capability.fetch("id").split(".").first
  next if namespace_modules.fetch(module_id).include?(namespace)

  errors << "#{capability.fetch("id")} is under #{module_id}, expected #{namespace_modules.fetch(module_id).join('/') }"
end
raise namespace_errors.join("\n") unless namespace_errors.empty?

status_counts = capabilities.each_with_object(Hash.new(0)) do |_entry, counts|
  counts[_entry.last.fetch("status")] += 1
end
unknown_statuses = status_counts.keys - %w[planned not_offered partial broken implemented verified deprecated]
raise "Unknown inventory statuses: #{unknown_statuses.join(', ')}" unless unknown_statuses.empty?

missing_current = capabilities.each_with_object([]) do |_entry, missing|
  capability = _entry.last
  missing << capability.fetch("id") if capability.fetch("current", nil).nil?
end
raise "Capabilities missing current surface data: #{missing_current.join(', ')}" unless missing_current.empty?

evidence_paths = capabilities.flat_map do |_module_id, capability|
  evidence = capability.fetch("evidence", {}) || {}
  evidence.values.flatten.select { |path| path.is_a?(String) && path.start_with?("apps/", "docs/", "scripts/") }
end.uniq
missing_evidence = evidence_paths.reject { |path| File.file?(path) || Dir.exist?(path) }
raise "Inventory references missing evidence paths: #{missing_evidence.join(', ')}" unless missing_evidence.empty?

puts "Inventory freshness audit passed"
puts "  modules: #{modules.length}"
puts "  capabilities: #{capabilities.length}"
puts "  statuses: #{status_counts.sort.to_h}"
puts "  declared gaps: #{capabilities.count { |_module_id, capability| Array(capability["gaps"]).any? }}"
puts "  evidence-bearing capabilities: #{capabilities.count { |_module_id, capability| capability["evidence"].is_a?(Hash) && !capability["evidence"].empty? }}"
puts "  evidence paths checked: #{evidence_paths.length}"
