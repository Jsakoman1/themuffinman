# frozen_string_literal: true

require "digest"
require_relative "compiled_feature_contract"
require_relative "generated_feature_manifest"

module Dora
  class GeneratedFeatureSafety
    def self.inspect!(manifest:, feature:, project_root:, expected_digests:, trace:, historic_migrations: [])
      declared = GeneratedFeatureManifest.validate!(manifest)
      model = CompiledFeatureContract.validate_type_mappings!(feature)
      fail!("generated feature project root is invalid") unless project_root.is_a?(String) && !project_root.empty?
      expected_paths = declared.fetch("outputs").map { |output| output.fetch("path") }
      fail!("generated feature expected digests must exactly match manifest outputs") unless expected_digests.is_a?(Hash) && expected_digests.keys.sort == expected_paths.sort && expected_digests.values.all? { |value| sha256?(value) }
      findings = []
      expected_paths.each do |path|
        absolute = safe_target!(project_root, path)
        if !File.file?(absolute)
          findings << finding("missing_output", path, "Manifest output does not exist.")
        elsif Digest::SHA256.file(absolute).hexdigest != expected_digests.fetch(path)
          findings << finding("content_digest_mismatch", path, "Output content differs from the declared generated content.")
        end
      end
      historical = Array(historic_migrations)
      fail!("generated feature historic migration paths are invalid") unless historical.all? { |path| safe_relative?(path) }
      (expected_paths & historical).each { |path| findings << finding("historic_migration_target", path, "Generated output would target a historic migration.") }
      findings.concat(trace_findings(model, trace))
      {"kind" => "dora_generated_feature_safety_report", "version" => 1, "findings" => findings, "safe_to_continue" => findings.empty?, "completion_boundary" => "This is a static generated-output inspection. It does not modify source, compile a project, run a database, or prove runtime or acceptance."}.freeze
    end

    def self.inspect_related_trace!(trace:, feature:)
      expected = GeneratedFeatureManifest.related_trace_path(feature)
      fail!("generated related trace is invalid") unless trace.is_a?(Hash) && trace["kind"] == "dora_related_resource_trace" && trace["capability"] == feature.fetch("capability") && trace.dig("relation", "confirmed") == true
      {"kind" => "dora_generated_related_resource_safety", "version" => 1, "safe_to_continue" => true, "trace_path" => expected, "completion_boundary" => "Related-resource inspection checks declared trace alignment only; it does not prove compilation, database behavior, browser behavior, or acceptance."}.freeze
    end

    def self.trace_findings(model, trace)
      fail!("generated feature trace is invalid") unless trace.is_a?(Hash)
      findings = []
      expected_operations = model.dig("api", "operations").map { |operation| operation.fetch("id") }
      fields = model.dig("entity", "fields").map { |field| field.fetch("id") }
      findings << finding("dto_trace_gap", "trace", "DTO trace fields differ from the confirmed entity fields.") unless Array(trace["dto_fields"]).sort == fields.sort
      findings << finding("api_trace_gap", "trace", "API trace operations differ from the confirmed API operations.") unless Array(trace["api_operations"]).sort == expected_operations.sort
      findings << finding("workflow_trace_gap", "trace", "Workflow trace differs from the confirmed workflow.") unless trace["workflow_id"] == model.dig("workflow", "id")
      findings << finding("permission_trace_gap", "trace", "Permission trace differs from the confirmed permission.") unless trace["permission_id"] == model.dig("permission", "id")
      findings
    end
    private_class_method :trace_findings
    def self.finding(id, path, message); {"id" => id, "path" => path, "message" => message}; end
    private_class_method :finding
    def self.safe_target!(root, path)
      fail!("generated feature manifest path is unsafe: #{path}") unless safe_relative?(path)
      absolute = File.expand_path(path, root)
      fail!("generated feature manifest path escapes project root: #{path}") unless absolute.start_with?("#{File.expand_path(root)}/")
      absolute
    end
    private_class_method :safe_target!
    def self.safe_relative?(value); value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative?
    def self.sha256?(value); value.is_a?(String) && value.match?(/\A[0-9a-f]{64}\z/); end
    private_class_method :sha256?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
