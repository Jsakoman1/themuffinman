#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/codex_integration"

ROOT = File.expand_path("..", __dir__)
TEMPLATES = File.join(ROOT, "templates/codex-integration")

Dir.mktmpdir("dora-codex-integration") do |root|
  agents = File.join(root, "AGENTS.md")
  File.write(agents, "# User-owned instructions\nKeep this exact text.\n")
  before = File.read(agents)
  result = Dora::CodexIntegration.install!(project_root: root, template_root: TEMPLATES)
  abort "Codex integration did not create all guidance" unless result.fetch("created").sort == %w[.dora/codex-integration/README.md .dora/codex-integration/session-discovery.md]
  abort "Codex integration overwrote user guidance" unless File.read(agents) == before && result.fetch("user_owned_instructions") == ["AGENTS.md"]
  guide = File.read(File.join(root, ".dora/codex-integration/README.md"))
  abort "Codex integration lacks declared session discovery" unless guide.include?("agent-session") && result.fetch("authority_boundary").include?("does not overwrite")

  second = Dora::CodexIntegration.install!(project_root: root, template_root: TEMPLATES)
  abort "Codex integration did not preserve identical Dora guidance" unless second.fetch("created").empty? && second.fetch("preserved").length == 2

  File.write(File.join(root, ".dora/codex-integration/README.md"), "user customization\n")
  begin
    Dora::CodexIntegration.install!(project_root: root, template_root: TEMPLATES)
    abort "Codex integration overwrote changed Dora guidance"
  rescue ArgumentError => error
    abort "wrong no-overwrite rejection: #{error.message}" unless error.message.include?("refuses to overwrite")
  end
end

puts "Dora Codex integration test passed (opt-in navigation guidance preserves user-owned instructions)."
