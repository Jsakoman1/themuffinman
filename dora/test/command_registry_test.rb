#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require_relative "../lib/dora/project_initializer"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
MANIFEST = File.join(ROOT, "templates/init-manifest.yaml")

def capture!(*command)
  output, status = Open3.capture2e(*command, chdir: ROOT)
  abort "command failed: #{command.join(" ")}" unless status.success?
  output
end

static_help = capture!(CLI, "help")
abort "static help is missing init" unless static_help.include?("dora init")
abort "static help exposed a project extension" if static_help.include?("Declared project extensions:")

Dir.mktmpdir("dora-command-registry") do |sandbox|
  project_root = File.join(sandbox, "registry-project")
  Dora::ProjectInitializer.initialize!(project_root, project_id: "registry-project", manifest_path: MANIFEST)
  project_help = capture!(CLI, "help", File.join(project_root, ".dora/project.yaml"))
  abort "project help is missing declared extension" unless project_help.include?("project-documentation (documentation)")
  abort "project help is missing generic doctor command" unless project_help.include?("dora doctor")
  abort "project help refers to MuffinMan" if project_help.downcase.include?("muffinman")
end

puts "Dora command registry test passed (generic commands and declared extensions)."
