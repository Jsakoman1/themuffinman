#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/voice_evaluation"

fixture = {"kind" => "dora_voice_evaluation", "version" => 1, "fixture_id" => "clarify-ambiguous-request", "stages" => [{"id" => "transcription", "outcome" => "passed", "observation" => "Fixture transcript contains ambiguity."}, {"id" => "semantic_interpretation", "outcome" => "passed", "observation" => "Fixture produces a candidate with uncertainty."}, {"id" => "deterministic_validation", "outcome" => "rejected", "observation" => "Missing required argument is rejected."}, {"id" => "review", "outcome" => "passed", "observation" => "Candidate remains visible for review."}, {"id" => "confirmation", "outcome" => "blocked", "observation" => "No confirmation is supplied."}, {"id" => "execution", "outcome" => "skipped", "observation" => "No adapter is invoked."}]}
record = Dora::VoiceEvaluation.record!(fixture)
abort "voice evaluation did not retain per-stage result" unless record.fetch("stages").map { |stage| stage.fetch("outcome") } == %w[passed passed rejected passed blocked skipped]
abort "voice evaluation invoked execution" unless record.fetch("execution") == "not invoked"
begin
  Dora::VoiceEvaluation.record!(fixture.merge("api_key" => "forbidden"))
  abort "voice evaluation accepted credentials"
rescue ArgumentError
end

puts "Dora voice evaluation test passed (credential-free fixture stages with no execution adapter)."
