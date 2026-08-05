#!/usr/bin/env ruby
# frozen_string_literal: true
require "json"
require "open3"
require "tmpdir"
require "yaml"
ROOT = File.expand_path("..", __dir__); CLI = File.join(ROOT, "bin/dora")
Dir.mktmpdir("dora-evidence-explain") do |root|
  trace = {"kind" => "dora_revision_evidence_trace", "version" => 1, "capability_id" => "record-note", "revision" => "a" * 40, "changed_paths" => ["app/note.rb"], "work" => {"plan" => "docs/work/note.yaml", "task" => "implement-note"}, "validation_evidence" => [{"id" => "test", "path" => "evidence/test.txt", "status" => "recorded", "command" => "ruby test/note.rb"}], "runtime_evidence" => [{"id" => "runtime", "path" => "evidence/runtime.json", "status" => "pending"}], "unresolved" => [{"id" => "runtime", "reason" => "Runtime is pending."}]}
  path = File.join(root, "trace.yaml"); File.write(path, YAML.dump(trace))
  output, status = Open3.capture2e(CLI, "evidence-explain", path, "--format", "json")
  abort output unless status.success?
  result = JSON.parse(output); payload = result.fetch("payload")
  abort "evidence command omitted a gap" unless payload.fetch("not_evidenced") == ["Runtime is pending."]
  abort "evidence command made a mutation" unless result.fetch("side_effect") == "read_only"
end
puts "Dora evidence explain command test passed (cited evidence and unresolved runtime gap remain explicit)."
