#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/session_create_app"

answers = Dora::IdeaInterview::REQUIRED_ANSWERS.map { |id| {"id" => id, "value" => id == "first_capability" ? "Record supply" : "confirmed #{id}", "source" => "user_confirmed"} }
input = {"kind" => "dora_session_create_app", "version" => 1, "session" => {"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => "home-stock", "answers" => answers, "open_decisions" => [{"id" => "offline", "question" => "Offline?", "source" => "user_confirmed", "status" => "open"}], "completion_boundary" => "A guided session records confirmed answers and explicit open decisions only; it does not infer product rules or prove readiness."}, "dora_source" => {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "reviewed-dora", "ref" => "a" * 40, "checksum" => "b" * 64}, "review" => {"id" => "review", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}, "first_work" => {"id" => "declare-first", "title" => "Declare first capability", "observable_outcome" => "Declared context exists.", "required_paths" => ["docs/capability-package.yaml"], "validation" => "true", "evidence_boundary" => ["declared context"]}}
bundle = Dora::SessionCreateApp.convert!(input)
abort "capability title lost" unless bundle.dig("first_capability", "title") == "Record supply"
abort "open decision lost" unless bundle.dig("interview", "unanswered_decisions", 0, "id") == "offline"
incomplete = Marshal.load(Marshal.dump(input)); incomplete["session"]["answers"].pop
begin; Dora::SessionCreateApp.convert!(incomplete); abort "incomplete session accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("complete"); end
puts "Dora session create-app test passed (complete confirmed sessions convert to declared create-app input without resolving open decisions)."
