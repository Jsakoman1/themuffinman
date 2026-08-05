#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/capability_trace"

def write(root, path, content = "fixture\n")
  absolute = File.join(root, path)
  FileUtils.mkdir_p(File.dirname(absolute))
  File.write(absolute, content)
end

def trace(runtime_status:, unresolved: [])
  {
    "kind" => "dora_capability_trace", "version" => 1,
    "capability" => {"id" => "record-note", "product_references" => ["docs/product-brief.yaml#record-note"], "domain_references" => ["docs/domain-library.yaml#note"]},
    "work" => {"plan" => "docs/work/record-note.yaml", "task" => "implement-record-note"},
    "implementation_references" => ["apps/note.rb"],
    "validation_evidence" => [{"id" => "record-note-test", "path" => "docs/evidence/test.txt", "status" => "recorded", "command" => "ruby test/note_test.rb"}],
    "runtime_evidence" => [{"id" => "record-note-runtime", "path" => "docs/evidence/runtime.json", "status" => runtime_status}],
    "unresolved" => unresolved
  }
end

Dir.mktmpdir("dora-capability-trace") do |root|
  %w[docs/product-brief.yaml docs/domain-library.yaml docs/work/record-note.yaml apps/note.rb docs/evidence/test.txt docs/evidence/runtime.json].each { |path| write(root, path) }
  complete = trace(runtime_status: "recorded")
  result = Dora::CapabilityTrace.validate!(complete, project_root: root)
  abort "complete trace did not retain evidence state" unless result.fetch("trace_state") == "evidence_recorded"
  abort "trace claimed completion" unless result.fetch("completion_boundary").include?("does not prove")

  unresolved = trace(runtime_status: "pending", unresolved: [{"id" => "record-note-runtime", "reason" => "Runtime review is not yet recorded."}])
  FileUtils.rm_f(File.join(root, "docs/evidence/runtime.json"))
  result = Dora::CapabilityTrace.validate!(unresolved, project_root: root)
  abort "pending runtime trace was not marked unresolved" unless result.fetch("trace_state") == "unresolved"

  invalid = trace(runtime_status: "recorded")
  invalid["capability"]["product_references"] = ["docs/missing-product.yaml"]
  begin
    Dora::CapabilityTrace.validate!(invalid, project_root: root)
    abort "missing product trace reference passed"
  rescue ArgumentError => error
    abort "wrong missing-reference rejection: #{error.message}" unless error.message.include?("reference is missing")
  end
end

puts "Dora capability trace test passed (declared intent, work, implementation, validation, runtime evidence, and unresolved boundaries)."
