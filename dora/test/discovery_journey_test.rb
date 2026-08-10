#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/discovery_journey"

skeleton = {
  "kind" => "dora_discovery_skeleton", "read_only" => true, "disposition" => "advisory",
  "source_references" => ["idea-interview.yaml#answers"],
  "payload" => {"foundation_choice_coverage" => [{"id" => "collaboration-scope", "state" => "confirmed", "value" => "shared", "source_references" => ["idea-interview.yaml#answers"]}]}
}
journeys = [{"id" => "record-stock", "role" => "Household member", "trigger" => "A supply is put away", "outcome" => "The shared stock is current", "scenario_states" => %w[happy_path empty permission recovery], "source_references" => ["idea-interview.yaml#first-capability"]}]
before = Marshal.dump(journeys)
result = Dora::DiscoveryJourney.build!(skeleton: skeleton, journeys: journeys, observed_at: "2026-08-10T13:20:00Z")

abort "journey coverage is not advisory" unless result.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_journey_coverage", "read_only" => true, "disposition" => "advisory"}
abort "journey coverage lost states" unless result.dig("payload", "journeys", 0, "scenario_states") == %w[empty happy_path permission recovery]
abort "journey coverage lost foundation coverage" unless result.dig("payload", "foundation_choice_coverage", 0, "value") == "shared"
abort "journey coverage mutated input" unless Marshal.dump(journeys) == before
abort "journey coverage exposed status" if result.key?("status") || result.dig("payload", "status")
abort "journey boundary lacks evidence protection" unless result.dig("payload", "completion_boundary").include?("record evidence")

begin
  Dora::DiscoveryJourney.build!(skeleton: skeleton, journeys: [journeys.first.merge("scenario_states" => ["verified"])])
  abort "journey coverage accepted lifecycle state as scenario"
rescue ArgumentError
  # Expected.
end

source = File.read(File.expand_path("../lib/dora/discovery_journey.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open].each do |forbidden_surface|
  abort "journey coverage exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end

puts "Dora discovery journey test passed (human scenarios, derived coverage, and no evidence authority)."
