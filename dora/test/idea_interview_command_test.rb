#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-guided-interview") do |sandbox|
  session = File.join(sandbox, "state", "interview.yaml")
  unrelated = File.join(sandbox, "state", "keep.txt")
  FileUtils.mkdir_p(File.dirname(unrelated)); File.write(unrelated, "keep")
  output, status = Open3.capture2e(CLI, "interview-start", session, "--project", "home-stock", "--format", "json", chdir: ROOT)
  abort "interview start failed: #{output}" unless status.success? && JSON.parse(output).dig("payload", "next_question", "id") == "target_users"
  output, status = Open3.capture2e(CLI, "interview-next", session, "--format", "json", chdir: ROOT)
  abort "interview next failed: #{output}" unless status.success? && JSON.parse(output).dig("payload", "next_question", "id") == "target_users"
  output, status = Open3.capture2e(CLI, "interview-answer", session, "--id", "target_users", "--value", "Household member", "--format", "json", chdir: ROOT)
  abort "interview answer failed: #{output}" unless status.success? && JSON.parse(output).dig("payload", "next_question", "id") == "first_problem"
  abort "unrelated file was changed" unless File.read(unrelated) == "keep"
  _output, duplicate = Open3.capture2e(CLI, "interview-start", session, "--project", "home-stock", chdir: ROOT)
  abort "existing session was overwritten" if duplicate.success?
  stored = YAML.load_file(session)
  abort "answer did not retain explicit provenance" unless stored.fetch("answers").first.fetch("source") == "user_confirmed"
end

puts "Dora idea interview command test passed (safe session creation, one question, confirmed answer recording, and unrelated-file preservation)."
