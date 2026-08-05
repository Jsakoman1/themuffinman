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

def write_manifest(root, plugins)
  path = File.join(root, "plugins.yaml")
  File.write(path, YAML.dump({"kind" => "dora_plugin_manifest", "version" => 2, "plugins" => plugins}))
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
end

puts "Dora plugin policy contract test passed (versioned trust, timeout, and no-sandbox limits are explicit)."
