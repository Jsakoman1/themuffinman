#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_contract"

DORA_BIN = File.expand_path("../bin/dora", __dir__)

def manifest(id, root)
  {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => id, "entrypoint" => "dora/plugins/#{id}", "source_roots" => [{"id" => "source", "path" => root}], "inputs" => {"extensions" => ["rb"]}, "output" => {"kind" => "static-analysis-report"}}]}
end

Dir.mktmpdir("dora-plugin-contract") do |root|
  alpha = File.join(root, "alpha.yaml")
  beta = File.join(root, "beta.yaml")
  File.write(alpha, YAML.dump(manifest("alpha-check", "src")))
  File.write(beta, YAML.dump(manifest("beta-check", "client")))
  abort "alpha manifest did not validate" unless Dora::PluginContract.validate!(alpha) == {"plugins" => ["alpha-check"]}
  abort "beta manifest did not validate" unless Dora::PluginContract.validate!(beta) == {"plugins" => ["beta-check"]}
  stdout, stderr, status = Open3.capture3(DORA_BIN, "plugin-contract", alpha)
  abort "CLI plugin contract failed: #{stderr}" unless status.success? && stdout.include?("alpha-check")

  invalid = manifest("bad", "../outside")
  File.write(alpha, YAML.dump(invalid))
  begin
    Dora::PluginContract.validate!(alpha)
    abort "unsafe plugin root passed"
  rescue ArgumentError => error
    abort "wrong unsafe root failure" unless error.message.include?("invalid")
  end
end

puts "Dora plugin contract test passed (two isolated manifests and root safety without product dependencies)."
