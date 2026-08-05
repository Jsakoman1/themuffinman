#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../lib/dora/task_lease"

NOW = Time.utc(2026, 8, 5, 17, 0, 0)

def lease(id:, holder:, state: "active", task: "write-note", expires_at: "2026-08-05T17:30:00Z", handoff: nil)
  {"id" => id, "work_plan" => "docs/work/note.yaml", "task" => task, "holder" => holder, "acquired_at" => "2026-08-05T16:30:00Z", "expires_at" => expires_at, "state" => state, "handoff" => handoff}.compact
end

def registry(leases)
  {"kind" => "dora_task_lease_registry", "version" => 1, "leases" => leases}
end

active = lease(id: "lease-note", holder: "alpha")
result = Dora::TaskLease.validate!(registry([active]), now: NOW)
abort "active lease lost authority boundary" unless result.fetch("leases").first.fetch("holder") == "alpha" && result.fetch("authority_boundary").include?("do not start work")

handoff = lease(id: "lease-note", holder: "beta", state: "handed_off", handoff: {"from" => "alpha", "to" => "beta", "at" => "2026-08-05T16:45:00Z", "reason" => "Alpha paused the focused task."})
result = Dora::TaskLease.validate!(registry([handoff]), now: NOW)
abort "explicit handoff was not retained" unless result.fetch("leases").first.dig("handoff", "from") == "alpha"

begin
  Dora::TaskLease.validate!(registry([active, lease(id: "lease-note-two", holder: "beta")]), now: NOW)
  abort "conflicting active lease passed"
rescue ArgumentError => error
  abort "wrong conflict rejection: #{error.message}" unless error.message.include?("conflict")
end

begin
  Dora::TaskLease.validate!(registry([lease(id: "expired", holder: "alpha", expires_at: "2026-08-05T16:59:00Z")]), now: NOW)
  abort "expired active lease passed"
rescue ArgumentError => error
  abort "wrong expiry rejection: #{error.message}" unless error.message.include?("expired")
end

puts "Dora task lease test passed (advisory ownership, conflict rejection, expiry, and explicit handoff)."
