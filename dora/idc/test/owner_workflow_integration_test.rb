#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
IDC_BIN = File.join(ROOT, "idc/bin/idc")
DORA_BIN = File.join(ROOT, "bin/dora")

require File.join(ROOT, "lib/dora/project_initializer")
require File.join(ROOT, "lib/dora/idc_owner_started_renderer")
require File.join(ROOT, "bridge/lib/dora_bridge/server")
require File.join(ROOT, "idc/lib/idc/dossier")

def write_yaml(path, document)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def bridge_request(server, id, name, arguments)
  server.handle({"jsonrpc" => "2.0", "id" => id, "method" => "tools/call", "params" => {"name" => name, "arguments" => arguments}})
end

request = {"kind" => "idc_research_request", "version" => 1, "id" => "owner-started", "goal" => "Prepare an advisory owner readback.", "scope" => "Only explicitly supplied local material.", "questions" => ["What remains open?"], "allowed_source_kinds" => ["owner_input", "dora_read_envelope"]}
manifest = {"kind" => "idc_source_manifest", "version" => 1, "id" => "owner-started-sources", "sources" => [{"id" => "owner-brief", "allowed_kind" => "owner_input", "locator" => "owner/brief.txt", "revision_or_digest" => "sha256:owner-brief", "observed_at" => "2026-08-12T12:00:00Z"}], "expected_evidence" => [{"id" => "budget", "expected_proof" => "A confirmed budget.", "disposition" => "not_provided"}]}
dossier = {"kind" => "idc_advisory_dossier", "version" => 1, "id" => "owner-started-dossier", "request_ref" => "owner-started", "manifest_ref" => "owner-started-sources", "claims" => [{"id" => "goal", "status" => "owner_confirmed", "source_refs" => ["owner-brief"], "wording" => "The owner wants a read-only advisory dossier."}, {"id" => "budget", "status" => "missing_context", "source_refs" => [], "wording" => "No budget was supplied.", "missing_context_basis" => {"kind" => "manifest_expected_evidence", "expected_evidence_ref" => "budget"}}], "stop_conditions" => ["Do not promote an unconfirmed budget."], "promotion_proposal" => {"kind" => "idc_promotion_proposal", "version" => 1, "dossier_ref" => "owner-started-dossier", "proposed_text" => "Owner may review selected advisory statements.", "owner_action" => "owner_review_required"}}
triage = {"kind" => "dora_idc_triage_request", "version" => 1, "id" => "owner-started", "request_shape" => "wide_research", "profile" => "research_dossier", "source_scope" => "explicit_owner_selected_only", "authorization_scope" => "current_request_only", "owner_authorization" => "authorize_local_idc_render"}

Dir.mktmpdir("idc-owner-workflow") do |root|
  request_path = File.join(root, "request.yaml")
  manifest_path = File.join(root, "manifest.yaml")
  dossier_path = File.join(root, "dossier.yaml")
  triage_path = File.join(root, "triage.yaml")
  output_path = File.join(root, "owner-readable.md")
  write_yaml(request_path, request)
  write_yaml(manifest_path, manifest)
  write_yaml(dossier_path, dossier)
  write_yaml(triage_path, triage)

  stdout, stderr, status = Open3.capture3(DORA_BIN, "idc-render", "--triage", triage_path, "--request", request_path, "--manifest", manifest_path, "--dossier", dossier_path, "--out", output_path)
  abort "owner-started local Dora IDC command failed: #{stderr}" unless status.success? && stdout.include?("status: rendered")
  abort "owner-started local IDC command did not reuse the deterministic renderer" unless File.read(output_path) == Idc::Dossier.render(request: request, manifest: manifest, dossier: dossier)

  project_root = File.join(root, "bridge-project")
  Dora::ProjectInitializer.initialize!(project_root, project_id: "owner-started-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  registry_path = File.join(root, "bridge-projects.yaml")
  write_yaml(registry_path, {"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "owner-started-project", "adapter_path" => "bridge-project/.dora/project.yaml"}]})
  bridge = DoraBridge::Server.new(DoraBridge::ProjectRegistry.load!(registry_path))
  envelope = bridge_request(bridge, 1, "get_idc_envelope", {"project" => "owner-started-project"}).dig("result", "structuredContent")
  abort "Bridge did not return the fixed sanitized IDC envelope" unless envelope.fetch("kind") == "dora_idc_read_envelope" && envelope.fetch("read_only") == true && envelope.fetch("selection") == {"project_fields" => DoraBridge::Server::IDC_ENVELOPE_SELECTION.fetch("project_fields").sort, "decision_ids" => [], "artifact_references" => []}
  abort "Bridge IDC envelope leaked a private root" if envelope.to_s.include?(root)
  unauthorized = bridge_request(bridge, 2, "get_idc_envelope", {"project" => "owner-started-project", "selector" => {"path" => "../../.env"}})
  abort "Bridge IDC envelope accepted caller-selected context" unless unauthorized.dig("error", "code") == -32602
end

source = File.read(File.join(ROOT, "bridge/lib/dora_bridge/server.rb"))
abort "Bridge IDC profile invokes the local IDC command" if source.include?("idc/bin/idc") || source.include?("Idc::Dossier")
abort "Bridge IDC profile exposes a process invocation" if source.match?(/\bOpen3\b|\bsystem\s*\(|\bexec\s*\(|\bspawn\s*\(/)

puts "IDC owner workflow integration test passed (owner-started local renderer, fixed Bridge envelope, and no Bridge execution)."
