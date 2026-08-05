# frozen_string_literal: true

require "yaml"

module Dora
  class PluginContract
    VERSION_WITH_EXECUTION_POLICY = 2
    TRUST_BY_EXECUTION_KIND = {"entrypoint" => "project_trusted", "builtin" => "dora_builtin"}.freeze
    REQUIRED_UNSUPPORTED_GUARANTEES = %w[filesystem_isolation network_isolation credential_isolation resource_sandbox].freeze
    MAX_TIMEOUT_SECONDS = 300

    def self.validate!(manifest_path)
      manifest = YAML.load_file(manifest_path)
      fail!("plugin manifest kind is invalid") unless manifest.is_a?(Hash) && manifest["kind"] == "dora_plugin_manifest" && [1, VERSION_WITH_EXECUTION_POLICY].include?(manifest["version"].to_i)
      policy_required = manifest["version"].to_i >= VERSION_WITH_EXECUTION_POLICY
      plugins = Array(manifest["plugins"])
      fail!("plugin manifest has no plugins") if plugins.empty?
      ids = plugins.map { |plugin| plugin["id"] }
      fail!("plugin ids must be unique and non-empty") if ids.any? { |id| id.to_s.strip.empty? } || ids.uniq.length != ids.length

      plugins.each do |plugin|
        %w[id source_roots inputs output].each { |field| fail!("plugin #{plugin["id"] || "<unknown>"} is missing #{field}") unless present?(plugin[field]) }
        entrypoint = plugin["entrypoint"]
        builtin = plugin["builtin"]
        fail!("plugin #{plugin["id"]} must declare exactly one of entrypoint or builtin") unless present?(entrypoint) ^ present?(builtin)
        fail!("plugin #{plugin["id"]} entrypoint is invalid") if present?(entrypoint) && !safe_relative_path?(entrypoint)
        fail!("plugin #{plugin["id"]} builtin is invalid") if present?(builtin) && !builtin.match?(/\A[a-z][a-z0-9-]*\z/)
        fail!("plugin #{plugin["id"]} source_roots must be a list") unless plugin["source_roots"].is_a?(Array)
        plugin["source_roots"].each do |root|
          fail!("plugin #{plugin["id"]} source root is invalid") unless root.is_a?(Hash) && root["id"].to_s.match?(/\A[a-z0-9][a-z0-9_-]*\z/) && safe_relative_path?(root["path"])
        end
        fail!("plugin #{plugin["id"]} inputs must be a mapping") unless plugin["inputs"].is_a?(Hash)
        fail!("plugin #{plugin["id"]} output must be a mapping") unless plugin["output"].is_a?(Hash) && plugin["output"]["kind"].to_s.match?(/\A[a-z0-9][a-z0-9_-]*\z/)
        validate_execution_policy!(plugin, execution_kind: present?(entrypoint) ? "entrypoint" : "builtin", required: policy_required)
      end

      {"plugins" => ids}
    rescue Psych::Exception => error
      fail!("plugin manifest YAML is invalid: #{error.message}")
    end

    def self.present?(value)
      value.is_a?(Array) || value.is_a?(Hash) ? !value.empty? : !value.to_s.strip.empty?
    end
    private_class_method :present?

    def self.validate_execution_policy!(plugin, execution_kind:, required:)
      policy = plugin["execution_policy"]
      fail!("plugin #{plugin["id"]} is missing execution_policy") if required && !policy.is_a?(Hash)
      return unless policy

      fail!("plugin #{plugin["id"]} execution_policy must be a mapping") unless policy.is_a?(Hash)
      %w[trust timeout_seconds isolation unsupported_guarantees].each do |field|
        fail!("plugin #{plugin["id"]} execution_policy is missing #{field}") unless present?(policy[field])
      end
      expected_trust = TRUST_BY_EXECUTION_KIND.fetch(execution_kind)
      fail!("plugin #{plugin["id"]} execution_policy trust must be #{expected_trust}") unless policy["trust"] == expected_trust
      timeout = policy["timeout_seconds"]
      fail!("plugin #{plugin["id"]} execution_policy timeout_seconds must be an integer from 1 to #{MAX_TIMEOUT_SECONDS}") unless timeout.is_a?(Integer) && timeout.between?(1, MAX_TIMEOUT_SECONDS)
      fail!("plugin #{plugin["id"]} execution_policy isolation must be none; Dora does not provide a sandbox") unless policy["isolation"] == "none"
      guarantees = policy["unsupported_guarantees"]
      fail!("plugin #{plugin["id"]} execution_policy unsupported_guarantees must be a list") unless guarantees.is_a?(Array) && guarantees.all? { |value| value.is_a?(String) }
      missing = REQUIRED_UNSUPPORTED_GUARANTEES - guarantees
      fail!("plugin #{plugin["id"]} execution_policy must declare unsupported guarantees: #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :validate_execution_policy!

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
