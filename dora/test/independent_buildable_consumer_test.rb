#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)

def run!(root, *command)
  output, status = Open3.capture2e(*command, chdir: root)
  abort "independent consumer command failed: #{command.join(' ')}\n#{output}" unless status.success?
  output
end

def run_shell!(root, command)
  run!(root, "/bin/zsh", "-lc", command)
end

Dir.mktmpdir("dora-independent-buildable-consumer") do |sandbox|
  reviewed_source = File.join(sandbox, "released-dora")
  FileUtils.cp_r(ROOT, reviewed_source)
  descriptor = File.join(sandbox, "bootstrap-source.yaml")
  File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => reviewed_source, "ref" => "7" * 40}}))
  project = File.join(sandbox, "new-app")

  run!(sandbox, File.join(reviewed_source, "bin/dora"), "bootstrap", project, "--project", "new-app", "--source", descriptor, "--starter", "spring-vue-buildable", "--ci", "github-actions")
  commands = YAML.load_file(File.join(project, ".dora/project-commands.yaml")).fetch("commands")
  %w[setup test build].each { |id| run_shell!(project, commands.fetch(id)) }
  doctor = run!(project, "./bin/dora", "doctor", ".dora/project.yaml")
  abort "independent consumer is not healthy" unless doctor.include?("PASSED")

  FileUtils.mkdir_p(File.join(project, "plugins"))
  File.write(File.join(project, "plugins", "portable.rb"), "puts 'portable plugin ran'\n")
  manifest = {
    "kind" => "dora_plugin_manifest",
    "version" => 1,
    "plugins" => [{
      "id" => "portable",
      "entrypoint" => "plugins/portable.rb",
      "source_roots" => [{"id" => "frontend", "path" => "frontend/src"}],
      "inputs" => {"scope" => "technical"},
      "output" => {"kind" => "portable-report", "path" => "docs/audit-output/portable.json"}
    }]
  }
  File.write(File.join(project, ".dora/plugins.yaml"), YAML.dump(manifest))
  run!(project, "./bin/dora", "plugin-run", ".dora/plugins.yaml", "portable")
  report_path = File.join(project, "docs/audit-output/portable.json")
  abort "portable plugin report is missing" unless File.file?(report_path)
  report = JSON.parse(File.read(report_path))
  abort "portable plugin did not execute" unless report.fetch("findings").first.fetch("output").include?("portable plugin ran")
  abort "CI workflow is missing" unless File.file?(File.join(project, ".github/workflows/dora-control.yml"))

  content = Dir[File.join(project, "{.dora,.github,backend,bin,docs,frontend,plugins}/**/*")].select { |path| File.file?(path) }.map { |path| File.binread(path).force_encoding(Encoding::UTF_8).scrub }.join("\n")
  abort "independent consumer refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora independent buildable consumer test passed (released local source, buildable starter, CI, doctor, and portable plugin)."
