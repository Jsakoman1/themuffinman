#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

def write(root, path)
  absolute = File.join(root, path)
  FileUtils.mkdir_p(File.dirname(absolute))
  File.write(absolute, "fixture\n")
end

Dir.mktmpdir("dora-capability-trace-command") do |root|
  %w[docs/product.yaml docs/domain.yaml docs/work/notes.yaml app/note.rb evidence/test.txt evidence/runtime.json].each { |path| write(root, path) }
  trace = {
    "kind" => "dora_capability_trace", "version" => 1,
    "capability" => {"id" => "note", "product_references" => ["docs/product.yaml#note"], "domain_references" => ["docs/domain.yaml#note"]},
    "work" => {"plan" => "docs/work/notes.yaml", "task" => "write-note"}, "implementation_references" => ["app/note.rb"],
    "validation_evidence" => [{"id" => "note-test", "path" => "evidence/test.txt", "status" => "recorded", "command" => "ruby test/note_test.rb"}],
    "runtime_evidence" => [{"id" => "note-runtime", "path" => "evidence/runtime.json", "status" => "recorded"}], "unresolved" => []
  }
  path = File.join(root, "docs/capability-trace.yaml")
  File.write(path, YAML.dump(trace)); before = File.read(path)
  output, status = Open3.capture2e(CLI, "capability-trace", "docs/capability-trace.yaml", "--format", "json", chdir: root)
  envelope = JSON.parse(output)
  abort "capability trace CLI did not emit a read-only envelope" unless status.success? && envelope.values_at("kind", "outcome", "side_effect") == ["dora_command_envelope", "success", "read_only"]
  abort "capability trace CLI lost citations" unless envelope.fetch("citations").include?("docs/product.yaml#note") && envelope.dig("payload", "trace_state") == "evidence_recorded"
  abort "capability trace CLI mutated project evidence" unless File.read(path) == before
end

puts "Dora capability trace command test passed (read-only cited trace without completion inference)."
