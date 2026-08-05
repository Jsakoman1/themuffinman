#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-zero-knowledge") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  descriptor = File.join(sandbox, "source.yaml")
  File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => source, "ref" => "d" * 40}}))
  project = File.join(sandbox, "new-project")
  output, status = Open3.capture2e(CLI, "bootstrap", project, "--project", "new-project", "--source", descriptor, "--starter", "blank", "--ci", "github-actions", chdir: ROOT)
  abort "zero-knowledge bootstrap failed: #{output}" unless status.success?
  abort "CI workflow was not generated" unless File.file?(File.join(project, ".github/workflows/dora-control.yml"))
  _output, status = Open3.capture2e(File.join(project, "bin/dora"), "doctor", ".dora/project.yaml", chdir: project)
  abort "bootstrapped project is not healthy" unless status.success?
  FileUtils.mkdir_p(File.join(project, "plugins"))
  File.write(File.join(project, "plugins", "fixture.rb"), "puts 'fixture plugin ran'\n")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "fixture", "entrypoint" => "plugins/fixture.rb", "source_roots" => [{"id" => "project", "path" => "src"}], "inputs" => {"fixture" => true}, "output" => {"kind" => "fixture-report", "path" => "docs/audit-output/fixture.json"}}]}
  File.write(File.join(project, ".dora/plugins.yaml"), YAML.dump(manifest))
  output, status = Open3.capture2e(File.join(project, "bin/dora"), "plugin-run", ".dora/plugins.yaml", "fixture", chdir: project)
  report_path = File.join(project, "docs/audit-output/fixture.json")
  abort "declared fixture plugin did not run: #{output}" unless status.success? && File.file?(report_path) && JSON.parse(File.read(report_path)).fetch("findings").first.fetch("output").include?("fixture plugin ran")
end

puts "Dora zero-knowledge bootstrap test passed (local source, starter, CI, doctor, and plugin)."
