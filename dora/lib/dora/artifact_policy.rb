# frozen_string_literal: true

require "yaml"

module Dora
  class ArtifactPolicy
    def self.load!(path)
      policy = policy!(path)
      policy.fetch("generated_roots").freeze
    end

    def self.work_artifact_audit_paths!(path)
      policy = policy!(path)
      audit = policy["work_artifact_audit"]
      return [] if audit.nil?

      raise ArgumentError, "work artifact audit must be a mapping" unless audit.is_a?(Hash)
      paths = Array(audit["paths"])
      raise ArgumentError, "work artifact audit has no paths" if paths.empty? || paths.any? { |entry| !safe_relative_path?(entry) }

      paths.freeze
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
