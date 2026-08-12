#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "yaml"
require_relative "../lib/dora/documentation_control"

Dir.mktmpdir("dora-documentation-control") do |root|
  map = File.join(root, "map.yaml"); evidence = File.join(root, "evidence.yaml")
  File.write(map, YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "core"}, {"id" => "docs"}], "edges" => [{"from" => "core", "to" => "docs"}]}))
  File.write(evidence, YAML.dump({"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "current-contract", "match" => "contract", "evidence" => ["docs/current.md"]}]}))
  Dora::DocumentationControl.validate!(system_map: map, documentation_evidence: evidence)
  File.write(map, YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "core"}], "edges" => [{"from" => "core", "to" => "missing"}]}))
  begin
    Dora::DocumentationControl.validate!(system_map: map, documentation_evidence: evidence)
    abort "unknown system-map endpoint was accepted"
  rescue ArgumentError
    nil
  end
end

ROOT = File.expand_path("..", __dir__)
Dora::DocumentationControl.validate!(system_map: File.join(ROOT, ".dora/controls/system-map.yaml"), documentation_evidence: File.join(ROOT, ".dora/controls/documentation-evidence.yaml"))
puts "Dora documentation control test passed (explicit map endpoints and claim references)."
