#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

def answer(id, value)
  {"id" => id, "value" => value, "source" => "user"}
end

def checksum_for(root)
  entries = Dir[File.join(root, "**/*")].select { |path| File.file?(path) && !path.delete_prefix("#{root}/").split("/").include?(".git") }.sort
  Digest::SHA256.hexdigest(entries.map { |path| "#{path.delete_prefix("#{root}/")}\0#{Digest::SHA256.file(path).hexdigest}" }.join("\n"))
end

Dir.mktmpdir("dora-create-app") do |temporary|
  source = File.join(temporary, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => "garden-journal", "answers" => [answer("target_users", ["member"]), answer("first_problem", "Notes are lost."), answer("first_capability", "Record note."), answer("domain_concepts", ["note"]), answer("permission_intent", "Member records own group note."), answer("workflow_intent", "Draft becomes recorded."), answer("forbidden_outcomes", ["No public notes."])], "unanswered_decisions" => [{"id" => "retention", "question" => "How long are notes retained?", "source" => "user"}]}
  bundle = {"kind" => "dora_create_app", "version" => 1, "interview" => interview, "dora_source" => {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "reviewed-dora", "ref" => "a" * 40, "checksum" => checksum_for(source)}, "review" => {"id" => "review-1", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}, "first_capability" => {"id" => "record-note", "title" => "Record a note", "interview_answer" => "first_capability"}, "first_work" => {"id" => "document-note", "title" => "Document note", "observable_outcome" => "A note boundary is declared.", "required_paths" => ["docs/note.md"], "validation" => "ruby test/note_test.rb", "evidence_boundary" => ["declared work fixture"]}, "starter" => "spring-vue-buildable", "codex_integration" => true}
  bundle_path = File.join(temporary, "create-app.yaml")
  File.write(bundle_path, YAML.dump(bundle))
  project = File.join(temporary, "garden-journal")
  output, status = Open3.capture2e(CLI, "create-app", project, "--bundle", bundle_path, "--format", "json")
  abort output unless status.success?
  result = JSON.parse(output)
  abort "create-app did not declare local mutation" unless result.fetch("side_effect") == "local_mutation"
  abort "create-app did not apply the declared neutral starter" unless result.dig("payload", "starter", "status") == "applied"
  %w[dora .dora docs/product-brief.yaml docs/domain-library.yaml docs/idea-interview.yaml docs/first-capability.yaml docs/work/first-work.yaml docs/work/execution-inventory.yaml docs/project-memory.yaml .dora/codex-integration/README.md .git].each { |relative| abort "create-app omitted #{relative}" unless File.exist?(File.join(project, relative)) }
  brief = YAML.load_file(File.join(project, "docs/product-brief.yaml"))
  abort "create-app lost cited user problem" unless brief.fetch("user_problem") == "Notes are lost."
  abort "create-app invented away open decisions" unless brief.fetch("unanswered_decisions").include?("How long are notes retained?")
  existing = File.join(temporary, "existing")
  FileUtils.mkdir_p(existing)
  File.write(File.join(existing, "AGENTS.md"), "user-owned instructions\n")
  _output, rejected = Open3.capture2e(CLI, "create-app", existing, "--bundle", bundle_path)
  abort "create-app overwrote a non-empty destination" if rejected.success? || File.read(File.join(existing, "AGENTS.md")) != "user-owned instructions\n"
end

puts "Dora create-app command test passed (declared project bundle, Git baseline, and preservation boundary)."
