#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
IDC_BIN = File.join(ROOT, "idc/bin/idc")
require File.join(ROOT, "idc/lib/idc/dossier")

request = {"kind" => "idc_research_request", "version" => 1, "id" => "preparedness-v1", "goal" => "Prepare an owner-readable V1 research dossier.", "scope" => "Local advisory material only.", "questions" => ["What remains unknown?", "Which stop condition applies?"], "allowed_source_kinds" => ["owner_input", "local_document"]}
manifest = {"kind" => "idc_source_manifest", "version" => 1, "id" => "preparedness-sources", "sources" => [{"id" => "owner-brief", "allowed_kind" => "owner_input", "locator" => "owner-input/brief.txt", "revision_or_digest" => "sha256:owner-brief", "observed_at" => "2026-08-12T11:00:00Z"}, {"id" => "local-notes", "allowed_kind" => "local_document", "locator" => "selected/notes.md", "revision_or_digest" => "sha256:notes", "observed_at" => "2026-08-12T11:05:00Z"}], "expected_evidence" => [{"id" => "budget", "expected_proof" => "Owner-approved budget", "disposition" => "not_provided"}]}
dossier = {"kind" => "idc_advisory_dossier", "version" => 1, "id" => "preparedness-dossier", "request_ref" => "preparedness-v1", "manifest_ref" => "preparedness-sources", "claims" => [{"id" => "goal", "status" => "owner_confirmed", "source_refs" => ["owner-brief"], "wording" => "The owner wants a simple household preparedness loop.", "assessment" => {"freshness" => "2026-08-12T11:00:00Z", "confidence" => "high"}}, {"id" => "budget-gap", "status" => "missing_context", "source_refs" => [], "wording" => "No owner-approved budget was supplied.", "missing_context_basis" => {"kind" => "manifest_expected_evidence", "expected_evidence_ref" => "budget"}}, {"id" => "mobile", "status" => "open_question", "source_refs" => [], "wording" => "The first frequent-use channel remains open."}, {"id" => "scope-conflict", "status" => "conflict", "source_refs" => ["owner-brief", "local-notes"], "wording" => "The selected materials disagree about initial scope."}], "stop_conditions" => ["Do not promote an architecture choice until the channel question is resolved."], "promotion_proposal" => {"kind" => "idc_promotion_proposal", "version" => 1, "dossier_ref" => "preparedness-dossier", "proposed_text" => "Owner may decide whether to promote selected statements through Dora.", "owner_action" => "owner_review_required"}}

first = Idc::Dossier.render(request: request, manifest: manifest, dossier: dossier)
abort "renderer is not deterministic" unless first == Idc::Dossier.render(request: request, manifest: manifest, dossier: dossier)
%w[owner_confirmed missing_context open_question conflict Source\ provenance Missing-context\ basis Stop\ conditions Owner\ action Traceability].each { |needle| abort "rendered dossier omits #{needle}" unless first.include?(needle) }
abort "renderer hid missing-context evidence" unless first.include?("expected_evidence_ref=budget")

Dir.mktmpdir("idc-renderer") do |directory|
  request_path, manifest_path, dossier_path, out_path = %w[request.yaml manifest.yaml dossier.yaml dossier.md].map { |name| File.join(directory, name) }
  File.write(request_path, YAML.dump(request)); File.write(manifest_path, YAML.dump(manifest)); File.write(dossier_path, YAML.dump(dossier))
  stdout, stderr, status = Open3.capture3("ruby", IDC_BIN, "render", "--request", request_path, "--manifest", manifest_path, "--dossier", dossier_path, "--out", out_path)
  abort "CLI render failed: #{stderr}" unless status.success? && stdout.include?("IDC advisory dossier rendered") && File.read(out_path) == first
  invalid = Marshal.load(Marshal.dump(dossier)); invalid.fetch("claims")[1].delete("missing_context_basis"); File.write(dossier_path, YAML.dump(invalid))
  _stdout, stderr, status = Open3.capture3("ruby", IDC_BIN, "render", "--request", request_path, "--manifest", manifest_path, "--dossier", dossier_path, "--out", out_path)
  abort "unfounded missing_context was accepted" if status.success?; abort "CLI did not report validation failure" unless stderr.include?("IDC validation failed")
end

source = [File.read(File.join(ROOT, "idc/lib/idc/request.rb")), File.read(File.join(ROOT, "idc/lib/idc/dossier.rb")), File.read(IDC_BIN)].join("\n")
{"Open3" => /\bOpen3\b/, "Net::HTTP" => /Net::HTTP/, "URI.open" => /URI\.open/, "Git command" => /["']git["']/, "Codex invocation" => /codex_invocation:\s*true/, "Bridge execution" => /bridge_execution:\s*true/}.each { |forbidden, pattern| abort "IDC runtime exposes forbidden capability #{forbidden}" if source.match?(pattern) }
abort "IDC runtime invokes a shell" if source.match?(/\bsystem\s*\(|\bexec\s*\(|\bspawn\s*\(/)
abort "IDC runtime traverses directories" if source.include?("Dir.glob") || source.include?("Dir.children")
puts "IDC dossier renderer test passed (deterministic explicit inputs, visible uncertainty, and no runtime authority)."
