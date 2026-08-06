#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require_relative "../lib/dora/compiled_feature_evidence"

digests = %w[flyway-migration spring-jdbc-feature api-contract vue-feature manifest evidence-obligations].to_h { |id| [id, Digest::SHA256.hexdigest(id)] }
manifest = {"kind" => "dora_generated_feature_manifest", "version" => 1, "generator_version" => "1.6", "confirmed_input_digest" => Digest::SHA256.hexdigest("feature"), "migration_version" => "1", "template_digests" => digests, "outputs" => [{"id" => "generated-manifest", "path" => ".dora/generated-features/record-supply.yaml", "template" => "manifest", "template_digest" => digests.fetch("manifest")}], "unresolved_obligations" => [{"id" => "compile", "reason" => "Not compiled."}]}
report = Dora::CompiledFeatureEvidence.declare!(manifest)
abort "acceptance was fabricated" unless report.fetch("acceptance_status") == "unresolved" && report.fetch("obligations").all? { |obligation| obligation.fetch("status") == "unresolved" }
compile = report.fetch("obligations").find { |obligation| obligation.fetch("id") == "compile" }
abort "compile approval gate is missing" unless compile.fetch("approval_gate") == "explicit_approval_required"
puts "Dora compiled feature evidence test passed (all generated evidence remains unresolved and external execution requires explicit approval)."
