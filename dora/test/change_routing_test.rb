#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "yaml"
require_relative "../lib/dora/change_routing"
Dir.mktmpdir("dora-change-routing") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml"); beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "commands" => ["test"]}]}))
  File.write(beta, YAML.dump({"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "docs", "path_prefixes" => ["guide/"], "commands" => ["lint"]}]}))
  abort "alpha routing failed" unless Dora::ChangeRouting.route!(alpha, ["src/a.rb"]).fetch("commands") == ["test"]
  abort "beta routing failed" unless Dora::ChangeRouting.route!(beta, ["guide/a.md"]).fetch("commands") == ["lint"]
end
puts "Dora change routing test passed (two project configurations)."
