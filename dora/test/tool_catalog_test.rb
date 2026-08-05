#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"

require_relative "../lib/dora/tool_catalog"

def catalog(id, target)
  {"kind" => "dora_tool_catalog", "version" => 1, "commands" => [{"id" => id, "target" => target, "purpose" => "Run #{target}.", "preconditions" => ["ready"], "expected_cost" => "short"}]}
end

Dir.mktmpdir("dora-tool-catalog") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml")
  beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump(catalog("alpha-check", "check")))
  File.write(beta, YAML.dump(catalog("beta-audit", "audit")))
  abort "alpha catalog was not rendered" unless Dora::ToolCatalog.help_lines(alpha) == ["check [short] — Run check."]
  abort "beta catalog was not rendered" unless Dora::ToolCatalog.help_lines(beta) == ["audit [short] — Run audit."]
end

puts "Dora tool catalog test passed (two project catalogs)."
