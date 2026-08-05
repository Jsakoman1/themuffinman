#!/usr/bin/env ruby
# frozen_string_literal: true

exec("dora/bin/dora", "plugin-run", ".dora/plugins.yaml", "http-contract-drift") unless ENV["DORA_PLUGIN_RUNNER"] == "1"

require "time"
require_relative "../audit_support"
require_relative "../../dora/lib/dora/plugins/http_contract_linker"

ROOT = File.expand_path("../..", __dir__)
dtos = Dora::Plugins::HttpContractLinker.dto_drift!(root: ROOT, dto_glob: "apps/themuffinman/src/main/java/com/themuffinman/app/**/*DTO.java", contract_path: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", frontend_glob: "apps/themuffinman/frontend/src/**/*.{ts,vue}")
report = {generated_at: Time.now.utc.iso8601, dto_count: dtos.length, missing_contract_count: dtos.count { |dto| !dto["generated_contract_present"] }, dtos: dtos}
AuditSupport.write_json("docs/audit-output/api-contract-drift.json", report)
AuditSupport.write_text("docs/audit-output/api-contract-drift-summary.md", "# API Contract Drift Audit\n\n- Backend DTOs scanned: `#{dtos.length}`\n")
puts "API contract drift audit passed (#{dtos.length} declared DTOs; static analysis only)."
