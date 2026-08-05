#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/api_workflow_safety"

safe = {"kind" => "dora_api_workflow_safety_input", "version" => 1, "operations" => [{"id" => "create-supply", "dto_id" => "supply-create", "api_id" => "create-supply", "client_id" => "supply-client", "permission_id" => "household-write", "workflow_id" => "supply-active", "permission_confirmed" => true, "api_statuses" => ["active"], "workflow_statuses" => ["active", "archived"]}]}
abort "complete declaration was not accepted" unless Dora::ApiWorkflowSafety.review!(safe).fetch("safe_to_implement") == true
unsafe = Marshal.load(Marshal.dump(safe)); operation = unsafe.fetch("operations").first; operation.delete("client_id"); operation["permission_confirmed"] = false; operation["api_statuses"] = ["deleted"]
findings = Dora::ApiWorkflowSafety.review!(unsafe).fetch("findings").map { |finding| finding.fetch("id") }
abort "client gap was not found" unless findings.include?("missing_client_id")
abort "permission gap was not found" unless findings.include?("permission_confirmation_missing")
abort "workflow mismatch was not found" unless findings.include?("api_workflow_status_mismatch")
puts "Dora API workflow safety test passed (static findings detect DTO/API/client/permission/workflow gaps)."
