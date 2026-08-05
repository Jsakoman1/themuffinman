#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require_relative "../lib/dora/starter_pack"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-buildable-starter") do |root|
  FileUtils.mkdir_p(File.join(root, ".dora"))
  Dora::StarterPack.apply!(File.join(ROOT, "starters/spring-vue-buildable.yaml"), project_root: root)
  %w[backend/pom.xml backend/src/main/java/example/starter/StarterApplication.java frontend/package.json frontend/src/main.js].each { |relative| abort "missing buildable starter file: #{relative}" unless File.file?(File.join(root, relative)) }
  commands = File.read(File.join(root, ".dora/project-commands.yaml"))
  abort "starter commands are placeholders" if commands.include?("'true'") || commands.include?("\"true\"")
  text = Dir[File.join(root, "**", "*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "starter generated a product domain" if text.match?(/@Entity|@RestController|password|booking/i)

  [
    %w[mvn -q -f backend/pom.xml test],
    %w[npm --prefix frontend install --ignore-scripts],
    %w[npm --prefix frontend run test],
    %w[npm --prefix frontend run build]
  ].each do |command|
    output, status = Open3.capture2e(*command, chdir: root)
    abort "buildable starter command failed: #{command.join(' ')}\n#{output}" unless status.success?
  end
end
puts "Dora buildable Spring/Vue starter test passed (domain-free real build commands)."
