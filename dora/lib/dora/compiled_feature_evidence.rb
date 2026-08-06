# frozen_string_literal: true

require_relative "generated_feature_manifest"

module Dora
  class CompiledFeatureEvidence
    def self.declare!(manifest)
      declared = GeneratedFeatureManifest.validate!(manifest)
      obligations = [
        obligation("static", "Run generated feature safety inspection.", "not_required"),
        obligation("compile", "Run the declared backend and frontend compile/test commands in a temporary consumer.", "explicit_approval_required"),
        obligation("database", "Run an isolated Docker/PostgreSQL migration scenario.", "explicit_approval_required"),
        obligation("runtime", "Run a project-owned runtime scenario.", "explicit_approval_required"),
        obligation("browser", "Run a project-owned Playwright scenario.", "explicit_approval_required"),
        obligation("acceptance", "Record project-owned business acceptance evidence.", "project_owned")
      ]
      {"kind" => "dora_compiled_feature_evidence_obligations", "version" => 1, "manifest_digest" => digest(declared), "obligations" => obligations, "acceptance_status" => "unresolved", "completion_boundary" => "Generated output creates obligations only. No static, compile, database, runtime, browser, or acceptance evidence is recorded by this declaration."}.freeze
    end

    def self.obligation(id, action, approval_gate); {"id" => id, "status" => "unresolved", "action" => action, "approval_gate" => approval_gate}; end
    private_class_method :obligation
    def self.digest(manifest); require "digest"; Digest::SHA256.hexdigest(Marshal.dump(manifest)); end
    private_class_method :digest
  end
end
