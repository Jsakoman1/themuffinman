#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/implementation_quality_profile"
require "yaml"

profile = {"kind" => "dora_implementation_quality_profile", "version" => 1, "id" => "spring-vue-api", "source_references" => ["docs/domain-library.yaml#quality"], "obligations" => %w[permission schema api ui test documentation runtime], "exclusions" => []}
before = Marshal.load(Marshal.dump(profile))
projection = Dora::ImplementationQualityProfile.project!(profile: profile)
abort "quality profile is not advisory provenance" unless projection.fetch("read_only") && projection.fetch("disposition") == "advisory" && projection.fetch("observed_at").match?(/\A\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z\z/) && projection.fetch("source_references") == ["docs/domain-library.yaml#quality"]
abort "quality profile mutated its declaration" unless profile == before
abort "quality profile lost declared obligations" unless projection.fetch("obligations") == Dora::ImplementationQualityProfile::OBLIGATION_CLASSES.sort
abort "quality profile invented evidence state" if projection.to_s.match?(/verified|recorded|passed/)

overlap = Marshal.load(Marshal.dump(profile)); overlap["exclusions"] = ["runtime"]
begin
  Dora::ImplementationQualityProfile.project!(profile: overlap)
  abort "quality profile accepted an implicit exception"
rescue ArgumentError => error
  abort "wrong overlap rejection: #{error.message}" unless error.message.include?("overlaps")
end

unknown = Marshal.load(Marshal.dump(profile)); unknown["obligations"] << "security"
begin
  Dora::ImplementationQualityProfile.project!(profile: unknown)
  abort "quality profile accepted an unknown obligation"
rescue ArgumentError => error
  abort "wrong unknown-obligation rejection: #{error.message}" unless error.message.include?("must be a list")
end

domain_library = YAML.load_file(File.expand_path("../docs/domain-library.yaml", __dir__))
abort "domain library omits implementation quality profile" unless domain_library.fetch("vocabulary").any? { |item| item.fetch("id") == "implementation-quality-profile" && item.fetch("description").include?("opt-in static") }
abort "domain library omits profile static boundary" unless domain_library.fetch("invariants").any? { |item| item.fetch("id") == "implementation-quality-profile-static-boundary" && item.fetch("description").include?("cannot execute tests") }

puts "Dora implementation quality profile test passed (opt-in obligations, explicit exclusions, provenance, and no evidence state)."
