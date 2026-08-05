#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
decision = {"kind" => "dora_architecture_decision", "version" => 1, "id" => "offline-choice", "status" => "unresolved", "alternatives" => ["Use online-only access."], "rationale" => "The product need is still being assessed.", "consequences" => ["Implementation remains blocked until a choice is accepted."], "citations" => ["docs/product-brief.yaml"], "offline_sync" => "undecided", "confirmation" => true}

Dir.mktmpdir("dora-architecture-decision-command") do |root|
  input = File.join(root, "decision.yaml"); log = File.join(root, "decisions.yaml")
  File.write(input, YAML.dump(decision))
  output, status = Open3.capture2e(CLI, "architecture-decision", log, "--input", input, chdir: ROOT)
  abort "append failed: #{output}" unless status.success?
  decision["id"] = "database-choice"; decision["offline_sync"] = "not-needed"; File.write(input, YAML.dump(decision))
  _output, second = Open3.capture2e(CLI, "architecture-decision", log, "--input", input, chdir: ROOT)
  abort "second append failed" unless second.success? && YAML.load_file(log).fetch("entries").map { |entry| entry.fetch("id") } == %w[offline-choice database-choice]
  _output, duplicate = Open3.capture2e(CLI, "architecture-decision", log, "--input", input, chdir: ROOT)
  abort "duplicate decision was accepted" if duplicate.success?
end

puts "Dora architecture decision command test passed (cited decisions append without replacing prior records and duplicates are rejected)."
