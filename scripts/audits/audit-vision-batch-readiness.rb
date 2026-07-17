#!/usr/bin/env ruby

require "yaml"

coverage = YAML.load_file("docs/work/target-capability-coverage-report.yaml")
backlog = YAML.load_file("docs/work/vision-gap-prioritized-backlog.yaml")
readiness = YAML.load_file("docs/work/vision-gap-readiness.yaml")

vision_gaps = coverage.fetch("capabilities")
                  .select { |capability| capability.dig("surface_coverage", "vision") == "missing" }
                  .map { |capability| capability.fetch("id") }
                  .sort
backlog_ids = backlog.fetch("priorities").flat_map { |priority| priority.fetch("capability_ids") }.sort
gate_ids = readiness.fetch("gates").flat_map { |gate| gate.fetch("capability_ids") }.sort

abort "Vision coverage and backlog are out of sync" unless vision_gaps == backlog_ids
abort "Vision backlog and readiness gates are out of sync" unless vision_gaps == gate_ids
abort "Backlog target_gap_count is stale" unless backlog.dig("scope", "target_gap_count") == vision_gaps.length
abort "Readiness deferred count is stale" unless readiness.dig("summary", "deferred_capability_count") == vision_gaps.length

gaps_by_id = vision_gaps.to_h { |id| [id, true] }
ready = []
blocked = []
readiness.fetch("gates").each do |gate|
  missing_ids = gate.fetch("capability_ids").select { |id| gaps_by_id[id] }
  if missing_ids.empty? || ["contract_resolved", "implementation_ready"].include?(gate.fetch("status"))
    ready << {
      "id" => gate.fetch("id"),
      "capability_ids" => missing_ids,
      "contract_status" => gate.fetch("status"),
      "implementation_status" => gate.fetch("implementation_status", "pending")
    }
  else
    blocked << {
      "id" => gate.fetch("id"),
      "capability_ids" => missing_ids,
      "blocker_class" => gate.fetch("blocker_class"),
      "status" => gate.fetch("status")
    }
  end
end

puts YAML.dump(
  "kind" => "vision_batch_readiness_report",
  "vision_gap_count" => vision_gaps.length,
  "ready_gate_count" => ready.length,
  "contract_ready_gate_count" => ready.count { |gate| gate.fetch("contract_status") == "contract_resolved" },
  "implementation_ready_gate_count" => ready.count { |gate| gate.fetch("contract_status") == "implementation_ready" },
  "blocked_gate_count" => blocked.length,
  "ready_gates" => ready,
  "blocked_gates" => blocked,
  "next_action" => ready.empty? ? "Resolve a product, backend, or device contract before implementation." : "Promote a contract-ready gate into an implementation work plan."
)
