#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_initializer"

MANIFEST_PATH = File.expand_path("../templates/init-manifest.yaml", __dir__)

Dir.mktmpdir("dora-init") do |sandbox|
  root = File.join(sandbox, "sample")
  files = Dora::ProjectInitializer.initialize!(root, project_id: "sample-app", manifest_path: MANIFEST_PATH)
  adapter = YAML.load_file(File.join(root, ".dora/project.yaml"))
  control = YAML.load_file(File.join(root, ".dora/project-control.yaml"))
  abort "initializer did not scaffold capability inventory" unless control.fetch("controls").key?("capability_inventory") && File.file?(File.join(root, ".dora/controls/capability-inventory.yaml"))
  abort "init did not create its manifest files" unless files.all? { |relative| File.file?(File.join(root, relative)) }
  abort "init adapter is not project-owned" unless adapter.dig("project", "id") == "sample-app" && adapter.dig("project", "root") == ".."
  abort "init adapter requires a global Dora command" unless adapter.fetch("commands").values.all? { |command| command.start_with?("./bin/dora ") }
  abort "init did not declare the local launcher" unless files.include?("bin/dora") && File.executable?(File.join(root, "bin/dora"))
  abort "init control bundle is incomplete" unless control.fetch("controls").length == 9
  abort "init created product source" unless Dir[File.join(root, "apps/**/*")].empty?
end

puts "Dora project initializer test passed (control-only project scaffold)."
