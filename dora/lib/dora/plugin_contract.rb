# frozen_string_literal: true

require "yaml"

module Dora
  class PluginContract
    VERSION_WITH_EXECUTION_POLICY = 2
    VERSION_WITH_CAPABILITY_CONTRACT = 3
    TRUST_BY_EXECUTION_KIND = {"entrypoint" => "project_trusted", "builtin" => "dora_builtin"}.freeze
    REQUIRED_UNSUPPORTED_GUARANTEES = %w[filesystem_isolation network_isolation credential_isolation resource_sandbox].freeze
    MAX_TIMEOUT_SECONDS = 300

    def self.validate!(manifest_path)
      manifest = YAML.load_file(manifest_path)
      fail!("plugin manifest kind is invalid") unless manifest.is_a?(Hash) && manifest["kind"] == "dora_plugin_manifest" && [1, VERSION_WITH_EXECUTION_POLICY, VERSION_WITH_CAPABILITY_CONTRACT].include?(manifest["version"].to_i)
      policy_required = manifest["version"].to_i >= VERSION_WITH_EXECUTION_POLICY
      capability_contract_required = manifest["version"].to_i >= VERSION_WITH_CAPABILITY_CONTRACT
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
        validate_capability_contract!(plugin, required: capability_contract_required)
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

    def self.validate_capability_contract!(plugin, required:)
      contract = plugin["capability_contract"]
      fail!("plugin #{plugin["id"]} is missing capability_contract") if required && !contract.is_a?(Hash)
      return unless contract

      fail!("plugin #{plugin["id"]} capability_contract must be a mapping") unless contract.is_a?(Hash)
      %w[capability_id api_version cache_identity output_schema_version deprecation].each do |field|
        fail!("plugin #{plugin["id"]} capability_contract is missing #{field}") unless present?(contract[field]) || field == "output_schema_version" && contract[field].is_a?(Integer)
      end
      fail!("plugin #{plugin["id"]} capability_contract capability_id is invalid") unless contract["capability_id"].is_a?(String) && contract["capability_id"].match?(/\A[a-z][a-z0-9-]*\z/)
      fail!("plugin #{plugin["id"]} capability_contract api_version is invalid") unless contract["api_version"].is_a?(String) && contract["api_version"].match?(/\A[1-9]\d*\.\d+\.\d+\z/)
      fail!("plugin #{plugin["id"]} capability_contract cache_identity is invalid") unless contract["cache_identity"].is_a?(String) && contract["cache_identity"].match?(/\A[a-z][a-z0-9_-]*\z/)
      fail!("plugin #{plugin["id"]} capability_contract output_schema_version is invalid") unless contract["output_schema_version"].is_a?(Integer) && contract["output_schema_version"].positive?
      fail!("plugin #{plugin["id"]} capability_contract deprecation is invalid") unless %w[supported deprecated].include?(contract["deprecation"])
    end
    private_class_method :validate_capability_contract!

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
