#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require_relative "../audit_support"

linker = File.join(AuditSupport::REPO_ROOT, "scripts/audits/audit-endpoint-callsite-linker.rb")
_stdout, stderr, status = Open3.capture3("ruby", linker, chdir: AuditSupport::REPO_ROOT)
abort "endpoint linker failed: #{stderr}" unless status.success?

report = JSON.parse(File.read(File.join(AuditSupport::REPO_ROOT, "docs/audit-output/endpoint-callsite-linker.json")))
classifications = report.fetch("endpoints").map do |entry|
  classification = if entry.fetch("frontend_matches").any?
    "web_source_linked"
  elsif entry.fetch("full_path").start_with?("/admin/") || entry.fetch("domain") == "agent"
    "admin_or_agent"
  elsif entry.fetch("domain") == "nativehandoff" || entry.fetch("full_path").start_with?("/native/")
    "native_handoff"
  else
    "unclassified_non_web"
  end
  entry.slice("endpoint_id", "domain", "backend_controller", "full_path").merge("consumer_evidence_class" => classification)
end

counts = classifications.group_by { |entry| entry.fetch("consumer_evidence_class") }.transform_values(&:length)
abort "endpoint classification did not cover every endpoint" unless classifications.length == report.fetch("endpoint_count")
abort "missing explicit unclassified queue" unless counts.key?("unclassified_non_web")

AuditSupport.write_json("docs/audit-output/interface-evidence-reconciliation.json", {
  generated_at: Time.now.utc.iso8601,
  source_report: "docs/audit-output/endpoint-callsite-linker.json",
  endpoint_count: classifications.length,
  classifications: counts,
  unclassified_endpoints: classifications.select { |entry| entry["consumer_evidence_class"] == "unclassified_non_web" }
})

puts "Interface evidence reconciliation audit passed"
puts "  endpoints classified: #{classifications.length}"
counts.sort.each { |kind, count| puts "  #{kind}: #{count}" }
