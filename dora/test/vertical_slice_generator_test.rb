#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/vertical_slice_generator"

context = {"kind" => "dora_confirmed_capability_context", "version" => 1, "capability" => {"id" => "record-supply", "title" => "Record a supply", "confirmed" => true}, "decisions" => {"data_safety" => "Retention is confirmed.", "workflow" => "Draft and recorded states are confirmed.", "permission" => "Member permission is confirmed.", "technical" => "Spring, Vue, and Postgres are selected."}}
proposal = Dora::VerticalSliceGenerator.generate!(context)
abort "generator invented a gap" unless proposal.fetch("gaps").empty?
abort "generator omitted a migration proposal" unless proposal.dig("surfaces", "migration").first.include?("record_supply")
abort "generator emitted source code" if proposal.to_s.match?(/class |create table|@RestController/i)

missing = Marshal.load(Marshal.dump(context)); missing["decisions"].delete("permission"); missing["decisions"].delete("technical")
blocked = Dora::VerticalSliceGenerator.generate!(missing)
%w[permission technical].each { |term| abort "generator hid #{term} gap" unless blocked.fetch("gaps").any? { |gap| gap.include?(term) } }
unconfirmed = Marshal.load(Marshal.dump(context)); unconfirmed["capability"]["confirmed"] = false
begin; Dora::VerticalSliceGenerator.generate!(unconfirmed); abort "generator accepted unconfirmed capability"; rescue ArgumentError => error; abort error.message unless error.message.include?("confirmed"); end

puts "Dora vertical-slice generator test passed (confirmed context creates proposals while missing decisions remain explicit gaps)."
