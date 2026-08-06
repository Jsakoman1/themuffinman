#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/guided_agent_entrypoint"

ROOT = File.expand_path("..", __dir__)
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/guided-agent-entrypoint.yaml"))

Dir.mktmpdir("dora-guided-entrypoint-consumer") do |sandbox|
  %w[incomplete complete].each do |state|
    consumer = File.join(sandbox, state)
    FileUtils.mkdir_p(File.join(consumer, ".dora"))
    input_path = File.join(consumer, ".dora", "guided-entrypoint.yaml")
    File.write(input_path, YAML.dump(fixture.fetch(state)))
    result = Dora::GuidedAgentEntrypoint.resolve!(YAML.load_file(input_path))
    stored = YAML.load_file(input_path)

    abort "consumer input changed during read-only resolution" unless stored == fixture.fetch(state)
    abort "consumer result leaked another fixture state" if result.to_s.include?(state == "incomplete" ? "review_create_app_handoff" : "ask_one_confirmed_question")
    if state == "incomplete"
      abort "incomplete consumer did not stop at one question" unless result.fetch("resolution") == "ask_one_confirmed_question" && result.dig("question", "id") == "target_users"
    else
      abort "complete consumer did not return a review handoff" unless result.fetch("resolution") == "review_create_app_handoff" && result.dig("handoff", "first_work") == "declare-first-capability"
      abort "complete consumer lost open decision" unless result.fetch("open_decisions").first.fetch("id") == "offline-mode"
    end
    abort "consumer result claimed implementation" if result.to_s.match?(/implemented|verified|accepted/i)
  end
end

puts "Dora independent guided agent entrypoint test passed (fresh incomplete and complete consumers receive isolated read-only next actions)."
