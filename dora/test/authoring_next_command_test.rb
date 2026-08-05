#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/idea_interview"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
Dir.mktmpdir("dora-authoring-next") do |sandbox|
  session = File.join(sandbox, "session.yaml")
  document = {"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => "home-stock", "answers" => [], "open_decisions" => [], "completion_boundary" => "A guided session records confirmed answers and explicit open decisions only; it does not infer product rules or prove readiness."}
  File.write(session, YAML.dump(document))
  output, status = Open3.capture2e(CLI, "authoring-next", session, "--format", "json", chdir: ROOT)
  abort "authoring next failed: #{output}" unless status.success?
  payload = JSON.parse(output).fetch("payload")
  abort "incomplete interview did not select its exact question" unless payload.dig("recommended_next_action", "id") == "answer_interview_question" && payload.dig("recommended_next_action", "reason").include?("target_users")
  document["open_decisions"] = [{"id" => "retention", "question" => "How long?", "source" => "user_confirmed", "status" => "open"}]
  document["answers"] = Dora::IdeaInterview::REQUIRED_ANSWERS.map { |id| {"id" => id, "value" => "confirmed #{id}", "source" => "user_confirmed"} }
  File.write(session, YAML.dump(document))
  output, status = Open3.capture2e(CLI, "authoring-next", session, "--format", "json", chdir: ROOT)
  abort "open decision report failed: #{output}" unless status.success? && JSON.parse(output).dig("payload", "recommended_next_action", "id") == "resolve_open_decision"
end
puts "Dora authoring next command test passed (one declared interview or decision action is selected without inference)."
