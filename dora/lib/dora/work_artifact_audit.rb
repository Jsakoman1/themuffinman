# frozen_string_literal: true

require "time"
require "yaml"

require_relative "work_artifact_schema"

module Dora
  # Classifies declared work artifacts without changing them. This is deliberately
  # an advisory input for diagnostics, not a lifecycle or repair mechanism.
  class WorkArtifactAudit
    RESULT_KIND = "dora_work_artifact_audit"
    CLASSIFICATIONS = %w[valid invalid_yaml unsupported_kind structurally_invalid].freeze

    def self.inspect!(project_root:, paths:, schema_path:, non_executable_paths: [], observed_at: Time.now.utc)
      root = File.realpath(project_root)
      timestamp = observed_at.utc.iso8601
      files = artifact_files!(root: root, paths: paths, non_executable_paths: non_executable_paths)
      findings = files.map { |path| classify(path: path, root: root, schema_path: schema_path, observed_at: timestamp) }

      {
        "kind" => RESULT_KIND,
        "version" => 1,
        "observed_at" => timestamp,
        "read_only" => true,
        "disposition" => "advisory",
        "findings" => findings
      }.freeze
    end

    def self.artifact_files!(root:, paths:, non_executable_paths:)
      declared = Array(paths)
      raise ArgumentError, "work artifact audit paths must be a non-empty list" if declared.empty?

      excluded = Array(non_executable_paths).map do |relative|
        raise ArgumentError, "non-executable work record path must be project-relative" unless relative.is_a?(String) && !relative.empty? && !relative.start_with?("/") && !relative.split("/").include?("..")

        absolute = File.expand_path(relative, root)
        raise ArgumentError, "non-executable work record path resolves outside project root" unless absolute == root || absolute.start_with?("#{root}/")

        absolute
      end

      declared.flat_map do |relative|
        raise ArgumentError, "work artifact audit path must be project-relative" unless relative.is_a?(String) && !relative.empty? && !relative.start_with?("/") && !relative.split("/").include?("..")

        absolute = File.expand_path(relative, root)
        raise ArgumentError, "work artifact audit path resolves outside project root" unless absolute == root || absolute.start_with?("#{root}/")

        if File.file?(absolute)
          yaml_file?(absolute) ? [absolute] : []
        elsif Dir.exist?(absolute)
          Dir[File.join(absolute, "**", "*.{yaml,yml}")]
        else
          []
        end
      end.uniq.sort.reject { |path| excluded.include?(path) }
    end
    private_class_method :artifact_files!

    def self.yaml_file?(path)
      %w[.yaml .yml].include?(File.extname(path))
    end
    private_class_method :yaml_file?

    def self.classify(path:, root:, schema_path:, observed_at:)
      artifact = YAML.load_file(path)
      classification = if artifact.is_a?(Hash) && !artifact["kind"].nil? && artifact_kind_supported?(artifact.fetch("kind"), schema_path)
                         WorkArtifactSchema.validate!(path, schema_path: schema_path)
                         "valid"
                       elsif artifact.is_a?(Hash) && artifact["kind"]
                         "unsupported_kind"
                       else
                         "structurally_invalid"
                       end
      finding(path: path, root: root, observed_at: observed_at, classification: classification)
    rescue Psych::Exception
      finding(path: path, root: root, observed_at: observed_at, classification: "invalid_yaml")
    rescue ArgumentError
      finding(path: path, root: root, observed_at: observed_at, classification: "structurally_invalid")
    end
    private_class_method :classify

    def self.artifact_kind_supported?(kind, schema_path)
      schema = YAML.load_file(schema_path)
      schema.fetch("artifacts", {}).values.any? { |definition| definition.is_a?(Hash) && definition["kind"] == kind }
    end
    private_class_method :artifact_kind_supported?

    def self.finding(path:, root:, observed_at:, classification:)
      raise ArgumentError, "work artifact classification is invalid" unless CLASSIFICATIONS.include?(classification)

      {
        "source_reference" => path.delete_prefix("#{root}/"),
        "observed_at" => observed_at,
        "read_only" => true,
        "disposition" => "advisory",
        "classification" => classification
      }.freeze
    end
    private_class_method :finding
  end
end
