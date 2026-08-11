#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/work_artifact_audit"

ROOT = File.expand_path("..", __dir__)
SCHEMA = File.join(ROOT, "templates/work-artifact-schema.yaml")
OBSERVED_AT = Time.utc(2026, 8, 10, 12, 0, 0)

def write_yaml(path, value)
  File.write(path, YAML.dump(value))
end

Dir.mktmpdir("dora-work-artifact-audit") do |sandbox|
  work = File.join(sandbox, "docs/work")
  Dir.mkdir(File.join(sandbox, "docs"))
  Dir.mkdir(work)

  valid = File.join(work, "valid.yaml")
  malformed = File.join(work, "malformed.yaml")
  unsupported = File.join(work, "unsupported.yaml")
  incomplete = File.join(work, "incomplete.yaml")
  historical = File.join(work, "historical-review.yaml")
  write_yaml(valid, {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Delivery", "status" => "draft", "children" => ["docs/work/delivery.yaml"]})
  File.write(malformed, "kind: [\n")
  write_yaml(unsupported, {"kind" => "review_note", "version" => 1})
  write_yaml(incomplete, {"kind" => "work", "version" => 1, "id" => "incomplete"})
  File.write(historical, "kind: [\n")
  before = [valid, malformed, unsupported, incomplete, historical].to_h { |path| [path, File.binread(path)] }

  report = Dora::WorkArtifactAudit.inspect!(project_root: sandbox, paths: ["docs/work"], non_executable_paths: ["docs/work/historical-review.yaml"], schema_path: SCHEMA, observed_at: OBSERVED_AT)
  abort "audit kind is invalid" unless report.fetch("kind") == "dora_work_artifact_audit" && report.fetch("version") == 1
  abort "audit result is not explicitly advisory" unless report.fetch("read_only") && report.fetch("disposition") == "advisory" && report.fetch("observed_at") == OBSERVED_AT.iso8601
  classifications = report.fetch("findings").to_h { |finding| [finding.fetch("source_reference"), finding.fetch("classification")] }
  expected = {"docs/work/valid.yaml" => "valid", "docs/work/malformed.yaml" => "invalid_yaml", "docs/work/unsupported.yaml" => "unsupported_kind", "docs/work/incomplete.yaml" => "structurally_invalid"}
  abort "unexpected classifications: #{classifications.inspect}" unless classifications == expected
  abort "non-executable historical record was audited as work" if classifications.key?("docs/work/historical-review.yaml")
  abort "finding provenance is incomplete" unless report.fetch("findings").all? { |finding| finding.fetch("read_only") && finding.fetch("disposition") == "advisory" && finding.fetch("observed_at") == OBSERVED_AT.iso8601 }
  abort "audit changed a fixture" unless before.all? { |path, content| File.binread(path) == content }
end

puts "Dora work artifact audit test passed (read-only classification and provenance)."
