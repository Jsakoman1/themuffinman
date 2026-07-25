#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"

ROOT = File.expand_path("..", __dir__)

def relative(path)
  path.delete_prefix("#{ROOT}/")
end

removed = []

Dir.glob(File.join(ROOT, "docs/audit-output", "**", "*"), File::FNM_DOTMATCH).each do |path|
  next unless File.file?(path)

  FileUtils.rm_f(path)
  removed << relative(path)
end

frontend_dist = File.join(ROOT, "apps/themuffinman/frontend/dist")
if Dir.exist?(frontend_dist)
  FileUtils.rm_rf(frontend_dist)
  removed << "apps/themuffinman/frontend/dist/"
end

%w[docs scripts apps].each do |relative_dir|
  patterns = ["*.tmp.*", "*.yaml-e", "*.bak", "*.orig", "*.rej", "*~"]
  patterns.each do |pattern|
    Dir.glob(File.join(ROOT, relative_dir, "**", pattern)).each do |path|
      next unless File.file?(path)

      FileUtils.rm_f(path)
      removed << relative(path)
    end
  end
end

puts "Removed #{removed.length} generated/transient artifacts."
removed.each { |path| puts "- #{path}" }
