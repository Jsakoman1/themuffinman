#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../audit_support"
require_relative "../../dora/lib/dora/plugins/http_contract_linker"

ROOT = File.expand_path("../..", __dir__)
endpoints = Dora::Plugins::HttpContractLinker.endpoint_links!(root: ROOT, controller_glob: "apps/themuffinman/src/main/java/com/themuffinman/app/*/controller/*Controller.java", client_glob: "apps/themuffinman/frontend/src/modules/**/*Api.ts")
report = {generated_at: Time.now.utc.iso8601, endpoint_count: endpoints.length, linked_endpoint_count: endpoints.count { |endpoint| !endpoint["client_matches"].empty? }, endpoints: endpoints}
AuditSupport.write_json("docs/audit-output/endpoint-callsite-linker.json", report)
AuditSupport.write_text("docs/audit-output/endpoint-callsite-linker-summary.md", "# Endpoint Callsite Linker\n\n- Endpoints scanned: `#{endpoints.length}`\n")
puts "Endpoint callsite linker passed (#{endpoints.length} declared endpoints; static links only)."
