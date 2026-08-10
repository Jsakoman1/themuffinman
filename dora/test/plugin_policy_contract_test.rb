#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_contract"

POLICY_LIMITS = %w[filesystem_isolation network_isolation credential_isolation resource_sandbox].freeze

def plugin(id:, kind:, policy:)
  execution = kind == :builtin ? {"builtin" => "architecture-integrity"} : {"entrypoint" => "plugins/check.rb"}
  execution.merge(
    "id" => id,
    "source_roots" => [{"id" => "source", "path" => "src"}],
    "inputs" => {"mode" => "fixture"},
    "output" => {"kind" => "static-analysis-report"},
    "execution_policy" => policy
  )
end

def write_manifest(root, plugins, version: 2)
  path = File.join(root, "plugins.yaml")
  File.write(path, YAML.dump({"kind" => "dora_plugin_manifest", "version" => version, "plugins" => plugins}))
  path
end

def assert_rejected!(path, expected)
  Dora::PluginContract.validate!(path)
  abort "plugin policy was unexpectedly accepted"
rescue ArgumentError => error
  abort "wrong plugin policy rejection: #{error.message}" unless error.message.include?(expected)
end

Dir.mktmpdir("dora-plugin-policy-contract") do |root|
  builtin_policy = {"trust" => "dora_builtin", "timeout_seconds" => 30, "isolation" => "none", "unsupported_guarantees" => POLICY_LIMITS}
  trusted_policy = builtin_policy.merge("trust" => "project_trusted")
  valid = write_manifest(root, [plugin(id: "builtin", kind: :builtin, policy: builtin_policy), plugin(id: "trusted", kind: :entrypoint, policy: trusted_policy)])
  abort "declared policy manifest did not validate" unless Dora::PluginContract.validate!(valid).fetch("plugins") == %w[builtin trusted]

  missing_policy = write_manifest(root, [plugin(id: "missing", kind: :builtin, policy: nil)])
  assert_rejected!(missing_policy, "missing execution_policy")

  false_sandbox = write_manifest(root, [plugin(id: "sandbox", kind: :entrypoint, policy: trusted_policy.merge("isolation" => "sandbox"))])
  assert_rejected!(false_sandbox, "does not provide a sandbox")

  incomplete_limits = write_manifest(root, [plugin(id: "incomplete", kind: :entrypoint, policy: trusted_policy.merge("unsupported_guarantees" => ["network_isolation"]))])
  assert_rejected!(incomplete_limits, "must declare unsupported guarantees")

  wrong_trust = write_manifest(root, [plugin(id: "wrong-trust", kind: :builtin, policy: trusted_policy)])
  assert_rejected!(wrong_trust, "trust must be dora_builtin")

  v3_contract = {"capability_id" => "http-contract-link", "api_version" => "1.0.0", "cache_identity" => "http-contract-v1", "output_schema_version" => 1, "deprecation" => "supported"}
  v3 = write_manifest(root, [plugin(id: "v3", kind: :builtin, policy: builtin_policy).merge("capability_contract" => v3_contract)], version: 3)
  abort "version three manifest did not validate" unless Dora::PluginContract.validate!(v3).fetch("plugins") == ["v3"]

  missing_contract = write_manifest(root, [plugin(id: "missing-contract", kind: :builtin, policy: builtin_policy)], version: 3)
  assert_rejected!(missing_contract, "missing capability_contract")

  invalid_version = write_manifest(root, [plugin(id: "bad-contract", kind: :builtin, policy: builtin_policy).merge("capability_contract" => v3_contract.merge("api_version" => "one"))], version: 3)
  assert_rejected!(invalid_version, "api_version is invalid")

  deprecated = write_manifest(root, [plugin(id: "deprecated-contract", kind: :builtin, policy: builtin_policy).merge("capability_contract" => v3_contract.merge("deprecation" => "deprecated"))], version: 3)
  abort "declared deprecated contract did not validate" unless Dora::PluginContract.validate!(deprecated).fetch("plugins") == ["deprecated-contract"]
end

domain_library = YAML.load_file(File.expand_path("../docs/domain-library.yaml", __dir__))
abort "domain library omits plugin v3 contract" unless domain_library.fetch("vocabulary").any? { |item| item.fetch("id") == "plugin-capability-contract" && item.fetch("description").include?("Static manifest v3") }
abort "domain library duplicates plugin canonical state" unless domain_library.fetch("invariants").any? { |item| item.fetch("id") == "plugin-contract-canonical-separation" && item.fetch("description").include?("AnalysisCache alone") }

puts "Dora plugin policy contract test passed (legacy support plus versioned trust, capability API, cache identity, and no-sandbox limits)."
