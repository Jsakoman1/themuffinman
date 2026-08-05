#!/usr/bin/env ruby
# frozen_string_literal: true
require "json"
require "open3"
require "tmpdir"
require "yaml"
ROOT = File.expand_path("..", __dir__); CLI = File.join(ROOT, "bin/dora")
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/revision-evidence-consumers.yaml"))
abort "revision evidence consumer fixture is invalid" unless fixture["kind"] == "dora_revision_evidence_consumers"
Dir.mktmpdir("dora-revision-evidence-consumers") do |root|
  fixture.fetch("consumers").each do |consumer|
    runtime = consumer.fetch("runtime")
    unresolved = runtime == "recorded" ? [] : [{"id" => "runtime", "reason" => "#{consumer.fetch("id")} runtime is pending."}]
    trace = {"kind" => "dora_revision_evidence_trace", "version" => 1, "capability_id" => consumer.fetch("capability"), "revision" => consumer.fetch("revision"), "changed_paths" => ["app/#{consumer.fetch("id")}.rb"], "work" => {"plan" => "docs/work/#{consumer.fetch("id")}.yaml", "task" => "implement-#{consumer.fetch("id")}"}, "validation_evidence" => [{"id" => "test", "path" => "evidence/test.txt", "status" => "recorded", "command" => "ruby test.rb"}], "runtime_evidence" => [{"id" => "runtime", "path" => "evidence/runtime.json", "status" => runtime}], "unresolved" => unresolved}
    path = File.join(root, "#{consumer.fetch("id")}.yaml"); File.write(path, YAML.dump(trace))
    output, status = Open3.capture2e(CLI, "evidence-explain", path, "--format", "json"); abort output unless status.success?
    payload = JSON.parse(output).fetch("payload")
    abort "consumer revision leaked" unless payload.fetch("revision") == consumer.fetch("revision") && payload.fetch("capability_id") == consumer.fetch("capability")
    other = fixture.fetch("consumers").find { |candidate| candidate.fetch("id") != consumer.fetch("id") }
    abort "consumer evidence leaked" if payload.to_s.include?(other.fetch("revision")) || payload.to_s.include?(other.fetch("capability"))
  end
end
puts "Dora independent revision evidence consumer test passed (two consumers expose only their own revision evidence and unresolved boundaries)."
