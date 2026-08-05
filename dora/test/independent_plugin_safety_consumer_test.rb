#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/plugin-safety-consumers.yaml"))

def policy(timeout_seconds)
  {
    "trust" => "project_trusted", "timeout_seconds" => timeout_seconds, "isolation" => "none",
    "unsupported_guarantees" => FIXTURE.fetch("unsupported_guarantees")
  }
end

def manifest(consumer)
  {
    "kind" => "dora_plugin_manifest", "version" => 2,
    "plugins" => [{
      "id" => "trusted-analysis", "entrypoint" => consumer.fetch("entrypoint"),
      "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"consumer" => consumer.fetch("id")},
      "output" => {"kind" => "static-analysis-report", "path" => "reports/plugin.json"},
      "execution_policy" => policy(consumer.fetch("timeout_seconds"))
    }]
  }
end

Dir.mktmpdir("dora-independent-plugin-safety") do |sandbox|
  installed_dora = File.join(sandbox, "dora")
  FileUtils.cp_r(ROOT, installed_dora)

  FIXTURE.fetch("consumers").each do |consumer|
    project = File.join(sandbox, consumer.fetch("id"))
    FileUtils.mkdir_p(File.join(project, "src"))
    FileUtils.mkdir_p(File.join(project, "plugins"))
    File.write(File.join(project, consumer.fetch("entrypoint")), consumer.fetch("program") + "\n")
    manifest_path = File.join(project, "plugins.yaml")
    File.write(manifest_path, YAML.dump(manifest(consumer)))

    output, status = Open3.capture2e(File.join(installed_dora, "bin/dora"), "plugin-run", "plugins.yaml", "trusted-analysis", chdir: project)
    if consumer.fetch("expected") == "success"
      abort "independent trusted plugin failed: #{output}" unless status.success?
      report = JSON.parse(File.read(File.join(project, "reports/plugin.json")))
      boundary = report.fetch("execution_boundary")
      abort "consumer lost declared trust boundary" unless boundary.fetch("trust") == "project_trusted" && boundary.fetch("status") == "declared_and_enforced"
      abort "consumer report implies a sandbox" unless boundary.fetch("enforcement").include?("does not sandbox")
    else
      abort "independent timeout plugin unexpectedly passed" if status.success?
      abort "timeout evidence is missing" unless output.include?("timed out after #{consumer.fetch("timeout_seconds")} seconds")
      abort "timed-out plugin wrote a success report" if File.exist?(File.join(project, "reports/plugin.json"))
    end

    content = Dir[File.join(project, "**/*")].select { |path| File.file?(path) }.map { |path| File.binread(path).force_encoding(Encoding::UTF_8).scrub }.join("\n")
    abort "independent consumer refers to MuffinMan" if content.downcase.include?("muffinman")
  end
end

puts "Dora independent plugin safety consumer test passed (two standalone consumers prove trusted timeout enforcement and no sandbox claim)."
