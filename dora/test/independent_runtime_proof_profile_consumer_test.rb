#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/runtime-proof-profile-consumer.yaml"))
Dir.mktmpdir("dora-runtime-proof-consumer") do |consumer|
  output, status = Open3.capture2e(CLI, "runtime-profile-apply", fixture.fetch("destination"), "--apply", chdir: consumer)
  abort "independent consumer apply failed: #{output}" unless status.success?
  profile = File.join(consumer, fixture.fetch("destination"))
  fixture.fetch("expected_files").each { |relative| abort "consumer missing #{relative}" unless File.file?(File.join(profile, relative)) }
  package = File.read(File.join(profile, "package.json"))
  spec = File.read(File.join(profile, "tests/technical-health.spec.mjs"))
  abort "consumer did not receive the declared runtime command" unless package.include?("test:runtime")
  abort "consumer profile leaked product behavior" if [package, spec].join("\n").match?(/inventory|supply|seed|production|user/i)
  abort "consumer profile started a browser" if output.match?(/playwright install|browser started/i)
end
puts "Dora independent runtime proof profile consumer test passed (fresh consumer receives declared neutral assets without browser execution or product leakage)."
