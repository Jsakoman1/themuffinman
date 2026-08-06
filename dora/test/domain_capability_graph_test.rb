#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/domain_capability_graph"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
graph = YAML.load_file(File.join(ROOT, "test/fixtures/domain-capability-graph.yaml"))

report = Dora::DomainCapabilityGraph.report!(graph)
abort "capability graph selected the wrong first capability" unless report.dig("next_safe_capability", "id") == "item-catalog"
abort "capability graph did not retain open decision blocker" unless report.fetch("blocking_gaps").any? { |gap| gap.fetch("category") == "open_decision" && gap.fetch("capability_id") == "stock-entry" }
abort "capability graph did not retain unconfirmed capability blocker" unless report.fetch("blocking_gaps").any? { |gap| gap.fetch("category") == "capability_not_confirmed" }

cycle = Marshal.load(Marshal.dump(graph))
cycle.fetch("capabilities")[0]["dependencies"] = ["expiry-review"]
begin
  Dora::DomainCapabilityGraph.report!(cycle)
  abort "capability graph accepted a cycle"
rescue ArgumentError => error
  abort error.message unless error.message.include?("cycle")
end

Dir.mktmpdir("dora-capability-graph") do |root|
  path = File.join(root, "graph.yaml")
  File.write(path, YAML.dump(graph))
  output, status = Open3.capture2e(CLI, "capability-graph", path, "--format", "json", chdir: ROOT)
  abort "capability-graph command failed: #{output}" unless status.success?
  envelope = JSON.parse(output)
  abort "capability-graph command did not report next safe capability" unless envelope.dig("payload", "next_safe_capability", "id") == "item-catalog"
end

puts "Dora domain capability graph test passed (declared gaps and one safe next capability are reported without completion claims)."
