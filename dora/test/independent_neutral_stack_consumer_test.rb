#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/starter_pack"
require_relative "../lib/dora/plugin_runner"

ROOT = File.expand_path("..", __dir__)
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/neutral-stack-consumers.yaml"))
abort "neutral stack fixture is invalid" unless fixture["kind"] == "dora_neutral_stack_consumers"
policy = {"trust" => "project_trusted", "timeout_seconds" => 5, "isolation" => "none", "unsupported_guarantees" => Dora::PluginContract::REQUIRED_UNSUPPORTED_GUARANTEES}
Dir.mktmpdir("dora-neutral-stack-consumers") do |sandbox|
  fixture.fetch("consumers").each do |consumer|
    root = File.join(sandbox, consumer.fetch("id")); FileUtils.mkdir_p(File.join(root, ".dora"))
    result = Dora::StarterPack.apply!(File.join(ROOT, "starters/#{consumer.fetch("starter")}.yaml"), project_root: root)
    abort "starter mismatch" unless result.fetch("id") == consumer.fetch("starter")
    FileUtils.mkdir_p(File.join(root, "src")); FileUtils.mkdir_p(File.join(root, "plugins"))
    File.write(File.join(root, "src/input.txt"), consumer.fetch("id")); File.write(File.join(root, "plugins/check.rb"), "puts 'diagnostic'\n")
    manifest = {"kind" => "dora_plugin_manifest", "version" => 2, "plugins" => [{"id" => "check", "entrypoint" => "plugins/check.rb", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "test"}, "output" => {"kind" => "report", "path" => "docs/audit-output/check.json"}, "execution_policy" => policy}]}
    path = File.join(root, "plugins.yaml"); File.write(path, YAML.dump(manifest)); Dora::PluginRunner.run!(path, plugin_id: "check", project_root: root)
    content = File.read(File.join(root, "docs/audit-output/check.json"))
    abort "plugin claimed a sandbox" if content.include?("filesystem-sandboxed") && !content.include?("not filesystem-sandboxed")
    abort "consumer contains MuffinMan data" if content.downcase.include?("muffinman")
  end
end
puts "Dora independent neutral stack consumer test passed (two neutral starters and trusted plugins remain isolated from MuffinMan data and sandbox claims)."
