#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/feature_skeleton"

input = {"kind" => "dora_feature_skeleton", "version" => 1, "capability" => "record-supply", "entity" => "supply", "fields" => [{"id" => "name", "type" => "string", "confirmed" => true}], "permission" => "household-write", "workflow" => "supply-active", "api_operations" => [{"id" => "create-supply", "purpose" => "Record one supply.", "confirmed" => true}], "ui_blueprint" => "list-detail", "confirmation" => true}
valid = Dora::FeatureSkeleton.validate!(input)
abort "feature skeleton lost confirmed field" unless valid.fetch("fields").first.fetch("id") == "name"
missing = Marshal.load(Marshal.dump(input)); missing["fields"].first["confirmed"] = false
begin; Dora::FeatureSkeleton.validate!(missing); abort "unconfirmed field accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("fields"); end
puts "Dora feature skeleton test passed (only explicit confirmed feature model inputs are accepted)."
