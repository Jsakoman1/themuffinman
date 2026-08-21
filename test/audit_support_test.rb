#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "tmpdir"
require_relative "../scripts/audit_support"

Dir.mktmpdir("audit-support") do |root|
  json_path = "reports/result.json"
  text_path = "reports/result.md"

  abort "JSON report path was not returned" unless AuditSupport.write_json(json_path, {status: "passed"}, root: root) == json_path
  abort "JSON report content is invalid" unless JSON.parse(File.read(File.join(root, json_path))) == {"status" => "passed"}
  abort "text report path was not returned" unless AuditSupport.write_text(text_path, "passed\n", root: root) == text_path
  abort "text report content is invalid" unless File.read(File.join(root, text_path)) == "passed\n"

  ["../outside.json", "/tmp/outside.json", ""].each do |unsafe_path|
    begin
      AuditSupport.write_text(unsafe_path, "unsafe", root: root)
      abort "unsafe report path was accepted: #{unsafe_path.inspect}"
    rescue ArgumentError
      nil
    end
  end

  begin
    AuditSupport.write_text("reports/not-text.md", {}, root: root)
    abort "non-string text report was accepted"
  rescue ArgumentError
    nil
  end
end

puts "Audit support test passed (local atomic reports preserve serialization and path containment without vendored Dora source)."
