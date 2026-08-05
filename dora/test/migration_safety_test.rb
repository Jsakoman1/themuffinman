#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/migration_safety"

safe = {"kind" => "dora_migration_safety_input", "version" => 1, "migrations" => [{"version" => "V1", "checksum" => "same", "baseline_checksum" => "same", "foreign_key_review" => "confirmed", "index_review" => "confirmed"}]}
abort "safe migration was not accepted" unless Dora::MigrationSafety.review!(safe).fetch("safe_to_apply") == true
unsafe = Marshal.load(Marshal.dump(safe)); unsafe.fetch("migrations").first.merge!("checksum" => "changed", "foreign_key_review" => "missing", "index_review" => "missing")
findings = Dora::MigrationSafety.review!(unsafe).fetch("findings").map { |finding| finding.fetch("id") }
abort "historical migration edit was not found" unless findings.include?("historical_migration_changed")
abort "review gaps were not found" unless findings.include?("foreign_key_review_missing") && findings.include?("index_review_missing")
puts "Dora migration safety test passed (static findings detect historical changes and missing foreign-key/index review)."
