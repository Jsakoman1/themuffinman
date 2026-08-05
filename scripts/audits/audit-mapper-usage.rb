#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../audit_support"
require_relative "../../dora/lib/dora/plugins/spring_mapper_usage"

ROOT = File.expand_path("../..", __dir__)
rows = Dora::Plugins::SpringMapperUsage.analyze!(root: ROOT, mapper_glob: "apps/themuffinman/src/main/java/com/themuffinman/app/*/mapper/*.java", source_root: "apps/themuffinman/src/main/java/com/themuffinman/app")
report = {generated_at: Time.now.utc.iso8601, mapper_count: rows.length, mapper_usages: rows}
AuditSupport.write_json("docs/audit-output/mapper-usage-audit.json", report)
AuditSupport.write_text("docs/audit-output/mapper-usage-audit-summary.md", "# Mapper Usage Audit\n\n- Mappers scanned: `#{rows.length}`\n")
puts "Mapper usage audit passed (#{rows.length} declared mappers; static callsites only)."
