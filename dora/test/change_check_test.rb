#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/change_check"

Dir.mktmpdir("dora-change-check") do |root|
  config = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "source", "path_prefixes" => ["src/"], "validations" => ["test"], "documentation" => ["docs/domain.md"], "runtime_evidence" => ["runtime/source-flow"], "decisions" => ["DEC-1"]}]}
  path = File.join(root, "impact.yaml")
  File.write(path, YAML.dump(config))
  result = Dora::ChangeCheck.check!(path, ["src/item.rb", "notes.txt"])
  abort "change-check lost declared validation" unless result.fetch("validations") == ["test"]
  abort "change-check lost declared documentation or decision" unless result.fetch("documentation") == ["docs/domain.md"] && result.fetch("decisions") == ["DEC-1"]
  abort "change-check hid unmatched path" unless result.fetch("unmatched_paths") == ["notes.txt"]
end

puts "Dora change-check test passed (declared validation, documentation, evidence, decision, and unmatched-path guidance)."
