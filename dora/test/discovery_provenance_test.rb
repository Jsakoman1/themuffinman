#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"

require_relative "../lib/dora/discovery_provenance"

input = {"hypotheses" => ["Household members need a shared stock view."], "confidence" => "assumption"}
before = Marshal.dump(input)
result = Dora::DiscoveryProvenance.advisory!(
  kind: "dora_discovery_direction",
  source_references: ["docs/domain-library.yaml#household", "docs/product-brief.yaml#problem"],
  observed_at: "2026-08-10T12:00:00Z",
  payload: input
)

expected_keys = %w[authority_boundary disposition kind observed_at payload read_only source_references version]
abort "discovery provenance result keys are wrong" unless result.keys.sort == expected_keys
abort "discovery provenance metadata is wrong" unless result.slice("kind", "version", "observed_at", "read_only", "disposition") == {
  "kind" => "dora_discovery_direction", "version" => 1, "observed_at" => "2026-08-10T12:00:00Z", "read_only" => true, "disposition" => "advisory"
}
abort "discovery provenance did not normalize references" unless result.fetch("source_references") == ["docs/domain-library.yaml#household", "docs/product-brief.yaml#problem"]
abort "discovery provenance payload was not copied" unless result.fetch("payload") == input && !result.fetch("payload").equal?(input)
abort "discovery provenance input was mutated" unless Marshal.dump(input) == before
input.fetch("hypotheses") << "A later mutation must not change the advisory result."
abort "discovery provenance retained a mutable caller payload" unless result.dig("payload", "hypotheses").length == 1
abort "discovery provenance result must be immutable" unless result.frozen? && result.fetch("payload").frozen?

%w[status decision inventory work execution github consumer runner agent remote].each do |forbidden|
  abort "authority boundary omits #{forbidden}" unless result.fetch("authority_boundary").downcase.include?(forbidden)
end

[
  {kind: "wrong", source_references: ["docs/source.yaml"], observed_at: "2026-08-10T12:00:00Z", payload: {}},
  {kind: "dora_discovery_direction", source_references: [], observed_at: "2026-08-10T12:00:00Z", payload: {}},
  {kind: "dora_discovery_direction", source_references: ["../private.yaml"], observed_at: "2026-08-10T12:00:00Z", payload: {}},
  {kind: "dora_discovery_direction", source_references: ["docs/source.yaml", "docs/source.yaml"], observed_at: "2026-08-10T12:00:00Z", payload: {}},
  {kind: "dora_discovery_direction", source_references: ["docs/source.yaml"], observed_at: "2026-08-10T13:00:00+01:00", payload: {}},
  {kind: "dora_discovery_direction", source_references: ["docs/source.yaml"], observed_at: "not-a-time", payload: {Object.new => "invalid"}}
].each do |invalid|
  begin
    Dora::DiscoveryProvenance.advisory!(**invalid)
    abort "invalid discovery provenance input was accepted: #{invalid.inspect}"
  rescue ArgumentError
    # Expected: invalid input must fail closed before any projection exists.
  end
end

source = File.read(File.expand_path("../lib/dora/discovery_provenance.rb", __dir__))
%w[File.write Open3 system Net::HTTP URI.open DecisionLog WorkExecution HandoffRunner].each do |forbidden_surface|
  abort "discovery provenance exposes forbidden authority surface #{forbidden_surface}" if source.include?(forbidden_surface)
end

abort "test unexpectedly changed its input fixture" unless Digest::SHA256.hexdigest(Marshal.dump({"hypotheses" => ["Household members need a shared stock view."], "confidence" => "assumption"})) == Digest::SHA256.hexdigest(before)

puts "Dora discovery provenance test passed (cited advisory envelope, immutable payload, and no authority surface)."
