#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

LIMITS = %w[filesystem_isolation network_isolation credential_isolation resource_sandbox].freeze

def manifest(plugin)
  {"kind" => "dora_plugin_manifest", "version" => 2, "plugins" => [plugin]}
end

def trusted_plugin(entrypoint:, timeout_seconds:, isolation: "none")
  {
    "id" => "trusted-check", "entrypoint" => entrypoint,
    "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "fixture"},
    "output" => {"kind" => "static-analysis-report", "path" => "reports/check.json"},
    "execution_policy" => {"trust" => "project_trusted", "timeout_seconds" => timeout_seconds, "isolation" => isolation, "unsupported_guarantees" => LIMITS}
  }
end

def run!(root, plugin)
  path = File.join(root, "plugins.yaml")
  File.write(path, YAML.dump(manifest(plugin)))
  Dora::PluginRunner.run!(path, plugin_id: "trusted-check", project_root: root)
end

Dir.mktmpdir("dora-plugin-policy-runner") do |root|
  FileUtils.mkdir_p(File.join(root, "src"))
  FileUtils.mkdir_p(File.join(root, "plugins"))

  File.write(File.join(root, "plugins", "quick.rb"), "puts 'checked'\n")
  result = run!(root, trusted_plugin(entrypoint: "plugins/quick.rb", timeout_seconds: 2))
  policy = result.fetch("policy")
  abort "runner lost declared timeout policy" unless policy.fetch("timeout_seconds") == 2 && policy.fetch("status") == "declared_and_enforced"
  abort "plugin report did not record no-sandbox boundary" unless File.read(File.join(root, "reports/check.json")).include?("does not sandbox")

  File.write(File.join(root, "plugins", "slow.rb"), "sleep 2\n")
  begin
    run!(root, trusted_plugin(entrypoint: "plugins/slow.rb", timeout_seconds: 1))
    abort "runner accepted a timed-out trusted plugin"
  rescue ArgumentError => error
    abort "wrong timeout failure: #{error.message}" unless error.message.include?("timed out after 1 seconds")
  end

  begin
    run!(root, trusted_plugin(entrypoint: "plugins/quick.rb", timeout_seconds: 2, isolation: "sandbox"))
    abort "runner accepted unsupported sandbox policy"
  rescue ArgumentError => error
    abort "wrong unsupported-policy failure: #{error.message}" unless error.message.include?("does not provide a sandbox")
  end
end

puts "Dora plugin policy runner test passed (declared timeout is enforced and unsupported sandbox claims fail closed)."
