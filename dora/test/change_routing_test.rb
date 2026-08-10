#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/change_routing"

Dir.mktmpdir("dora-change-routing") do |root|
  config_path = File.join(root, "routing.yaml")
  config = {"kind" => "dora_change_routing", "version" => 1, "rules" => [
    {"id" => "core", "path_prefixes" => ["lib/"], "commands" => ["core-test", "shared-test"]},
    {"id" => "plugins", "path_prefixes" => ["lib/dora/plugins/"], "commands" => ["plugin-test", "shared-test"]},
    {"id" => "docs", "path_prefixes" => ["docs/"], "commands" => ["doctor"]}
  ]}
  File.write(config_path, YAML.dump(config))

  result = Dora::ChangeRouting.route!(config_path, ["docs/guide.md", "lib/dora/plugins/http.rb", "misc/file.txt", "lib/dora/plugins/http.rb"])
  abort "routing omitted advisory metadata" unless result.fetch("kind") == "dora_change_routing_result" && result.fetch("read_only") && result.fetch("disposition") == "advisory" && !result.fetch("observed_at").empty?
  abort "routing did not sort and deduplicate paths" unless result.fetch("changed_paths") == ["docs/guide.md", "lib/dora/plugins/http.rb", "misc/file.txt"]
  abort "routing did not merge overlapping rules" unless result.fetch("commands") == ["doctor", "core-test", "shared-test", "plugin-test"]
  plugin_route = result.fetch("path_routes").find { |route| route.fetch("path") == "lib/dora/plugins/http.rb" }
  abort "routing omitted per-path explanation" unless plugin_route.fetch("rule_ids") == ["core", "plugins"] && plugin_route.fetch("commands") == ["core-test", "shared-test", "plugin-test"]
  abort "routing did not expose unmatched path" unless result.fetch("unmatched_paths") == ["misc/file.txt"]
  begin
    Dora::ChangeRouting.route!(config_path, ["../outside.rb"])
    abort "routing accepted an unsafe path"
  rescue ArgumentError
    nil
  end
end

puts "Dora change routing test passed (deterministic advisory explanations without execution)."
