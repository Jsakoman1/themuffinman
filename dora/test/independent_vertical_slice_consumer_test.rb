#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/vertical_slice_generator"
require_relative "../lib/dora/vertical_slice_readiness"

ROOT = File.expand_path("..", __dir__)
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/vertical-slice-consumers.yaml"))
abort "vertical-slice consumer fixture is invalid" unless fixture["kind"] == "dora_vertical_slice_consumers" && fixture["version"] == 1
ids = fixture.fetch("consumers").map { |consumer| consumer.dig("capability", "id") }

Dir.mktmpdir("dora-vertical-slice-consumers") do |sandbox|
  fixture.fetch("consumers").each do |consumer|
    root = File.join(sandbox, consumer.fetch("id")); FileUtils.mkdir_p(File.join(root, ".dora"))
    context = {"kind" => "dora_confirmed_capability_context", "version" => 1, "capability" => consumer.fetch("capability"), "decisions" => consumer.fetch("decisions")}
    proposal = Dora::VerticalSliceGenerator.generate!(context)
    readiness = Dora::VerticalSliceReadiness.evaluate!(proposal)
    File.write(File.join(root, ".dora/vertical-slice-proposal.yaml"), YAML.dump(proposal))
    File.write(File.join(root, ".dora/vertical-slice-readiness.yaml"), YAML.dump(readiness))

    stored = YAML.load_file(File.join(root, ".dora/vertical-slice-proposal.yaml"))
    own_id = consumer.dig("capability", "id")
    abort "consumer proposal lost its own capability" unless stored.dig("capability", "id") == own_id
    other_ids = ids - [own_id]
    abort "consumer proposal leaked another project capability" if other_ids.any? { |other_id| stored.to_s.include?(other_id) }
    abort "consumer proposal invented product implementation" if stored.to_s.match?(/@Entity|create table|source_code/i)
    abort "consumer readiness is wrong" unless readiness.fetch("ready_to_plan") == consumer.fetch("expected_ready")
    categories = readiness.fetch("blocking_gaps").map { |gap| gap.fetch("category") }
    abort "consumer blockers are wrong" unless categories.sort == consumer.fetch("expected_blockers").sort
  end
end

puts "Dora independent vertical-slice consumer test passed (two projects receive isolated proposals and explicit blockers without product inference)."
