#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/data_safety_profile"

control = {"strategy" => "Declare and review before external execution.", "external_approval_required" => true, "confirmed" => true}
input = {"kind" => "dora_data_safety_profile", "version" => 1, "backup" => control, "restore_test" => control, "export" => control, "retention" => control, "audit" => control, "demo_data" => {"isolation" => "Use a separately declared demo environment.", "real_data_excluded" => true, "confirmed" => true}, "confirmation" => true}
valid = Dora::DataSafetyProfile.validate!(input)
abort "demo isolation was not preserved" unless valid.dig("demo_data", "real_data_excluded") == true
invalid = Marshal.load(Marshal.dump(input)); invalid["export"] = invalid.fetch("export").dup; invalid.fetch("export")["external_approval_required"] = false
begin
  Dora::DataSafetyProfile.validate!(invalid)
  abort "external export approval boundary was accepted as missing"
rescue ArgumentError => error
  abort error.message unless error.message.include?("export")
end
puts "Dora data safety profile test passed (confirmed controls and real-data/demo separation are required without external actions)."
