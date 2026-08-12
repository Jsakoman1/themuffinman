#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
DORA_BIN = File.join(ROOT, "bin/dora")
require_relative "../lib/dora/idc_envelope"
require_relative "../lib/dora/project_initializer"

def write_yaml(root, path, value)
  absolute = File.join(root, path)
  FileUtils.mkdir_p(File.dirname(absolute))
  File.write(absolute, YAML.dump(value))
end

Dir.mktmpdir("dora-idc-envelope") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "safe-idc", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief = YAML.load_file(File.join(root, "docs/product-brief.yaml")); brief["product"] = "Safe IDC"; File.write(File.join(root, "docs/product-brief.yaml"), YAML.dump(brief))
  write_yaml(root, "docs/decision-log.yaml", {"kind" => "dora_decision_log", "version" => 1, "entries" => [{"id" => "keep-local", "decision" => "Keep IDC local and advisory.", "status" => "accepted", "domain_references" => ["docs/domain-library.yaml"], "plan_references" => ["docs/work/idc-plan.yaml"], "evidence_references" => ["docs/product-brief.yaml"]}]})
  selector = {"kind" => "dora_idc_envelope_selection", "version" => 1, "project_fields" => ["project", "state", "open_decisions", "references"], "decision_ids" => ["keep-local"], "artifact_references" => ["docs/product-brief.yaml", "docs/decision-log.yaml"]}
  observed_at = "2026-08-12T12:00:00Z"
  first = Dora::IdcEnvelope.export!(adapter_path: File.join(root, ".dora/project.yaml"), selection: selector, observed_at: observed_at)
  second = Dora::IdcEnvelope.export!(adapter_path: File.join(root, ".dora/project.yaml"), selection: selector, observed_at: observed_at)
  abort "IDC envelope is not deterministic for fixed input/time" unless first == second
  abort "IDC envelope output is not read-only advisory" unless first.values_at("kind", "read_only", "disposition") == ["dora_idc_read_envelope", true, "advisory"]
  abort "IDC envelope leaked unselected ProjectReadModel fields" unless first.fetch("project_read_model").keys.sort == %w[open_decisions project references state]
  abort "IDC envelope lost selected accepted decision" unless first.fetch("selected_decisions") == [{"id" => "keep-local", "statement" => "Keep IDC local and advisory.", "status" => "accepted", "reference" => "docs/decision-log.yaml"}]
  abort "IDC envelope did not retain digest-bearing provenance" unless first.fetch("sources").all? { |source| source.keys.sort == %w[allowed_kind id locator observed_at revision_or_digest] && source.fetch("revision_or_digest").start_with?("sha256:") }
  abort "IDC envelope leaked raw source or root" if first.to_s.include?(root) || first.to_s.include?("user_problem") || first.to_s.include?("project_memory")

  selector_path = File.join(root, "selector.yaml"); File.write(selector_path, YAML.dump(selector))
  stdout, stderr, status = Open3.capture3(DORA_BIN, "idc-envelope", File.join(root, ".dora/project.yaml"), selector_path, chdir: ROOT)
  abort "IDC envelope CLI failed: #{stderr}" unless status.success?
  cli = YAML.safe_load(stdout)
  abort "IDC envelope CLI changed output shape" unless cli.fetch("kind") == "dora_idc_read_envelope" && cli.fetch("read_only")

  invalid = selector.merge("artifact_references" => ["../../.env"])
  begin
    Dora::IdcEnvelope.export!(adapter_path: File.join(root, ".dora/project.yaml"), selection: invalid, observed_at: observed_at)
    abort "arbitrary source reference was accepted"
  rescue ArgumentError
    nil
  end
  invalid = selector.merge("path" => "docs/decision-log.yaml")
  begin
    Dora::IdcEnvelope.export!(adapter_path: File.join(root, ".dora/project.yaml"), selection: invalid, observed_at: observed_at)
    abort "authority selector field was accepted"
  rescue ArgumentError
    nil
  end
end

source = File.read(File.join(ROOT, "lib/dora/idc_envelope.rb"))
{"File.write" => /File\.write/, "Open3" => /\bOpen3\b/, "system invocation" => /\bsystem\s*\(/, "Net::HTTP" => /Net::HTTP/, "URI.open" => /URI\.open/, "Git command" => /["']git["']/, "Codex invocation" => /codex_invocation:\s*true/}.each { |forbidden, pattern| abort "IDC envelope exposes forbidden capability #{forbidden}" if source.match?(pattern) }
puts "Dora IDC envelope test passed (fixed selector, sanitized provenance, stdout-only CLI, and no authority)."
