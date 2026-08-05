#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
context = {"kind" => "dora_confirmed_capability_context", "version" => 1, "capability" => {"id" => "record-supply", "title" => "Record a supply", "confirmed" => true}, "decisions" => {"data_safety" => "Confirmed", "workflow" => "Confirmed", "permission" => "Confirmed", "technical" => "Confirmed"}}
Dir.mktmpdir("dora-vertical-slice") do |sandbox|
  path = File.join(sandbox, "context.yaml")
  File.write(path, YAML.dump(context))
  output, status = Open3.capture2e(CLI, "vertical-slice", path, "--format", "json", chdir: ROOT)
  abort "vertical slice command failed: #{output}" unless status.success?
  payload = JSON.parse(output).fetch("payload")
  abort "ready context was blocked" unless payload.dig("readiness", "ready_to_plan")
  abort "proposal omitted runtime evidence surface" unless payload.dig("proposal", "surfaces", "runtime_evidence").first.include?("record-supply")
  blocked = Marshal.load(Marshal.dump(context)); blocked.fetch("decisions").delete("permission"); File.write(path, YAML.dump(blocked))
  output, status = Open3.capture2e(CLI, "vertical-slice", path, "--format", "json", chdir: ROOT)
  abort "blocked vertical slice command failed: #{output}" unless status.success?
  gaps = JSON.parse(output).dig("payload", "readiness", "blocking_gaps")
  abort "permission blocker was omitted" unless gaps.any? { |gap| gap.fetch("category") == "permission" }
end
puts "Dora vertical slice command test passed (read-only proposal output preserves exact readiness blockers)."
