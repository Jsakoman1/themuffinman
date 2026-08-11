# frozen_string_literal: true

require "yaml"

module Dora
  class ArtifactPolicy
    def self.load!(path)
      policy = policy!(path)
      policy.fetch("generated_roots").freeze
    end

    def self.work_artifact_audit_paths!(path)
      work_artifact_audit_config!(path).fetch("paths")
    end

    def self.work_artifact_audit_config!(path)
      policy = policy!(path)
      audit = policy["work_artifact_audit"]
      return {"paths" => [], "non_executable_paths" => []}.freeze if audit.nil?

      raise ArgumentError, "work artifact audit must be a mapping" unless audit.is_a?(Hash)
      paths = Array(audit["paths"])
      raise ArgumentError, "work artifact audit has no paths" if paths.empty? || paths.any? { |entry| !safe_relative_path?(entry) }
      non_executable_paths = Array(audit["non_executable_records"]).map do |record|
        raise ArgumentError, "non-executable work record must be a mapping" unless record.is_a?(Hash)

        path = record["path"]
        reason = record["reason"]
        raise ArgumentError, "non-executable work record path is invalid" unless safe_relative_path?(path)
        raise ArgumentError, "non-executable work record reason is invalid" unless reason.is_a?(String) && !reason.empty?
        raise ArgumentError, "non-executable work record must be within an audited path" unless paths.any? { |root| path == root || path.start_with?("#{root}/") }

        path
      end

      {"paths" => paths.freeze, "non_executable_paths" => non_executable_paths.freeze}.freeze
    end

    def self.policy!(path)
      policy = YAML.load_file(path)
      raise ArgumentError, "artifact policy kind is invalid" unless policy["kind"] == "dora_artifact_policy" && policy["version"].to_i == 1
      roots = Array(policy["generated_roots"])
      raise ArgumentError, "artifact policy has no generated roots" if roots.empty? || roots.any? { |root| !root.is_a?(String) || root.empty? || root.start_with?("/") || root.include?("..") }
      raise ArgumentError, "artifact policy must prohibit implicit deletion" unless policy["deletion_authority"] == "project_authorized_command"
      policy
    end
    private_class_method :policy!

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?
  end
end
