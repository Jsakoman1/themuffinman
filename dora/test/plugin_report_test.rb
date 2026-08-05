#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/plugin_report"

report = Dora::PluginReport.build!(plugin_id: "alpha-check", inputs: {"roots" => ["src"]}, findings: [{"id" => "alpha", "status" => "passed"}], output: {"kind" => "static-analysis-report", "path" => "reports/alpha.json"})
abort "plugin report kind is missing" unless report["kind"] == "dora_plugin_report"
abort "plugin report lost inputs or findings" unless report["inputs"].fetch("roots") == ["src"] && report["findings"].length == 1
abort "plugin report claims completion" unless report["completion_boundary"].include?("does not prove")

begin
  Dora::PluginReport.build!(plugin_id: "bad id", inputs: {}, findings: [], output: {"kind" => "static-analysis-report"})
  abort "plugin report accepted invalid id"
rescue ArgumentError
  # expected
end

puts "Dora plugin report contract test passed (diagnostic output only)."
