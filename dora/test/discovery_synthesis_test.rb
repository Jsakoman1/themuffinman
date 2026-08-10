#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/discovery_synthesis"

sources = [{"kind" => "dora_discovery_skeleton", "read_only" => true, "disposition" => "advisory", "source_references" => ["idea.yaml#first-capability"]}]
claims = [
  {"id" => "routine-confirmed", "topic" => "routine-flow", "classification" => "confirmed", "statement" => "Routine stock updates are the first delivery.", "source_references" => ["idea.yaml#first-capability"]},
  {"id" => "routine-research", "topic" => "routine-flow", "classification" => "external_research", "statement" => "Routine stock updates are the first delivery.", "source_references" => ["research.md#pattern"]},
  {"id" => "history-one", "topic" => "history", "classification" => "assumption", "statement" => "Keep stock history indefinitely.", "source_references" => ["idea.yaml#retention"]},
  {"id" => "history-two", "topic" => "history", "classification" => "open_question", "statement" => "Retain stock history for one year.", "source_references" => ["idea.yaml#retention"]}
]
before = Marshal.dump(claims)
result = Dora::DiscoverySynthesis.build!(sources: sources, claims: claims, observed_at: "2026-08-10T13:30:00Z")

abort "synthesis is not advisory" unless result.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_synthesis", "read_only" => true, "disposition" => "advisory"}
abort "synthesis lost deterministic agreement" unless result.dig("payload", "agreement").map { |item| item.fetch("topic") } == ["routine-flow"]
abort "synthesis did not surface conflict" unless result.dig("payload", "conflicts").map { |item| item.fetch("topic") } == ["history"]
abort "synthesis did not turn conflict into owner question" unless result.dig("payload", "owner_questions", 0, "id") == "resolve-history"
abort "synthesis mutated claims" unless Marshal.dump(claims) == before
abort "synthesis exposed a status" if result.key?("status") || result.dig("payload", "status")
abort "synthesis boundary permits conflict resolution" unless result.dig("payload", "completion_boundary").include?("cannot resolve a conflict")

source = File.read(File.expand_path("../lib/dora/discovery_synthesis.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open].each { |forbidden| abort "synthesis exposes #{forbidden}" if source.include?(forbidden) }

puts "Dora discovery synthesis test passed (deterministic conflicts, owner questions, and no authority mutation)."
