#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/capability-trace-consumers.yaml"))

def write(root, path, content)
  absolute = File.join(root, path)
  FileUtils.mkdir_p(File.dirname(absolute))
  File.write(absolute, content)
end

def trace(consumer)
  runtime_id = "#{consumer.fetch("capability")}-runtime"
  {
    "kind" => "dora_capability_trace", "version" => 1,
    "capability" => {"id" => consumer.fetch("capability"), "product_references" => ["docs/product.yaml##{consumer.fetch("capability")}"], "domain_references" => ["docs/domain.yaml##{consumer.fetch("capability")}"]},
    "work" => {"plan" => "docs/work/capability.yaml", "task" => "implement-capability"}, "implementation_references" => ["app/capability.rb"],
    "validation_evidence" => [{"id" => "#{consumer.fetch("capability")}-test", "path" => "evidence/test.txt", "status" => "recorded", "command" => "ruby test/capability_test.rb"}],
    "runtime_evidence" => [{"id" => runtime_id, "path" => "evidence/runtime.json", "status" => consumer.fetch("runtime_status")}],
    "unresolved" => consumer.fetch("runtime_status") == "recorded" ? [] : [{"id" => runtime_id, "reason" => consumer.fetch("unresolved_reason")}]
  }
end

Dir.mktmpdir("dora-independent-capability-trace") do |sandbox|
  installed_dora = File.join(sandbox, "dora")
  FileUtils.cp_r(ROOT, installed_dora)

  FIXTURE.fetch("consumers").each do |consumer|
    project = File.join(sandbox, consumer.fetch("id"))
    %w[docs/product.yaml docs/domain.yaml docs/work/capability.yaml app/capability.rb evidence/test.txt].each { |path| write(project, path, consumer.fetch("capability")) }
    write(project, "evidence/runtime.json", consumer.fetch("capability")) if consumer.fetch("runtime_status") == "recorded"
    path = File.join(project, "docs/capability-trace.yaml")
    File.write(path, YAML.dump(trace(consumer))); before = File.read(path)

    output, status = Open3.capture2e(File.join(installed_dora, "bin/dora"), "capability-trace", "docs/capability-trace.yaml", "--format", "json", chdir: project)
    envelope = JSON.parse(output)
    payload = envelope.fetch("payload")
    abort "consumer trace did not remain read-only" unless status.success? && envelope.fetch("side_effect") == "read_only" && File.read(path) == before
    abort "consumer trace state is wrong" unless payload.fetch("trace_state") == consumer.fetch("expected_trace_state")
    other_capability = FIXTURE.fetch("consumers").find { |candidate| candidate.fetch("id") != consumer.fetch("id") }.fetch("capability")
    abort "consumer trace leaked another capability" unless payload.dig("capability", "id") == consumer.fetch("capability") && !output.include?(other_capability)
    abort "consumer trace lost product citation" unless envelope.fetch("citations").include?("docs/product.yaml##{consumer.fetch("capability")}")
  end
end

puts "Dora independent capability trace consumer test passed (two isolated traces preserve evidence state and unresolved boundaries)."
