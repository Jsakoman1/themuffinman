#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/product_brief"
require_relative "../lib/dora/domain_library"
require_relative "../lib/dora/agent_project_profile"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-agent-knowledge-bootstrap") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "new-application", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  brief_path = File.join(root, "docs/product-brief.yaml")
  library_path = File.join(root, "docs/domain-library.yaml")
  profile_path = File.join(root, ".dora/agent-project-profile.yaml")
  abort "bootstrap did not create the Codex entrypoint" unless File.read(File.join(root, "AGENTS.md")).include?("agent-project-profile")
  abort "bootstrap product brief is invalid" unless Dora::ProductBrief.load!(brief_path).fetch("product").include?("Replace")
  abort "bootstrap domain library is invalid" unless Dora::DomainLibrary.load!(library_path).fetch("entities").any?
  abort "bootstrap agent profile is invalid" unless Dora::AgentProjectProfile.load!(profile_path).fetch("authority_limits").any?
end

puts "Dora agent knowledge bootstrap test passed (brief, domain library, agent profile, and Codex entrypoint)."
