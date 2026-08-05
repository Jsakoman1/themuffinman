#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"
require_relative "../lib/dora/idea_interview"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
answers = Dora::IdeaInterview::REQUIRED_ANSWERS.map { |id| {"id" => id, "value" => id == "first_capability" ? "Record supply" : "confirmed #{id}", "source" => "user_confirmed"} }
Dir.mktmpdir("dora-session-create-command") do |root|
  input = {"kind" => "dora_session_create_app", "version" => 1, "session" => {"kind" => "dora_idea_interview_session", "version" => 1, "project_id" => "home-stock", "answers" => answers, "open_decisions" => [], "completion_boundary" => "A guided session records confirmed answers and explicit open decisions only; it does not infer product rules or prove readiness."}, "dora_source" => {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => ROOT, "ref" => "a" * 40, "checksum" => Dora::BootstrapSource.send(:checksum_for, ROOT)}, "review" => {"id" => "review", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}, "first_work" => {"id" => "declare-first", "title" => "Declare first", "observable_outcome" => "Declared context exists.", "required_paths" => ["docs/capability-package.yaml"], "validation" => "true", "evidence_boundary" => ["declared context"]}}
  input_path = File.join(root, "input.yaml"); File.write(input_path, YAML.dump(input)); destination = File.join(root, "project")
  output, status = Open3.capture2e(CLI, "session-create-app", destination, "--input", input_path, "--preview", "--format", "json", chdir: ROOT)
  abort "preview failed: #{output}" unless status.success? && JSON.parse(output).dig("payload", "first_capability", "title") == "Record supply" && !File.exist?(destination)
  output, status = Open3.capture2e(CLI, "session-create-app", destination, "--input", input_path, "--apply", chdir: ROOT)
  abort "apply failed: #{output}" unless status.success? && File.file?(File.join(destination, "docs/product-brief.yaml"))
  _output, overwrite = Open3.capture2e(CLI, "session-create-app", destination, "--input", input_path, "--apply", chdir: ROOT)
  abort "existing destination was overwritten" if overwrite.success?
end
puts "Dora session create-app command test passed (preview and safe empty-destination creation use only complete confirmed sessions)."
