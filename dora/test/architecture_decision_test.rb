#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/architecture_decision"

input = {"kind" => "dora_architecture_decision", "version" => 1, "id" => "offline-choice", "status" => "unresolved", "alternatives" => ["Use online-only access.", "Add offline sync."], "rationale" => "The product need is still being assessed.", "consequences" => ["Implementation remains blocked until a choice is accepted."], "citations" => ["docs/product-brief.yaml"], "offline_sync" => "undecided", "confirmation" => true}
valid = Dora::ArchitectureDecision.validate!(input)
abort "offline state was not preserved" unless valid.fetch("offline_sync") == "undecided"
invalid = Marshal.load(Marshal.dump(input)); invalid["citations"] = ["../unsafe.yaml"]
begin
  Dora::ArchitectureDecision.validate!(invalid)
  abort "unsafe citation was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("citations")
end
puts "Dora architecture decision test passed (cited alternatives, consequences, and explicit offline/sync state are required)."
