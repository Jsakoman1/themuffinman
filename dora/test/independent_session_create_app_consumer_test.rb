#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"
require_relative "../lib/dora/idea_interview"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/session-create-app-consumer.yaml"))
answers = Dora::IdeaInterview::REQUIRED_ANSWERS.map { |id| {"id" => id, "value" => id == "first_capability" ? fixture.fetch("capability") : "confirmed #{id}", "source" => "user_confirmed"} }
Dir.mktmpdir("dora-session-consumer") do |root|
  input = {"kind" => "dora_session_create_app", "version" => 1, "session" => {"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => fixture.fetch("id"), "answers" => answers, "open_decisions" => [{"id" => "offline", "question" => fixture.fetch("open_decision"), "source" => "user_confirmed", "status" => "open"}], "completion_boundary" => "A guided session records confirmed answers and explicit open decisions only; it does not infer product rules or prove readiness."}, "dora_source" => {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => ROOT, "ref" => "a" * 40, "checksum" => Dora::BootstrapSource.send(:checksum_for, ROOT)}, "review" => {"id" => "fixture-review", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}, "first_work" => {"id" => "declare-first", "title" => "Declare first", "observable_outcome" => "Declared context exists.", "required_paths" => ["docs/capability-package.yaml"], "validation" => "true", "evidence_boundary" => ["declared context"]}}
  input_path = File.join(root, "input.yaml"); File.write(input_path, YAML.dump(input)); consumer = File.join(root, fixture.fetch("id"))
  output, status = Open3.capture2e(CLI, "session-create-app", consumer, "--input", input_path, "--apply", chdir: ROOT)
  abort "fresh consumer creation failed: #{output}" unless status.success?
  brief = YAML.load_file(File.join(consumer, "docs/product-brief.yaml"))
  abort "consumer capability context missing" unless brief.fetch("intended_outcomes") == [fixture.fetch("capability")]
  abort "consumer open decision was resolved" unless brief.fetch("unanswered_decisions").include?(fixture.fetch("open_decision"))
  abort "consumer generated product source" if File.exist?(File.join(consumer, "src/main/java/example/Supply.java"))
end
puts "Dora independent session create-app consumer test passed (fresh declared context preserves open decisions without product source)."
