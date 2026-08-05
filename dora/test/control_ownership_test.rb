#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/control_ownership"

alpha = {"kind" => "dora_control_ownership", "version" => 1, "allowed_owners" => ["core", "extension"], "rules" => [{"id" => "core-tool", "owner" => "core", "subjects" => ["script:tools/check.rb"]}, {"id" => "extension-fallback", "owner" => "extension", "patterns" => ["script:project/**/*.rb"]}]}
beta = {"kind" => "dora_control_ownership", "version" => 1, "allowed_owners" => ["shared", "local"], "rules" => [{"id" => "shared-command", "owner" => "shared", "subjects" => ["make:verify"]}, {"id" => "local-command", "owner" => "local", "patterns" => ["make:*"]}]}

alpha_result = Dora::ControlOwnership.classify!(alpha, ["script:tools/check.rb", "script:project/audit.rb"])
abort "alpha ownership classification is wrong" unless alpha_result.map { |row| row["owner"] } == ["core", "extension"]
beta_result = Dora::ControlOwnership.classify!(beta, ["make:verify", "make:build"])
abort "beta ownership classification is wrong" unless beta_result.map { |row| row["rule"] } == ["shared-command", "local-command"]

begin
  Dora::ControlOwnership.classify!(alpha, ["script:unknown.rb"])
  abort "unclassified subject passed"
rescue ArgumentError => error
  abort "wrong unclassified failure" unless error.message.include?("unclassified")
end

puts "Dora control ownership test passed (two declared ownership models)."
