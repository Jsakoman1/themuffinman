#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
RELEASE_DIRECTORIES = %w[.dora bin bridge/lib bridge/templates lib packs starters templates tools].freeze
RELEASE_FILES = %w[AGENTS.md README.md bridge/bin/dora-bridge-mcp compiled-feature-type-map.yaml stack-catalog.yaml].freeze
RELEASE_DOCS = %w[docs/product-brief.yaml docs/domain-library.yaml docs/project-memory.yaml docs/decision-log.yaml].freeze
REPOSITORY_TEST_FILES = %w[
  test/independent_release_tree_test.rb
  test/independent_self_contained_consumer_test.rb
  test/independent_project_read_model_consumer_test.rb
  test/portable_fixture_test.rb
  test/project_doctor_test.rb
  test/project_read_model_test.rb
].freeze
REPOSITORY_TEST_FIXTURES = %w[
  test/fixtures/project-read-model-projects.yaml
  test/fixtures/self-contained-project-answers.yaml
].freeze
FORBIDDEN_PATHS = [
  %r{\A#{%w[apps themuffinman].join("/")}(?:/|\z)},
  %r{(?:\A|/)#{%w[node modules].join("_")}(?:/|\z)},
  %r{(?:\A|/)\.env(?:/|\z)},
  %r{\A\.dora/plugins\.yaml\z},
  %r{\Abridge/test(?:/|\z)},
  %r{\Atest/dora_consumer_integration(?:/|\z)}
].freeze
FORBIDDEN_CONTENT = [
  %w[apps themuffinman].join("/"),
  "../.dora/plugins.yaml",
  ["", "Users", ""].join("/"),
  ["", "Desktop", ""].join("/"),
  %w[Dooms Day Storage].join,
  %w[node modules].join("_")
].freeze

def copy_path(source, destination)
  FileUtils.mkdir_p(File.dirname(destination))
  FileUtils.cp_r(source, destination)
end

def run!(root, *command, environment: {})
  output, status = Open3.capture2e(environment, *command, chdir: root)
  abort "independent release tree command failed: #{command.join(' ')}\n#{output}" unless status.success?
  output
end

def release_files(root)
  Dir.glob(File.join(root, "**", "*"), File::FNM_DOTMATCH).select { |path| File.file?(path) }
end

def materialize_release_tree!(source, release)
  RELEASE_DIRECTORIES.each { |relative| copy_path(File.join(source, relative), File.join(release, relative)) }
  RELEASE_FILES.each { |relative| copy_path(File.join(source, relative), File.join(release, relative)) }
  Dir.children(File.join(source, "fixtures")).reject { |entry| entry == "muffinman-adapter.yaml" }.each do |entry|
    copy_path(File.join(source, "fixtures", entry), File.join(release, "fixtures", entry))
  end
  Dir[File.join(source, "*.schema.yaml")].each { |path| copy_path(path, File.join(release, File.basename(path))) }
  RELEASE_DOCS.each { |relative| copy_path(File.join(source, relative), File.join(release, relative)) }
  Dir[File.join(source, "docs/work/**/*.yaml")].each do |path|
    copy_path(path, File.join(release, path.delete_prefix("#{source}/")))
  end
  %w[docs/audit-output/.gitkeep docs/runtime-evidence/.gitkeep].each { |relative| copy_path(File.join(source, relative), File.join(release, relative)) }
end

def materialize_independent_repository!(source, target)
  abort "independent Dora repository target already exists: #{target}" if File.exist?(target)

  materialize_release_tree!(source, target)
  REPOSITORY_TEST_FILES.each { |relative| copy_path(File.join(source, relative), File.join(target, relative)) }
  REPOSITORY_TEST_FIXTURES.each { |relative| copy_path(File.join(source, relative), File.join(target, relative)) }
  copied_tests = Dir.glob(File.join(target, "test", "**", "*"), File::FNM_DOTMATCH).select { |path| File.file?(path) }.map { |path| path.delete_prefix("#{target}/") }.sort
  expected_tests = (REPOSITORY_TEST_FILES + REPOSITORY_TEST_FIXTURES).sort
  abort "independent Dora repository test surface is not curated" unless copied_tests == expected_tests
  run!(target, "git", "init", "--quiet")
  abort "independent Dora repository is not its own Git repository" unless File.directory?(File.join(target, ".git"))
end

def assert_release_boundary!(release)
  files = release_files(release)
  required = %w[.dora/project.yaml bin/dora bridge/bin/dora-bridge-mcp lib/dora/project_read_model.rb templates/init-manifest.yaml tools/typescript-vue-ast-index.mjs]
  missing = required.reject { |relative| File.file?(File.join(release, relative)) }
  abort "release tree omitted required Dora content: #{missing.join(', ')}" unless missing.empty?
  relative = files.map { |path| path.delete_prefix("#{release}/") }
  leaks = relative.select { |path| FORBIDDEN_PATHS.any? { |pattern| pattern.match?(path) } }
  abort "release tree includes a consumer or local-runtime path: #{leaks.sort.join(', ')}" unless leaks.empty?

  content_leaks = files.each_with_object([]) do |path, found|
    content = File.binread(path).force_encoding(Encoding::UTF_8).scrub
    found << path.delete_prefix("#{release}/") if FORBIDDEN_CONTENT.any? { |needle| content.include?(needle) }
  end
  abort "release tree contains a forbidden parent/local reference: #{content_leaks.sort.join(', ')}" unless content_leaks.empty?
end

def write_registry(release)
  path = File.join(release, "bridge-projects.yaml")
  File.write(path, YAML.dump({"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "dora", "name" => "Dora", "adapter_path" => ".dora/project.yaml"}]}))
  path
end

def assert_read_only_mcp!(release, registry)
  Open3.popen3("ruby", File.join(release, "bridge/bin/dora-bridge-mcp"), registry, chdir: release) do |stdin, stdout, stderr, wait_thread|
    stdin.puts(JSON.generate({"jsonrpc" => "2.0", "id" => 1, "method" => "initialize", "params" => {"protocolVersion" => "2025-06-18"}}))
    initialized = JSON.parse(stdout.gets)
    abort "isolated MCP initialization failed" unless initialized.dig("result", "capabilities", "tools")

    stdin.puts(JSON.generate({"jsonrpc" => "2.0", "id" => 2, "method" => "tools/call", "params" => {"name" => "get_project_summary", "arguments" => {"project" => "dora"}}}))
    summary = JSON.parse(stdout.gets).dig("result", "structuredContent")
    abort "isolated MCP summary did not use the Dora read model" unless summary.fetch("kind") == "dora_project_read_model"
    abort "isolated MCP summary is not healthy" unless summary.dig("health", "healthy") && summary.dig("integrity", "status") == "HEALTHY"
    abort "isolated MCP summary leaked its filesystem root" if summary.to_s.include?(release)

    stdin.close
    abort "isolated MCP exited unsuccessfully: #{stderr.read}" unless wait_thread.value.success?
  end
end

Dir.mktmpdir("dora-independent-release-tree") do |sandbox|
  release = File.join(sandbox, "dora-release")
  materialize_release_tree!(ROOT, release)
  assert_release_boundary!(release)
  run!(release, "git", "init", "--quiet")
  abort "isolated release tree is not its own Git repository" unless File.directory?(File.join(release, ".git"))

  traps = [File.join(sandbox, ".dora"), File.join(sandbox, "apps")]
  traps.each { |path| FileUtils.mkdir_p(path); FileUtils.chmod(0o000, path) }
  begin
    run!(release, "bin/dora", "doctor", ".dora/project.yaml")
    projection = run!(release, "ruby", "-Ilib", "-e", "require 'dora/project_read_model'; summary = Dora::ProjectReadModel.load!(adapter_path: '.dora/project.yaml').summary; abort 'not healthy' unless summary.dig('integrity', 'status') == 'HEALTHY'; puts summary.fetch('kind')")
    abort "isolated ProjectReadModel did not load" unless projection.include?("dora_project_read_model")
    assert_read_only_mcp!(release, write_registry(release))
  ensure
    traps.each { |path| FileUtils.chmod(0o700, path) if File.exist?(path) }
  end
end

if (target = ENV["DORA_MATERIALIZE_TARGET"])
  materialize_independent_repository!(ROOT, File.expand_path(target))
  run!(target, "ruby", "test/independent_release_tree_test.rb", environment: {"DORA_MATERIALIZE_TARGET" => nil})
end

puts "Dora independent release tree test passed (isolated Git tree, parent traps, Doctor, ProjectReadModel, and read-only MCP)."
