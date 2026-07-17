#!/usr/bin/env ruby

require "yaml"

catalog = YAML.load_file("docs/target-capability-catalog.yaml")
inventory = YAML.load_file("docs/capability-inventory.yaml")
inventory_capabilities = inventory.fetch("modules").flat_map { |mod| mod.fetch("capabilities") }
inventory_by_id = inventory_capabilities.to_h { |capability| [capability.fetch("id"), capability] }

priority_order = { "critical" => 0, "high" => 1, "medium" => 2, "low" => 3 }

def aggregate_status(statuses)
  return "unmapped" if statuses.empty?
  return "broken" if statuses.include?("broken")
  return "partial" if statuses.include?("partial")
  return "planned" if statuses.include?("planned")
  return "not_offered" if statuses.include?("not_offered")
  return "verified" if statuses.all? { |status| status == "verified" }

  "implemented"
end

def workstream_for(module_id)
  {
    "platform" => "platform-and-identity",
    "vision" => "vision-command-surface",
    "work" => "work-marketplace",
    "circles" => "circles-and-relationships",
    "chat" => "shared-chat",
    "business" => "business-and-booking",
    "sharing" => "things-and-rides",
    "cross_module" => "cross-module-platform"
  }.fetch(module_id)
end

rows = catalog.fetch("capabilities").map do |target|
  links = target.fetch("current_inventory_ids")
  current = links.map { |id| inventory_by_id[id] }.compact
  statuses = current.map { |capability| capability.fetch("status") }.uniq
  mapping_quality = if links.empty?
                      "unmapped"
                    elsif links.include?(target.fetch("id"))
                      "exact"
                    else
                      "broad"
                    end
  target_surfaces = target.fetch("target_surfaces")
  current_web = current.any? { |capability| !capability.fetch("current", {}).fetch("web", []).empty? }
  current_vision = target.fetch("id").start_with?("vision.") || current.any? do |capability|
    next true if capability.fetch("id").start_with?("vision.")
    current = capability.fetch("current", {})
    current.fetch("web", []).any? { |surface| surface.to_s.include?("vision") } ||
      current.fetch("backend", []).any? { |surface| surface.to_s.include?("vision") }
  end
  gaps = []
  gaps << "exact-current-evidence" if mapping_quality == "broad"
  gaps << "current-mapping" if mapping_quality == "unmapped"
  gaps << "web-ui" if target_surfaces.fetch("web_ui") && !current_web
  gaps << "vision" if target_surfaces.fetch("vision") && !current_vision

  {
    "slice_id" => "capability-#{target.fetch('id').tr('.', '-')}",
    "capability_id" => target.fetch("id"),
    "module" => target.fetch("module"),
    "object" => target.fetch("object"),
    "action" => target.fetch("action"),
    "name" => target.fetch("name"),
    "priority" => target.fetch("priority"),
    "queue" => %w[critical high].include?(target.fetch("priority")) ? "next" : "later",
    "workstream" => workstream_for(target.fetch("module")),
    "current_status" => aggregate_status(statuses),
    "mapping_quality" => mapping_quality,
    "current_inventory_ids" => links,
    "target_surfaces" => target_surfaces,
    "gaps" => gaps,
    "recommended_scope" => "Implement and verify #{target.fetch('name').downcase} as one backend-owned user capability."
  }
end

rows.sort_by! { |row| [priority_order.fetch(row.fetch("priority")), row.fetch("module"), row.fetch("capability_id")] }

summary = {
  "total" => rows.length,
  "next_queue" => rows.count { |row| row.fetch("queue") == "next" },
  "later_queue" => rows.count { |row| row.fetch("queue") == "later" },
  "critical" => rows.count { |row| row.fetch("priority") == "critical" },
  "high" => rows.count { |row| row.fetch("priority") == "high" },
  "broad_mapping" => rows.count { |row| row.fetch("mapping_quality") == "broad" },
  "missing_web_ui" => rows.count { |row| row.fetch("gaps").include?("web-ui") },
  "missing_vision" => rows.count { |row| row.fetch("gaps").include?("vision") }
}

report = {
  "version" => 1,
  "kind" => "target_capability_implementation_slices",
  "generated_from" => ["docs/target-capability-catalog.yaml", "docs/capability-inventory.yaml"],
  "semantics" => {
    "next" => "Critical and high priority capability slices for the next implementation planning pass.",
    "later" => "Medium and low priority slices retained for future sequencing.",
    "mapping_quality" => "Broad or unmapped records need exact implementation evidence before closure."
  },
  "summary" => summary,
  "slices" => rows
}

puts YAML.dump(report)
