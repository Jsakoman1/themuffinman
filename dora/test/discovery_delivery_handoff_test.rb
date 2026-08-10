#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/discovery_delivery_handoff"

skeleton = {"kind" => "dora_discovery_skeleton", "read_only" => true, "disposition" => "advisory", "source_references" => ["idea.yaml#capability"]}
journeys = {"kind" => "dora_discovery_journey_coverage", "read_only" => true, "disposition" => "advisory", "source_references" => ["idea.yaml#journey"], "payload" => {"journeys" => [{"scenario_states" => %w[happy_path permission recovery]}]}}
selection = {"first_capability" => "Record stock", "cross_layer_obligations" => ["backend", "api", "ui", "tests", "docs"], "next_atomic_candidate" => "Declare stock mutation contract", "source_references" => ["decision-log.yaml#first-delivery"], "source" => "user_confirmed"}
result = Dora::DiscoveryDeliveryHandoff.build!(skeleton: skeleton, journeys: journeys, selection: selection, observed_at: "2026-08-10T13:40:00Z")

abort "delivery handoff is not advisory" unless result.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_delivery_handoff", "read_only" => true, "disposition" => "advisory"}
abort "delivery handoff lost selected capability" unless result.dig("payload", "first_capability") == "Record stock"
abort "delivery handoff lost scenarios" unless result.dig("payload", "scenario_states") == %w[happy_path permission recovery]
abort "delivery handoff exposed status" if result.key?("status") || result.dig("payload", "status")
abort "delivery handoff boundary is incomplete" unless result.dig("payload", "completion_boundary").include?("cannot activate or verify work")

begin
  Dora::DiscoveryDeliveryHandoff.build!(skeleton: skeleton, journeys: journeys, selection: selection.merge("source" => "agent"))
  abort "delivery handoff accepted inferred selection"
rescue ArgumentError
  # Expected.
end

source = File.read(File.expand_path("../lib/dora/discovery_delivery_handoff.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open].each { |forbidden| abort "delivery handoff exposes #{forbidden}" if source.include?(forbidden) }
puts "Dora discovery delivery handoff test passed (owner-selected advisory handoff and no activation authority)."
