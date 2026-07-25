#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"

root = File.expand_path("../..", __dir__)
frontend_root = File.join(root, "apps/themuffinman/frontend")
runtime_scripts = Dir[File.join(frontend_root, "scripts/*runtime.mjs")].sort
corpus = Dir[File.join(root, "docs/**/*"), File.join(root, "Makefile")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
package = JSON.parse(File.read(File.join(frontend_root, "package.json")))
failures = []

runtime_scripts.each do |path|
  relative = path.delete_prefix("#{root}/")
  basename = File.basename(path)
  failures << "unreferenced runtime script: #{relative}" unless corpus.include?(basename)
  _stdout, stderr, status = Open3.capture3("node", "--check", path, chdir: root)
  failures << "runtime syntax failed: #{relative}\n#{stderr}" unless status.success?
end

playwright_version = package.dig("devDependencies", "playwright")
failures << "frontend devDependencies does not declare Playwright" if playwright_version.to_s.empty?

abort "Runtime tool preflight failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Runtime tool preflight passed (#{runtime_scripts.length} Chromium/runtime scripts, Playwright #{playwright_version})."
