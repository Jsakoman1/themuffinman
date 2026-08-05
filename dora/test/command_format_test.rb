#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

text, status = Open3.capture2e(CLI, "help", chdir: ROOT)
abort "default help changed unexpectedly" unless status.success? && text.include?("Dora commands:")
json, status = Open3.capture2e(CLI, "help", "--format", "json", chdir: ROOT)
envelope = JSON.parse(json)
abort "JSON help envelope is invalid" unless status.success? && envelope.values_at("kind", "outcome", "side_effect") == ["dora_command_envelope", "success", "read_only"]

Dir.mktmpdir("dora-command-format") do |sandbox|
  yaml, status = Open3.capture2e(CLI, "readiness", sandbox, "--format", "yaml", chdir: ROOT)
  envelope = YAML.safe_load(yaml)
  abort "YAML readiness envelope is invalid" unless status.success? && envelope.dig("payload", "state") == "missing_git_repository"
end

error, status = Open3.capture2e(CLI, "help", "--format", "xml", chdir: ROOT)
abort "invalid format did not return a machine-readable error" if status.success? || !error.include?("--format must be json or yaml")

puts "Dora command format test passed (explicit JSON/YAML envelopes preserve default output)."
