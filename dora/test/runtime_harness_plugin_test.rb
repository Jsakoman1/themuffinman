#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugins/runtime_harness"
Dir.mktmpdir("dora-runtime-harness") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml"); beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_runtime_trace", "version" => 1, "command" => "alpha run", "evidence_path" => "evidence/a.json", "viewport" => "desktop"}))
  File.write(beta, YAML.dump({"kind" => "dora_runtime_trace", "version" => 1, "command" => "beta run", "evidence_path" => "trace/b.json", "viewport" => "mobile"}))
  abort "alpha runtime contract failed" unless Dora::Plugins::RuntimeHarness.contract!(alpha).fetch("viewport") == "desktop"
  abort "beta runtime contract failed" unless Dora::Plugins::RuntimeHarness.contract!(beta).fetch("viewport") == "mobile"
end
puts "Dora runtime harness plugin test passed (two project contracts)."
