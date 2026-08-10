#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/discovery_skeleton"

def answer(id, value)
  {"id" => id, "value" => value, "source" => "user_confirmed"}
end

session = Dora::IdeaInterviewSession.new!(project_id: "home-stock").merge(
  "answers" => [answer("target_users", ["Household members"]), answer("first_problem", "Supplies are forgotten"), answer("first_capability", "Record stock"), answer("forbidden_outcomes", "Do not auto-order goods")],
  "open_decisions" => [{"id" => "retention", "question" => "How long is history retained?", "source" => "user_confirmed", "status" => "open"}]
)
framing = {"kind" => "dora_discovery_owner_framing_gate", "read_only" => true, "disposition" => "advisory", "source_references" => ["idea-interview.yaml#answers"], "payload" => {"sufficient_for_proposal" => true, "collaboration_scope" => "shared"}}
areas = [{"id" => "routine-stock", "title" => "Routine stock visibility", "source_references" => ["idea-interview.yaml#first-capability"]}, {"id" => "household-access", "title" => "Household access", "source_references" => ["idea-interview.yaml#target-users"]}]
session_before = Marshal.dump(session)
skeleton = Dora::DiscoverySkeleton.build!(session: session, framing: framing, product_areas: areas, observed_at: "2026-08-10T13:10:00Z")

abort "skeleton is not advisory provenance" unless skeleton.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_skeleton", "read_only" => true, "disposition" => "advisory"}
abort "skeleton did not preserve first delivery" unless skeleton.dig("payload", "first_delivery", "capability") == "Record stock"
abort "skeleton did not preserve exclusions" unless skeleton.dig("payload", "first_delivery", "intentional_exclusions") == "Do not auto-order goods"
abort "skeleton invented or lost product areas" unless skeleton.dig("payload", "product_areas").map { |area| area.fetch("id") } == %w[household-access routine-stock]
abort "skeleton did not retain explicit open questions" unless skeleton.dig("payload", "unresolved_questions").map { |item| item.fetch("id") } == ["retention"]
abort "skeleton did not derive collaboration coverage" unless skeleton.dig("payload", "foundation_choice_coverage", 0, "value") == "shared"
abort "skeleton mutated the session" unless Marshal.dump(session) == session_before
abort "skeleton exposed lifecycle status" if skeleton.key?("status") || skeleton.dig("payload", "status")
abort "skeleton boundary lacks canonical-write protection" unless skeleton.dig("payload", "completion_boundary").include?("does not write ProductBrief")

source = File.read(File.expand_path("../lib/dora/discovery_skeleton.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open].each do |forbidden_surface|
  abort "skeleton exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end

puts "Dora discovery skeleton test passed (cited outline, explicit exclusions, and no canonical mutation)."
