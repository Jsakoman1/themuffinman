#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/project_read_model"
require "yaml"

def summary(overrides = {})
  {
    "kind" => "dora_project_read_model",
    "version" => 1,
    "references" => [".dora/project.yaml", "docs/project-memory.yaml"],
    "delivery" => {"active" => nil, "latest_verified" => {"id" => "previous", "title" => "Previous delivery", "status" => "verified", "master_plan" => "docs/work/previous-master.yaml"}},
    "next_task" => nil,
    "open_decisions" => [{"id" => "owner-choice", "statement" => "Choose retention.", "source" => "decision_log", "reference" => "docs/decision-log.yaml"}
    ]
  }.merge(overrides)
end

active_summary = summary("delivery" => {"active" => {"id" => "delivery", "title" => "Current delivery", "status" => "draft", "master_plan" => "docs/work/current-master.yaml", "inventory" => "docs/work/current-inventory.yaml", "task" => {"task" => "implement-current"}}, "latest_verified" => nil})
before = Marshal.load(Marshal.dump(active_summary))
active = Dora::ProjectReadModel.current_work_index(summary: active_summary)
abort "current work index is not advisory provenance" unless active.fetch("read_only") && active.fetch("disposition") == "advisory" && active.fetch("observed_at").match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && active.fetch("source_references").include?("docs/project-memory.yaml")
abort "current work index mutated summary" unless active_summary == before
abort "active current work was not retained" unless active.dig("current_work", "state") == "active" && active.dig("current_work", "delivery", "id") == "delivery"
abort "current work index lost open decisions" unless active.fetch("open_decisions") == before.fetch("open_decisions")

canonical_goal = Dora::ProjectReadModel.current_work_index(summary: summary("current_goal" => {"state" => "blocked", "source" => "execution_inventory", "next_task" => {"id" => "blocked", "plan" => "docs/work/blocked.yaml", "task" => "resolve-blocker", "status" => "blocked"}}))
abort "current work index did not reuse the canonical blocked goal" unless canonical_goal.dig("current_work", "state") == "blocked" && canonical_goal.dig("current_work", "next_task", "task") == "resolve-blocker"

planned = Dora::ProjectReadModel.current_work_index(summary: summary("next_task" => {"id" => "next", "plan" => "docs/work/next.yaml", "task" => "implement-next", "status" => "pending"}))
abort "planned current work was not explicit" unless planned.dig("current_work", "state") == "planned" && planned.dig("next_action", "task") == "implement-next"

idle = Dora::ProjectReadModel.current_work_index(summary: summary)
abort "idle current work was inferred from history" unless idle.dig("current_work", "state") == "none" && idle.dig("latest_verified_delivery", "id") == "previous"

ambiguous = Dora::ProjectReadModel.current_work_index(summary: summary("delivery" => {"active" => {"status" => "ambiguous", "references" => ["docs/work/one-inventory.yaml", "docs/work/two-inventory.yaml"]}, "latest_verified" => nil}))
abort "ambiguous current work was inferred" unless ambiguous.dig("current_work", "state") == "ambiguous" && ambiguous.dig("current_work", "references") == ["docs/work/one-inventory.yaml", "docs/work/two-inventory.yaml"]

begin
  Dora::ProjectReadModel.current_work_index(summary: {"kind" => "unexpected"})
  abort "current work index accepted malformed summary"
rescue ArgumentError => error
  abort "wrong malformed summary rejection: #{error.message}" unless error.message.include?("invalid")
end

domain_library = YAML.load_file(File.expand_path("../docs/domain-library.yaml", __dir__))
abort "domain library omits current-work index" unless domain_library.fetch("vocabulary").any? { |item| item.fetch("id") == "current-work-index" && item.fetch("description").include?("ProjectReadModel summary") }
abort "domain library omits current-work index authority boundary" unless domain_library.fetch("invariants").any? { |item| item.fetch("id") == "current-work-index-authority-boundary" && item.fetch("description").include?("cannot mutate work status") }
abort "domain library omits Bridge context integrity" unless domain_library.fetch("vocabulary").any? { |item| item.fetch("id") == "bridge-context-integrity" && item.fetch("description").include?("ProjectReadModel") }
abort "domain library omits Bridge integrity precedence" unless domain_library.fetch("invariants").any? { |item| item.fetch("id") == "bridge-context-integrity-precedence" && item.fetch("description").include?("passing task evidence") }

puts "Dora current work index test passed (active, planned, idle, ambiguous, provenance, and no mutation)."
