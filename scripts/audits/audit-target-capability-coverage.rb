#!/usr/bin/env ruby

require "yaml"

catalog = YAML.load_file("docs/target-capability-catalog.yaml")
inventory = YAML.load_file("docs/capability-inventory.yaml")

inventory_capabilities = inventory.fetch("modules").flat_map { |mod| mod.fetch("capabilities") }
inventory_by_id = inventory_capabilities.to_h { |capability| [capability.fetch("id"), capability] }

def current_status(statuses)
  return "unmapped" if statuses.empty?
  return "broken" if statuses.include?("broken")
  return "partial" if statuses.include?("partial")
  return "planned" if statuses.include?("planned")
  return "not_offered" if statuses.include?("not_offered")
  return "verified" if statuses.all? { |status| status == "verified" }

  "implemented"
end

def current_web_available?(capability)
  current = capability.fetch("current", {})
  current.fetch("web", []).is_a?(Array) && !current.fetch("web", []).empty?
end

def current_vision_available?(capability)
  return true if capability.fetch("id").start_with?("vision.")

  current = capability.fetch("current", {})
  return true if current.fetch("vision", []).is_a?(Array) && !current.fetch("vision", []).empty?

  current.fetch("web", []).any? { |surface| surface.to_s.include?("vision") } ||
    current.fetch("backend", []).any? { |surface| surface.to_s.include?("vision") }
end

rows = catalog.fetch("capabilities").map do |target|
  links = target.fetch("current_inventory_ids")
  current = links.map { |id| inventory_by_id[id] }.compact
  statuses = current.map { |capability| capability.fetch("status") }.uniq
  target_surfaces = target.fetch("target_surfaces")
  current_surfaces = {
    "web_ui" => current.any? { |capability| current_web_available?(capability) },
    "vision" => current.any? { |capability| current_vision_available?(capability) }
  }
  mapping_quality = if links.empty?
                      "unmapped"
                    elsif links.include?(target.fetch("id"))
                      "exact"
                    else
                      "broad"
                    end

  surface_coverage = %w[web_ui vision].to_h do |surface|
    state = if !target_surfaces.fetch(surface)
              "not_required"
            elsif current_surfaces.fetch(surface)
              "covered"
            else
              "missing"
            end
    [surface, state]
  end

  gaps = []
  gaps << "current_mapping" if links.empty?
  gaps << "decomposition_required" if mapping_quality == "broad"
  gaps << "web_ui" if surface_coverage["web_ui"] == "missing"
  gaps << "vision" if surface_coverage["vision"] == "missing"

  {
    "id" => target.fetch("id"),
    "module" => target.fetch("module"),
    "object" => target.fetch("object"),
    "name" => target.fetch("name"),
    "priority" => target.fetch("priority"),
    "current_inventory_ids" => links,
    "mapping_quality" => mapping_quality,
    "current_statuses" => statuses,
    "current_status" => current_status(statuses),
    "target_surfaces" => target_surfaces,
    "current_surfaces" => current_surfaces,
    "surface_coverage" => surface_coverage,
    "gaps" => gaps
  }
end

report = {
  "version" => 1,
  "kind" => "target_capability_coverage_report",
  "generated_from" => ["docs/target-capability-catalog.yaml", "docs/capability-inventory.yaml"],
  "semantics" => {
    "current_status" => "Aggregated from linked current inventory capabilities; this report does not change inventory status.",
    "current_surfaces" => "Detected only from current inventory web/backend evidence and Vision capability IDs.",
    "gaps" => "Missing current mapping or missing required web UI/Vision surface evidence."
  },
  "summary" => {
    "total" => rows.length,
    "mapped" => rows.count { |row| row["current_status"] != "unmapped" },
    "unmapped" => rows.count { |row| row["current_status"] == "unmapped" },
    "exact_mapped" => rows.count { |row| row["mapping_quality"] == "exact" },
    "broad_mapped" => rows.count { |row| row["mapping_quality"] == "broad" },
    "decomposition_required" => rows.count { |row| row["gaps"].include?("decomposition_required") },
    "web_ui_missing" => rows.count { |row| row["gaps"].include?("web_ui") },
    "vision_missing" => rows.count { |row| row["gaps"].include?("vision") },
    "not_ready" => rows.count { |row| !row["gaps"].empty? || !%w[implemented verified].include?(row["current_status"]) }
  },
  "capabilities" => rows
}

puts YAML.dump(report)
