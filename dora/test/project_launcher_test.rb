#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require_relative "../lib/dora/project_launcher"

ROOT = File.expand_path("..", __dir__)
TEMPLATE = File.join(ROOT, "templates/project-launcher")

Dir.mktmpdir("dora-project-launcher") do |sandbox|
  project_root = File.join(sandbox, "consumer")
  FileUtils.mkdir_p(project_root)
  FileUtils.cp_r(ROOT, File.join(project_root, "dora"))
  launcher = Dora::ProjectLauncher.write!(project_root, template_path: TEMPLATE)
  abort "launcher is not executable" unless File.executable?(File.join(project_root, launcher))
  output, status = Open3.capture2e(File.join(project_root, launcher), "help", chdir: project_root)
  abort "local launcher did not reach Dora package: #{output}" unless status.success? && output.include?("Dora commands:")
end

puts "Dora project launcher test passed (project-local invocation without PATH setup)."
