#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/discovery_research_packet"

framing = {
  "kind" => "dora_discovery_owner_framing_gate", "read_only" => true, "disposition" => "advisory",
  "source_references" => ["idea-interview.yaml#answers"],
  "payload" => {"sufficient_for_proposal" => true, "confirmed_direction_answer_ids" => %w[target_users first_problem first_capability forbidden_outcomes]}
}
consent = {"topic" => "household inventory patterns", "disclosure_level" => "public_summary_redacted", "allowed_sources" => ["public-web"], "source" => "user_confirmed"}
questions = [{"id" => "patterns", "question" => "Which routine inventory patterns reduce household friction?"}]
packet = Dora::DiscoveryResearchPacket.render!(framing: framing, consent: consent, questions: questions, observed_at: "2026-08-10T13:00:00Z")

abort "research packet is not advisory" unless packet.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_research_packet", "read_only" => true, "disposition" => "advisory"}
abort "research packet lost confirmed consent" unless packet.dig("payload", "consent", "source") == "user_confirmed"
abort "research packet prompt is not copyable" unless packet.dig("payload", "copyable_prompt").include?("Separate facts, inferences, and speculation")
abort "research packet exposed status" if packet.key?("status") || packet.dig("payload", "status")

response = Dora::DiscoveryResearchPacket.validate_pasted_response!(
  packet: packet,
  response: {
    "facts" => [{"statement" => "Comparable apps show routine stock separately from administration.", "source_references" => ["research/example#routine"]}],
    "inferences" => [{"statement" => "A routine-first view may reduce friction.", "source_references" => ["research/example#routine"]}],
    "speculation" => [{"statement" => "A shopping preview may be a differentiator.", "source_references" => ["research/example#routine"]}],
    "source_references" => ["research/example"]
  },
  observed_at: "2026-08-10T13:01:00Z"
)
abort "pasted response is not advisory" unless response.slice("kind", "read_only", "disposition") == {"kind" => "dora_discovery_research_response", "read_only" => true, "disposition" => "advisory"}
abort "pasted response changed claim classes" unless response.dig("payload", "claims").keys.sort == %w[facts inferences speculation]
abort "pasted response boundary lacks decision protection" unless response.dig("payload", "completion_boundary").include?("cannot create or amend an owner decision")

begin
  Dora::DiscoveryResearchPacket.render!(framing: framing, consent: consent.merge("source" => "agent"), questions: questions)
  abort "research packet accepted inferred consent"
rescue ArgumentError
  # Expected.
end

source = File.read(File.expand_path("../lib/dora/discovery_research_packet.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open].each do |forbidden_surface|
  abort "research packet exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end
abort "research packet exposes a direct service client" if source.match?(/ChatGPT|web_search|connector/i)

puts "Dora discovery research packet test passed (owner consent, pasted advisory response, and no external authority)."
