#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/guided_agent_entrypoint"
require_relative "../lib/dora/codex_context_packet"
require_relative "../lib/dora/domain_capability_graph"
require_relative "../lib/dora/project_convention_check"
require_relative "../lib/dora/compiled_feature_renderers"
require_relative "../lib/dora/generated_feature_manifest"
require_relative "../lib/dora/generated_feature_safety"
require_relative "../lib/dora/capability_proof_packet"

ROOT = File.expand_path("..", __dir__)
guided = YAML.load_file(File.join(ROOT, "test/fixtures/guided-agent-entrypoint.yaml"))
context = YAML.load_file(File.join(ROOT, "test/fixtures/codex-context-packet.yaml"))
graph = YAML.load_file(File.join(ROOT, "test/fixtures/domain-capability-graph.yaml"))
related = YAML.load_file(File.join(ROOT, "test/fixtures/related-resource-feature.yaml"))
matrix = YAML.load_file(File.join(ROOT, "templates/capability-proof-matrix.yaml"))
matrix.merge!("capability" => "record-item", "assertions" => [{"id" => "item-created", "statement" => "An authorized actor can record an item.", "confirmed" => true}], "obligations" => [{"id" => "item-unit", "assertion_id" => "item-created", "evidence_class" => "unit", "status" => "required", "boundary" => "A project-owned unit test must prove this assertion."}, {"id" => "item-browser", "assertion_id" => "item-created", "evidence_class" => "browser_runtime", "status" => "unresolved", "boundary" => "Browser execution requires explicit approval."}])

Dir.mktmpdir("dora-codex-acceleration-consumer") do |root|
  FileUtils.mkdir_p(File.join(root, ".dora"))
  %w[guided context graph related proof].zip([guided.fetch("incomplete"), context, graph, related, matrix]).each do |id, document|
    File.write(File.join(root, ".dora", "#{id}.yaml"), YAML.dump(document))
  end
  abort "guided consumer did not stop at one question" unless Dora::GuidedAgentEntrypoint.resolve!(guided.fetch("incomplete")).fetch("resolution") == "ask_one_confirmed_question"
  abort "context consumer lost allowed paths" unless Dora::CodexContextPacket.build!(context).fetch("allowed_paths").length == 2
  graph_report = Dora::DomainCapabilityGraph.report!(graph)
  abort "graph consumer selected wrong safe capability" unless graph_report.dig("next_safe_capability", "id") == "item-catalog"
  abort "graph consumer did not preserve blocker" unless graph_report.fetch("blocking_gaps").any? { |gap| gap.fetch("category") == "open_decision" }
  profile = {"kind" => "dora_project_convention_profile", "version" => 1, "project_id" => "consumer", "backend_root" => "backend", "frontend_root" => "frontend", "java_package" => "example.records", "migration_directory" => "backend/src/main/resources/db/migration", "api_contract_directory" => "docs/api", "frontend_feature_directory" => "frontend/src/features", "test_commands" => [{"id" => "backend-test", "command" => "mvn test"}], "documentation_root" => "docs", "confirmation" => true, "completion_boundary" => "Declared compatibility input only; it does not generate a feature."}
  outputs = Dora::CompiledFeatureRenderers.render_related_resource_packet!(document: related)
  manifest = {"outputs" => outputs.keys.reject { |path| path.end_with?("-relation.yaml") }.map { |path| {"id" => path.include?("src/main/java") ? "java-model" : path.include?("src/test/java") ? "junit" : path.include?("docs/api") ? "api-contract" : path.include?("frontend") ? "vue-client" : "capability-doc", "path" => path } }}
  report = Dora::ProjectConventionCheck.inspect!(profile: profile, manifest: manifest)
  abort "consumer convention check rejected declared output roots: #{report.fetch("findings")}" unless report.fetch("compatible")
  trace_path = Dora::GeneratedFeatureManifest.related_trace_path(related.fetch("feature"))
  trace = YAML.load(outputs.fetch(trace_path))
  abort "consumer relation trace was unsafe" unless Dora::GeneratedFeatureSafety.inspect_related_trace!(trace: trace, feature: related.fetch("feature")).fetch("safe_to_continue")
  abort "consumer proof packet lost approval gate" unless Dora::CapabilityProofPacket.build!(matrix).fetch("approval_gates").length == 1
  abort "consumer accidentally created product source" unless Dir.glob(File.join(root, "**", "*")).all? { |path| path.include?(".dora") || File.directory?(path) }
end

puts "Dora independent Codex acceleration consumer test passed (guided context, graph, conventions, related preview, and proof obligations stay local and declared)."
