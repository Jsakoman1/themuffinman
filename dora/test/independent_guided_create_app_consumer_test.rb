#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
FIXTURE = File.join(ROOT, "test/fixtures/guided-create-app-consumers.yaml")

def checksum_for(root)
  entries = Dir[File.join(root, "**/*")].select { |path| File.file?(path) && !path.delete_prefix("#{root}/").split("/").include?(".git") }.sort
  Digest::SHA256.hexdigest(entries.map { |path| "#{path.delete_prefix("#{root}/")}\0#{Digest::SHA256.file(path).hexdigest}" }.join("\n"))
end

def answers_for(consumer)
  [{"id" => "target_users", "value" => consumer.fetch("users"), "source" => "user"}, {"id" => "first_problem", "value" => consumer.fetch("problem"), "source" => "user"}, {"id" => "first_capability", "value" => consumer.fetch("capability"), "source" => "user"}, {"id" => "domain_concepts", "value" => consumer.fetch("concepts"), "source" => "user"}, {"id" => "permission_intent", "value" => consumer.fetch("permission"), "source" => "user"}, {"id" => "workflow_intent", "value" => consumer.fetch("workflow"), "source" => "user"}, {"id" => "forbidden_outcomes", "value" => consumer.fetch("forbidden"), "source" => "user"}]
end

fixture = YAML.load_file(FIXTURE)
abort "guided consumer fixture is invalid" unless fixture["kind"] == "dora_guided_create_app_consumers" && fixture["version"] == 1
Dir.mktmpdir("dora-guided-consumers") do |temporary|
  source = File.join(temporary, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  checksum = checksum_for(source)
  created = fixture.fetch("consumers").map do |consumer|
    interview = {"kind" => "dora_idea_interview", "version" => 1, "project_id" => consumer.fetch("id"), "answers" => answers_for(consumer), "unanswered_decisions" => [{"id" => "open-decision", "question" => consumer.fetch("decision"), "source" => "user"}]}
    bundle = {"kind" => "dora_create_app", "version" => 1, "interview" => interview, "dora_source" => {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "reviewed-dora", "ref" => "a" * 40, "checksum" => checksum}, "review" => {"id" => "independent-guided-create-app", "reviewed_by" => "user", "reviewed_at" => "2026-08-05"}}, "first_capability" => {"id" => "first-capability", "title" => consumer.fetch("capability"), "interview_answer" => "first_capability"}, "first_work" => {"id" => "declare-first-capability", "title" => "Declare first capability", "observable_outcome" => "The first capability is declared.", "required_paths" => ["docs/first-capability.yaml"], "validation" => "./bin/dora doctor .dora/project.yaml", "evidence_boundary" => ["declared first capability"]}, "codex_integration" => true}
    bundle_path = File.join(temporary, "#{consumer.fetch("id")}.yaml")
    File.write(bundle_path, YAML.dump(bundle))
    root = File.join(temporary, consumer.fetch("id"))
    output, status = Open3.capture2e(CLI, "create-app", root, "--bundle", bundle_path)
    abort "guided consumer creation failed: #{output}" unless status.success?
    [consumer, root, bundle_path]
  end

  created.each do |consumer, root, bundle_path|
    product = YAML.load_file(File.join(root, "docs/product-brief.yaml"))
    domain = YAML.load_file(File.join(root, "docs/domain-library.yaml"))
    abort "consumer lost its product intent" unless product.fetch("user_problem") == consumer.fetch("problem")
    abort "consumer lost its domain intent" unless domain.fetch("vocabulary").map { |row| row.fetch("description") } == consumer.fetch("concepts")
    user_guidance = "User-owned #{consumer.fetch("id")} instructions\n"
    File.write(File.join(root, "AGENTS.md"), user_guidance)
    _output, rejected = Open3.capture2e(CLI, "create-app", root, "--bundle", bundle_path)
    abort "create-app overwrote user guidance" if rejected.success? || File.read(File.join(root, "AGENTS.md")) != user_guidance
  end

  first_docs = File.read(File.join(created[0][1], "docs/idea-interview.yaml"))
  second_docs = File.read(File.join(created[1][1], "docs/idea-interview.yaml"))
  abort "guided consumers received shared product knowledge" if first_docs.include?(fixture.fetch("consumers")[1].fetch("problem")) || second_docs.include?(fixture.fetch("consumers")[0].fetch("problem"))
end

puts "Dora independent guided create-app consumer test passed (two projects keep distinct cited intent and preserve user instructions)."
