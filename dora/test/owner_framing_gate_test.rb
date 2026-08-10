#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/owner_framing_gate"

def answer(id, value)
  {"id" => id, "value" => value, "source" => "user_confirmed"}
end

ledger = Dora::DiscoveryAssumptionLedger.build!(
  statements: [{"id" => "first-direction", "classification" => "confirmed", "statement" => "Initial direction is owner-confirmed.", "source_references" => ["idea-interview.yaml#answers"]}],
  observed_at: "2026-08-10T12:50:00Z"
)
session = Dora::IdeaInterviewSession.new!(project_id: "home-stock").merge("answers" => [
  answer("target_users", "Household members"),
  answer("first_problem", "Supplies are forgotten"),
  answer("first_capability", "Record stock"),
  answer("forbidden_outcomes", "Do not auto-order supplies")
])
session_before = Marshal.dump(session)
ledger_before = Marshal.dump(ledger)

blocked = Dora::OwnerFramingGate.evaluate!(session: session, ledger: ledger, observed_at: "2026-08-10T12:51:00Z")
abort "gate is not advisory provenance" unless blocked.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_owner_framing_gate", "read_only" => true, "disposition" => "advisory"}
abort "gate did not expose the sole missing owner choice" unless blocked.dig("payload", "blocking_questions").map { |item| item.fetch("id") } == ["confirm-collaboration-scope"]
abort "blocked gate incorrectly declared sufficiency" if blocked.dig("payload", "sufficient_for_proposal")

sufficient = Dora::OwnerFramingGate.evaluate!(
  session: session,
  ledger: ledger,
  collaboration_scope: {"value" => "shared", "source" => "user_confirmed"},
  observed_at: "2026-08-10T12:52:00Z"
)
abort "sufficient gate did not remain a read-only result" unless sufficient.dig("payload", "sufficient_for_proposal") && sufficient.dig("payload", "blocking_questions") == []
abort "gate did not preserve user-confirmed scope" unless sufficient.dig("payload", "collaboration_scope") == "shared"
abort "gate mutated session or ledger" unless Marshal.dump(session) == session_before && Marshal.dump(ledger) == ledger_before
abort "gate exposed a lifecycle status" if sufficient.key?("status") || sufficient.dig("payload", "status")
abort "gate boundary lacks non-authority warning" unless sufficient.dig("payload", "completion_boundary").include?("not owner confirmation")

begin
  Dora::OwnerFramingGate.evaluate!(session: session, ledger: ledger, collaboration_scope: {"value" => "shared", "source" => "agent"})
  abort "gate accepted an inferred collaboration scope"
rescue ArgumentError
  # Expected.
end

source = File.read(File.expand_path("../lib/dora/owner_framing_gate.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open DecisionLog. WorkExecution. HandoffRunner.].each do |forbidden_surface|
  abort "owner framing gate exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end

puts "Dora owner framing gate test passed (explicit blocker, user-confirmed scope, and no authority mutation)."
