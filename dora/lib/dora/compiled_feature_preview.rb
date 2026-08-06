# frozen_string_literal: true

require "yaml"
require_relative "generated_feature_manifest"

module Dora
  class CompiledFeaturePreview
    def self.preview!(document)
      fail!("compiled feature preview input is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_compiled_feature_preview_input" && document["version"].to_i == 1
      %w[feature migration_version template_digests].each { |field| fail!("compiled feature preview input is missing #{field}") unless document.key?(field) }
      manifest = GeneratedFeatureManifest.build!(feature: document.fetch("feature"), migration_version: document.fetch("migration_version"), template_digests: document.fetch("template_digests"))
      {"kind" => "dora_compiled_feature_preview", "version" => 1, "manifest" => manifest, "blockers" => [], "completion_boundary" => "This preview is read-only. It does not create source files, compile a project, or resolve runtime or acceptance obligations."}.freeze
    rescue Psych::Exception => error
      fail!("compiled feature preview YAML is invalid: #{error.message}")
    end

    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
