#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
DORA_BIN = File.join(ROOT, "bin/dora")
require_relative "../lib/dora/idc_owner_started_renderer"

def write_yaml(path, value)
  File.write(path, YAML.dump(value))
end

def triage(authorization: "authorize_local_idc_render")
  {"kind" => "dora_idc_triage_request", "version" => 1, "id" => "owner-render", "request_shape" => "wide_research", "profile" => "research_dossier", "source_scope" => "explicit_owner_selected_only", "authorization_scope" => "current_request_only", "owner_authorization" => authorization}
end

request = {"kind" => "idc_research_request", "version" => 1, "id" => "owner-render", "goal" => "Prepare a bounded advisory dossier.", "scope" => "Explicit local input only.", "questions" => ["What remains unknown?"], "allowed_source_kinds" => ["owner_input"]}
manifest = {"kind" => "idc_source_manifest", "version" => 1, "id" => "owner-render-sources", "sources" => [{"id" => "owner", "allowed_kind" => "owner_input", "locator" => "owner/brief.txt", "revision_or_digest" => "sha256:owner", "observed_at" => "2026-08-12T14:00:00Z"}], "expected_evidence" => []}
dossier = {"kind" => "idc_advisory_dossier", "version" => 1, "id" => "owner-render-dossier", "request_ref" => "owner-render", "manifest_ref" => "owner-render-sources", "claims" => [{"id" => "goal", "status" => "owner_confirmed", "source_refs" => ["owner"], "wording" => "The owner requests a local advisory render."}], "promotion_proposal" => {"kind" => "idc_promotion_proposal", "version" => 1, "dossier_ref" => "owner-render-dossier", "proposed_text" => "Owner review remains required.", "owner_action" => "owner_review_required"}}

Dir.mktmpdir("dora-idc-owner-render") do |root|
  triage_path = File.join(root, "triage.yaml")
  request_path = File.join(root, "request.yaml")
  manifest_path = File.join(root, "manifest.yaml")
  dossier_path = File.join(root, "dossier.yaml")
  output_path = File.join(root, "dossier.md")
  write_yaml(triage_path, triage)
  write_yaml(request_path, request)
  write_yaml(manifest_path, manifest)
  write_yaml(dossier_path, dossier)

  result = Dora::IdcOwnerStartedRenderer.run!(triage_path: triage_path, request_path: request_path, manifest_path: manifest_path, dossier_path: dossier_path, output_path: output_path, root: ROOT)
  abort "local wrapper result is incomplete" unless result.values_at("kind", "status", "profile", "output") == ["dora_idc_local_render_result", "rendered", "research_dossier", "dossier.md"]
  abort "local wrapper did not render the advisory dossier" unless File.read(output_path).include?("IDC Advisory Dossier: owner-render-dossier")
  abort "local wrapper leaked an absolute output path" if result.to_s.include?(root)

  cli_output = File.join(root, "cli-dossier.md")
  stdout, stderr, status = Open3.capture3(DORA_BIN, "idc-render", "--triage", triage_path, "--request", request_path, "--manifest", manifest_path, "--dossier", dossier_path, "--out", cli_output, "--format", "json", chdir: ROOT)
  abort "Dora IDC render command failed: #{stderr}" unless status.success?
  envelope = JSON.parse(stdout)
  abort "Dora IDC render command did not return local-mutation envelope" unless envelope.values_at("outcome", "side_effect") == ["success", "local_mutation"] && envelope.dig("payload", "status") == "rendered"

  write_yaml(triage_path, triage(authorization: "not_granted"))
  begin
    Dora::IdcOwnerStartedRenderer.run!(triage_path: triage_path, request_path: request_path, manifest_path: manifest_path, dossier_path: dossier_path, output_path: output_path, root: ROOT)
    abort "unapproved triage started local rendering"
  rescue ArgumentError
    nil
  end
  write_yaml(triage_path, triage)
  begin
    Dora::IdcOwnerStartedRenderer.run!(triage_path: triage_path, request_path: request_path, manifest_path: manifest_path, dossier_path: dossier_path, output_path: request_path, root: ROOT)
    abort "local wrapper overwrote an input"
  rescue ArgumentError
    nil
  end
end

source = File.read(File.join(ROOT, "lib/dora/idc_owner_started_renderer.rb"))
abort "local wrapper invokes a shell" if source.match?(/\bsystem\s*\(|\bexec\s*\(|\bspawn\s*\(/)
abort "local wrapper exposes external authority" if source.match?(/Net::HTTP|URI\.open|["']git["']|["']codex["']|codex_invocation/)
abort "local wrapper invokes a non-fixed IDC entrypoint" unless source.include?("idc/bin/idc") && source.include?("RbConfig.ruby")

puts "Dora owner-started IDC renderer test passed (explicit authorization, fixed local entrypoint, bounded output, and no shell)."
