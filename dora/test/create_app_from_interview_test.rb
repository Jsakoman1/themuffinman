#!/usr/bin/env ruby
# frozen_string_literal: true
require "digest"
require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"
ROOT = File.expand_path("..", __dir__); CLI = File.join(ROOT, "bin/dora")
def checksum(root); entries = Dir[File.join(root, "**/*")].select { |path| File.file?(path) && !path.delete_prefix("#{root}/").split("/").include?(".git") }.sort; Digest::SHA256.hexdigest(entries.map { |path| "#{path.delete_prefix("#{root}/")}\0#{Digest::SHA256.file(path).hexdigest}" }.join("\n")); end
def answer(id, value); {"id" => id, "value" => value, "source" => "user"}; end
Dir.mktmpdir("dora-create-app-interview") do |root|
  source = File.join(root, "source"); FileUtils.cp_r(ROOT, source)
  interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => "garden-journal", "answers" => [answer("target_users", ["member"]), answer("first_problem", "Notes are lost."), answer("first_capability", "Record note."), answer("domain_concepts", ["note"]), answer("permission_intent", "Member records a note."), answer("workflow_intent", "Draft becomes recorded."), answer("forbidden_outcomes", ["No public notes."])], "unanswered_decisions" => []}
  interview_path = File.join(root, "interview.yaml"); File.write(interview_path, YAML.dump(interview))
  source_path = File.join(root, "source.yaml"); File.write(source_path, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "source", "ref" => "a" * 40, "checksum" => checksum(source)}, "review" => {"id" => "review", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}))
  destination = File.join(root, "project"); output, status = Open3.capture2e(CLI, "create-app", destination, "--interview", interview_path, "--source", source_path, "--starter", "blank", "--format", "json"); abort output unless status.success?
  package = YAML.load_file(File.join(destination, "docs/capability-package.yaml")); abort "generated capability package is invalid" unless package.fetch("id") == "first-capability" && package.dig("work", "task") == "prepare-first-capability"
end
puts "Dora create-app interview test passed (interview and reviewed source generate the first capability package without a hand-authored bundle)."
