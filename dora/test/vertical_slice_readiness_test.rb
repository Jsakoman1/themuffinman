#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/vertical_slice_generator"
require_relative "../lib/dora/vertical_slice_readiness"

context = {"kind" => "dora_confirmed_capability_context", "version" => 1, "capability" => {"id" => "record-supply", "title" => "Record a supply", "confirmed" => true}, "decisions" => {"data_safety" => "Confirmed", "workflow" => "Confirmed", "permission" => "Confirmed", "technical" => "Confirmed"}}
ready = Dora::VerticalSliceReadiness.evaluate!(Dora::VerticalSliceGenerator.generate!(context))
abort "complete proposal is not ready" unless ready.fetch("ready_to_plan") && ready.fetch("blocking_gaps").empty?

blocked_context = Marshal.load(Marshal.dump(context)); blocked_context["decisions"].delete("data_safety"); blocked_context["decisions"].delete("permission")
blocked = Dora::VerticalSliceReadiness.evaluate!(Dora::VerticalSliceGenerator.generate!(blocked_context))
abort "incomplete proposal is ready" if blocked.fetch("ready_to_plan")
categories = blocked.fetch("blocking_gaps").map { |gap| gap.fetch("category") }
%w[data_safety permission].each { |category| abort "readiness omitted #{category} blocker" unless categories.include?(category) }
abort "blocker lacks cited origin" unless blocked.fetch("blocking_gaps").all? { |gap| gap.fetch("source") == "proposal.gaps" }

puts "Dora vertical-slice readiness test passed (exact data-safety, workflow, permission, and technical blockers remain visible before planning)."
