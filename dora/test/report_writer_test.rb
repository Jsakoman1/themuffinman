#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require_relative "../lib/dora/report_writer"

Dir.mktmpdir("dora-report-writer") do |root|
  Dora::ReportWriter.write_json!(root: root, relative_path: "reports/result.json", payload: {"result" => "passed"})
  Dora::ReportWriter.write_text!(root: root, relative_path: "reports/summary.md", content: "# Summary\n")
  abort "JSON report was not written" unless File.read(File.join(root, "reports/result.json")).include?("passed")
  abort "Markdown report was not written" unless File.read(File.join(root, "reports/summary.md")) == "# Summary\n"
  begin
    Dora::ReportWriter.write_text!(root: root, relative_path: "../outside.md", content: "no")
    abort "writer accepted an unsafe path"
  rescue ArgumentError
    # expected
  end
end

puts "Dora report writer test passed (portable atomic diagnostic reports)."
