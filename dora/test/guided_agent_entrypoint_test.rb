#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/guided_agent_entrypoint"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/guided-agent-entrypoint.yaml"))

incomplete = Dora::GuidedAgentEntrypoint.resolve!(fixture.fetch("incomplete"))
abort "guided entrypoint did not select one question" unless incomplete.fetch("resolution") == "ask_one_confirmed_question" && incomplete.dig("question", "id") == "target_users"
abort "guided entrypoint exposed a handoff before interview completion" if incomplete.key?("handoff")

complete = Dora::GuidedAgentEntrypoint.resolve!(fixture.fetch("complete"))
abort "guided entrypoint did not select review handoff" unless complete.fetch("resolution") == "review_create_app_handoff" && complete.dig("handoff", "starter") == "spring-vue-postgres-buildable"
abort "guided entrypoint lost explicit decision" unless complete.fetch("open_decisions").first.fetch("id") == "offline-mode"

invalid = Marshal.load(Marshal.dump(fixture.fetch("complete")))
invalid.fetch("handoff").delete("starter")
begin
  Dora::GuidedAgentEntrypoint.resolve!(invalid)
  abort "guided entrypoint accepted missing handoff field"
rescue ArgumentError => error
  abort error.message unless error.message.include?("starter")
end

Dir.mktmpdir("dora-guided-next") do |root|
  path = File.join(root, "entrypoint.yaml")
  File.write(path, YAML.dump(fixture.fetch("incomplete")))
  output, status = Open3.capture2e(CLI, "guided-next", path, "--format", "json", chdir: ROOT)
  abort "guided-next command failed: #{output}" unless status.success?
  envelope = JSON.parse(output)
  abort "guided-next command did not return one cited question" unless envelope.dig("payload", "resolution") == "ask_one_confirmed_question" && envelope.fetch("citations") == ["entrypoint.yaml"]
end

puts "Dora guided agent entrypoint test passed (one confirmed question or cited review-only handoff without inferred product behavior)."
