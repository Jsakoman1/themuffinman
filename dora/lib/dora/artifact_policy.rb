# frozen_string_literal: true

require "yaml"

module Dora
  class ArtifactPolicy
    def self.load!(path)
      policy = YAML.load_file(path)
      raise ArgumentError, "artifact policy kind is invalid" unless policy["kind"] == "dora_artifact_policy" && policy["version"].to_i == 1
      roots = Array(policy["generated_roots"])
      raise ArgumentError, "artifact policy has no generated roots" if roots.empty? || roots.any? { |root| !root.is_a?(String) || root.empty? || root.start_with?("/") || root.include?("..") }
      raise ArgumentError, "artifact policy must prohibit implicit deletion" unless policy["deletion_authority"] == "project_authorized_command"
      roots.freeze
    end
  end
end
