#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

def approval(operation, scope)
  {"kind" => "dora_approval_record", "version" => 1, "id" => "#{operation}-fixture", "actor" => "reviewer", "operation" => operation, "scope" => scope, "expires_at" => "2030-01-01T00:00:00Z", "evidence" => "reviewed fixture", "rollback" => "recorded backup"}
end

Dir.mktmpdir("dora-upgrade-cli") do |root|
  consumer = File.join(root, "consumer"); FileUtils.mkdir_p(File.join(consumer, "dora")); FileUtils.mkdir_p(File.join(consumer, ".dora"))
  File.write(File.join(consumer, "dora", "marker"), "old\n")
  File.write(File.join(consumer, ".dora/bootstrap-source.yaml"), YAML.dump({"kind" => "dora_bootstrap_record", "version" => 1, "source" => {"ref" => "a" * 40}, "package_path" => "dora"}))
  source = File.join(root, "source"); FileUtils.mkdir_p(File.join(source, "bin")); FileUtils.mkdir_p(File.join(source, "lib/dora")); File.write(File.join(source, "bin/dora"), "new package\n")
  descriptor = File.join(root, "source.yaml"); File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => source, "ref" => "b" * 40, "checksum" => Dora::BootstrapSource.send(:checksum_for, source)}}))
  apply_path = File.join(root, "apply.yaml"); File.write(apply_path, YAML.dump(approval("upgrade_apply", consumer)))
  output, status = Open3.capture2e(CLI, "upgrade-apply", consumer, "--source", descriptor, "--approval", apply_path)
  applied = YAML.safe_load(output)
  abort "CLI upgrade apply failed: #{output}" unless status.success? && applied.fetch("applied") && File.read(File.join(consumer, "dora/bin/dora")) == "new package\n"
  backup = applied.fetch("backup")
  abort "CLI upgrade backup did not retain old package" unless File.read(File.join(consumer, backup, "marker")) == "old\n"

  rejected_path = File.join(root, "rejected.yaml"); File.write(rejected_path, YAML.dump(approval("upgrade_apply", File.join(root, "other"))))
  _output, rejected = Open3.capture2e(CLI, "upgrade-apply", consumer, "--source", descriptor, "--approval", rejected_path)
  abort "CLI upgrade accepted a wrong approval scope" if rejected.success?

  rollback_path = File.join(root, "rollback.yaml"); File.write(rollback_path, YAML.dump(approval("upgrade_rollback", consumer)))
  output, status = Open3.capture2e(CLI, "upgrade-rollback", consumer, "--backup", backup, "--approval", rollback_path)
  rolled_back = YAML.safe_load(output)
  abort "CLI rollback failed: #{output}" unless status.success? && rolled_back.fetch("rolled_back") && File.read(File.join(consumer, "dora/marker")) == "old\n"
end

puts "Dora project upgrade apply command test passed (actual CLI apply rejects bad scope, records backup, and explicitly rolls back)."
