#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "digest"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/upgrade-apply-consumers.yaml"))

def approval(operation, scope, id)
  {"kind" => "dora_approval_record", "version" => 1, "id" => id, "actor" => "consumer-reviewer", "operation" => operation, "scope" => scope, "expires_at" => "2030-01-01T00:00:00Z", "evidence" => "consumer fixture", "rollback" => "recorded backup"}
end

Dir.mktmpdir("dora-independent-upgrade") do |root|
  FIXTURE.fetch("consumers").each do |consumer|
    project = File.join(root, consumer.fetch("id")); FileUtils.mkdir_p(File.join(project, "dora")); FileUtils.mkdir_p(File.join(project, ".dora"))
    File.write(File.join(project, "dora", "marker"), consumer.fetch("old_marker"))
    File.write(File.join(project, ".dora/bootstrap-source.yaml"), YAML.dump({"kind" => "dora_bootstrap_record", "version" => 1, "source" => {"ref" => "a" * 40}, "package_path" => "dora"}))
    source = File.join(project, "reviewed-source"); FileUtils.mkdir_p(File.join(source, "bin")); FileUtils.mkdir_p(File.join(source, "lib/dora")); File.write(File.join(source, "bin/dora"), consumer.fetch("new_marker"))
    descriptor = File.join(project, "source.yaml"); checksum = Digest::SHA256.hexdigest(Dir[File.join(source, "**/*")].select { |path| File.file?(path) }.sort.map { |path| "#{path.delete_prefix("#{source}/")}\0#{Digest::SHA256.file(path).hexdigest}" }.join("\n")); File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => source, "ref" => "b" * 40, "checksum" => checksum}}))
    apply_path = File.join(project, "apply.yaml"); File.write(apply_path, YAML.dump(approval("upgrade_apply", project, "apply-#{consumer.fetch("id")}")))
    output, status = Open3.capture2e(CLI, "upgrade-apply", project, "--source", descriptor, "--approval", apply_path)
    applied = YAML.safe_load(output)
    abort "independent upgrade failed: #{output}" unless status.success? && applied.fetch("applied") && File.read(File.join(project, "dora/bin/dora")) == consumer.fetch("new_marker")
    rollback_path = File.join(project, "rollback.yaml"); File.write(rollback_path, YAML.dump(approval("upgrade_rollback", project, "rollback-#{consumer.fetch("id")}")))
    output, status = Open3.capture2e(CLI, "upgrade-rollback", project, "--backup", applied.fetch("backup"), "--approval", rollback_path)
    abort "independent rollback failed: #{output}" unless status.success? && YAML.safe_load(output).fetch("rolled_back") && File.read(File.join(project, "dora/marker")) == consumer.fetch("old_marker")
  end
end

puts "Dora independent upgrade apply consumer test passed (two consumers execute scoped CLI apply, backup, and rollback without product data)."
