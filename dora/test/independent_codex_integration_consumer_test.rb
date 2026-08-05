#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
FIXTURE = YAML.load_file(File.join(ROOT, "test/fixtures/codex-integration-consumers.yaml"))

Dir.mktmpdir("dora-independent-codex-integration") do |sandbox|
  installed_dora = File.join(sandbox, "dora")
  FileUtils.cp_r(ROOT, installed_dora)
  require File.join(installed_dora, "lib/dora/codex_integration")

  FIXTURE.fetch("consumers").each do |consumer|
    project = File.join(sandbox, consumer.fetch("id"))
    FileUtils.mkdir_p(project)
    agents = File.join(project, "AGENTS.md")
    File.write(agents, consumer.fetch("agents")); before = File.read(agents)
    result = Dora::CodexIntegration.install!(project_root: project, template_root: File.join(installed_dora, "templates/codex-integration"))
    guide = File.read(File.join(project, ".dora/codex-integration/README.md"))
    other = FIXTURE.fetch("consumers").find { |candidate| candidate.fetch("id") != consumer.fetch("id") }
    abort "consumer integration changed user-owned instructions" unless File.read(agents) == before && result.fetch("created").length == 2
    abort "consumer integration lacks bounded Dora guidance" unless guide.include?("does not replace `AGENTS.md`") && guide.include?("agent-session")
    abort "consumer integration leaked other user instructions" if guide.include?(other.fetch("agents")) || File.read(agents).include?(other.fetch("agents"))
    content = Dir[File.join(project, "**/*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
    abort "consumer integration refers to MuffinMan" if content.downcase.include?("muffinman")
  end
end

puts "Dora independent Codex integration consumer test passed (two projects keep distinct user guidance and receive bounded Dora navigation)."
