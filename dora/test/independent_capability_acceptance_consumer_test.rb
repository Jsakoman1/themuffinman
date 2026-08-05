#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/capability_acceptance"

ROOT = File.expand_path("..", __dir__)
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/capability-acceptance-consumer.yaml"))

Dir.mktmpdir("dora-capability-acceptance-consumer") do |root|
  result = Dora::CapabilityAcceptance.declare!(fixture)
  output_path = File.join(root, "docs/capability-acceptance-obligations.yaml")
  Dir.mkdir(File.join(root, "docs"))
  File.write(output_path, YAML.dump(result))
  persisted = YAML.load_file(output_path)
  abort "consumer did not receive obligations" unless persisted.fetch("obligations").length == 3
  abort "consumer acceptance was fabricated" unless persisted.fetch("acceptance_status") == "unresolved" && persisted.fetch("obligations").all? { |obligation| obligation.fetch("status") == "unresolved" }
end

puts "Dora independent capability acceptance consumer test passed (a fresh consumer receives unresolved obligations rather than fabricated evidence)."
