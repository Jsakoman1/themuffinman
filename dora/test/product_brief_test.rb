#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/product_brief"

Dir.mktmpdir("dora-product-brief") do |root|
  brief = {"kind" => "dora_product_brief", "version" => 1, "product" => "Circle desk", "user_problem" => "Small teams lose decisions across chat.", "primary_users" => ["Small teams"], "intended_outcomes" => ["Decisions remain findable."], "non_goals" => ["Replace every chat tool."], "assumptions" => ["Teams will record decisions."], "risks" => ["Low adoption."], "unanswered_decisions" => ["Which notifications are needed?"]}
  path = File.join(root, "product-brief.yaml")
  File.write(path, YAML.dump(brief))
  loaded = Dora::ProductBrief.load!(path)
  abort "product brief lost its product intent" unless loaded.fetch("product") == "Circle desk"

  brief["unanswered_decisions"] = []
  File.write(path, YAML.dump(brief))
  abort "product brief rejected a resolved decision list" unless Dora::ProductBrief.load!(path).fetch("unanswered_decisions") == []

  brief["risks"] = []
  File.write(path, YAML.dump(brief))
  begin
    Dora::ProductBrief.load!(path)
    abort "product brief accepted a missing risk boundary"
  rescue ArgumentError => error
    abort "wrong product brief failure: #{error.message}" unless error.message.include?("risks")
  end
end

puts "Dora product brief test passed (portable intent, boundaries, and explicit uncertainty)."
