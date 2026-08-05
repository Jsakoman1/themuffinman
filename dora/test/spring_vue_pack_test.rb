#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-spring-vue-pack") do |sandbox|
  root = File.join(sandbox, "stack-project")
  output, status = Open3.capture2e(CLI, "init", root, "--project", "stack-project", "--stack", "spring-vue", chdir: ROOT)
  abort "stack init failed: #{output}" unless status.success?
  abort "backend root missing" unless Dir.exist?(File.join(root, "backend/src/main/java"))
  abort "frontend root missing" unless Dir.exist?(File.join(root, "frontend/src"))
  stack = YAML.load_file(File.join(root, ".dora/stack.yaml"))
  repository_map = YAML.load_file(File.join(root, ".dora/repository-map.yaml"))
  abort "validation commands missing" unless stack.fetch("validation_commands").keys.sort == %w[backend frontend]
  abort "plugin configuration missing" unless repository_map.fetch("source_roots").map { |source| source.fetch("plugins") }.flatten.sort == %w[java_spring typescript_vue]
  abort "stack created application source" unless Dir[File.join(root, "backend/**/*.java"), File.join(root, "frontend/**/*.{ts,vue}")].empty?
end

puts "Dora Spring/Vue pack test passed (declared roots, commands, and plugins only)."
