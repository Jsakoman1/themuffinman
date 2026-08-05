#!/usr/bin/env ruby
# frozen_string_literal: true
require_relative "../lib/dora/revision_evidence_trace"
trace = {"kind" => "dora_revision_evidence_trace", "version" => 1, "capability_id" => "record-note", "revision" => "a" * 40, "changed_paths" => ["app/note.rb"], "work" => {"plan" => "docs/work/note.yaml", "task" => "implement-note"}, "validation_evidence" => [{"id" => "test", "path" => "evidence/test.txt", "status" => "recorded", "command" => "ruby test/note_test.rb"}], "runtime_evidence" => [{"id" => "runtime", "path" => "evidence/runtime.json", "status" => "pending"}], "unresolved" => [{"id" => "runtime", "reason" => "Runtime scenario is pending."}]}
result = Dora::RevisionEvidenceTrace.validate!(trace)
abort "trace lost revision" unless result.fetch("revision") == "a" * 40
abort "trace inferred runtime evidence" unless result.fetch("trace_state") == "unresolved"
invalid = Marshal.load(Marshal.dump(trace)); invalid["unresolved"] = []
begin; Dora::RevisionEvidenceTrace.validate!(invalid); abort "untracked pending evidence accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("must be unresolved"); end
puts "Dora revision evidence trace test passed (revision, change paths, task, validation, runtime, and unresolved links remain explicit)."
