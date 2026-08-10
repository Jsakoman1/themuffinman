#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/change_impact"

Dir.mktmpdir("dora-change-impact") do |root|
  alpha = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "api", "path_prefixes" => ["api/"], "validations" => ["api-test"], "documentation" => ["docs/api.md"], "runtime_evidence" => ["runtime/api"], "decisions" => ["API-1"], "companions" => [{"id" => "api-dto", "paths" => ["dto/api.rb", "test/api_test.rb"], "reason" => "API contract changes may require DTO and test review."}]}]}
  beta = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => "ui", "path_prefixes" => ["ui/"], "validations" => ["ui-test"], "documentation" => ["docs/ui.md"], "runtime_evidence" => ["runtime/ui"], "decisions" => ["UI-1"]}]}
  alpha_path = File.join(root, "alpha.yaml")
  beta_path = File.join(root, "beta.yaml")
  File.write(alpha_path, YAML.dump(alpha))
  File.write(beta_path, YAML.dump(beta))
  alpha_result = Dora::ChangeImpact.assess!(alpha_path, ["api/controller.rb"])
  beta_result = Dora::ChangeImpact.assess!(beta_path, ["ui/surface.vue"])
  abort "alpha impact omitted declared validation" unless alpha_result.fetch("validations") == ["api-test"]
  abort "beta impact omitted declared decision" unless beta_result.fetch("decisions") == ["UI-1"]
  abort "impact crossed project boundaries" unless alpha_result.fetch("documentation") != beta_result.fetch("documentation")
  companion = alpha_result.fetch("companion_findings").first
  abort "impact omitted declared advisory companion" unless companion.fetch("source_path") == "api/controller.rb" && companion.fetch("paths") == ["dto/api.rb", "test/api_test.rb"] && companion.fetch("classification_required") && companion.fetch("read_only") && companion.fetch("disposition") == "advisory"
end

puts "Dora change impact test passed (two projects retain their own declared validation, evidence, documentation, and decisions)."
