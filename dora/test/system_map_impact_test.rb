#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "yaml"
require_relative "../lib/dora/system_map_impact"
Dir.mktmpdir("dora-system-map") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml"); beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "code"}, {"id" => "docs"}], "edges" => [{"from" => "code", "to" => "docs"}]}))
  File.write(beta, YAML.dump({"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "api"}, {"id" => "test"}], "edges" => [{"from" => "api", "to" => "test"}]}))
  abort "alpha traversal failed" unless Dora::SystemMapImpact.related!(alpha, ["code"]).fetch("related") == ["docs"]
  abort "beta traversal failed" unless Dora::SystemMapImpact.related!(beta, ["api"]).fetch("related") == ["test"]
end
puts "Dora system map impact test passed (two project maps)."
