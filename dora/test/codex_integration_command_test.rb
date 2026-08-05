#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "tmpdir"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
Dir.mktmpdir("dora-codex-integration-command") do |root|
  instructions = "User-owned instructions remain authoritative.\n"
  File.write(File.join(root, "AGENTS.md"), instructions)
  output, status = Open3.capture2e(CLI, "codex-integrate", root, "--format", "json")
  abort output unless status.success?
  envelope = JSON.parse(output)
  abort "integration command did not declare local mutation" unless envelope.fetch("side_effect") == "local_mutation"
  abort "integration command overwrote user instructions" unless File.read(File.join(root, "AGENTS.md")) == instructions
  abort "integration command omitted Dora guidance" unless File.file?(File.join(root, ".dora/codex-integration/README.md"))
  File.write(File.join(root, ".dora/codex-integration/README.md"), "changed guidance\n")
  _output, rejected = Open3.capture2e(CLI, "codex-integrate", root)
  abort "integration command overwrote changed Dora guidance" if rejected.success?
end
puts "Dora Codex integration command test passed (one explicit command preserves user instructions and changed guidance)."
