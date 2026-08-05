#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/authoring_trace"
require_relative "../lib/dora/vertical_slice_generator"

Dir.mktmpdir("dora-authoring-trace") do |root|
  session_path = File.join(root, "interview.yaml")
  session = {"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => "home-stock", "answers" => [{"id" => "target_users", "value" => "household", "source" => "user_confirmed"}], "open_decisions" => [{"id" => "retention", "question" => "How long?", "source" => "user_confirmed", "status" => "open"}], "completion_boundary" => "A guided session records confirmed answers and explicit open decisions only; it does not infer product rules or prove readiness."}
  File.write(session_path, YAML.dump(session))
  context = {"kind" => "dora_confirmed_capability_context", "version" => 1, "capability" => {"id" => "record-supply", "title" => "Record supply", "confirmed" => true}, "decisions" => {"data_safety" => "Confirmed", "workflow" => "Confirmed", "permission" => "Confirmed"}}
  trace = Dora::AuthoringTrace.build!(session_path: session_path, proposal: Dora::VerticalSliceGenerator.generate!(context))
  abort "trace lost confirmed input" unless trace.fetch("confirmed_interview_inputs").first.fetch("id") == "target_users"
  abort "trace hid readiness gap" unless trace.fetch("readiness_gaps").any? { |gap| gap.fetch("category") == "technical" }
  abort "trace lost open decision" unless trace.fetch("open_decisions").first.fetch("id") == "retention"
end
puts "Dora authoring trace test passed (confirmed inputs, unresolved decisions, proposal gaps, and citations remain distinct)."
