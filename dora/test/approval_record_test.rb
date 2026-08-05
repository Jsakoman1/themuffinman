#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../lib/dora/approval_record"

NOW = Time.utc(2026, 8, 5)
record = {"kind" => "dora_approval_record", "version" => 1, "id" => "approved-upgrade", "actor" => "reviewer", "operation" => "upgrade_apply", "scope" => "project-a", "expires_at" => "2026-08-06T00:00:00Z", "evidence" => "reviewed fixture", "rollback" => "recorded backup"}
abort "valid approval was rejected" unless Dora::ApprovalRecord.validate!(record, operation: "upgrade_apply", scope: "project-a", now: NOW).frozen?

[[record.merge("scope" => "project-b"), "scope mismatch"], [record.merge("expires_at" => "2026-08-04T00:00:00Z"), "expired approval"], [record.merge("expires_at" => "not-a-time"), ""]].each do |invalid, expected|
  begin
    Dora::ApprovalRecord.validate!(invalid, operation: "upgrade_apply", scope: "project-a", now: NOW)
    abort "invalid approval was accepted"
  rescue ArgumentError => error
    abort "wrong approval rejection: #{error.message}" unless expected.empty? || error.message.include?(expected)
  end
end

puts "Dora approval record test passed (valid, scope-mismatch, expired, and invalid-time records)."
