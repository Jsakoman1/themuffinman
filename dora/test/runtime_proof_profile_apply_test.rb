#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "tmpdir"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
Dir.mktmpdir("dora-profile-apply") do |root|
  output, status = Open3.capture2e(CLI, "runtime-profile-apply", "profile", "--dry-run", chdir: root)
  abort output unless status.success? && !File.exist?(File.join(root, "profile"))
  output, status = Open3.capture2e(CLI, "runtime-profile-apply", "profile", "--apply", chdir: root)
  abort output unless status.success? && File.file?(File.join(root, "profile/package.json"))
  File.write(File.join(root, "keep.txt"), "keep")
  _output, unsafe = Open3.capture2e(CLI, "runtime-profile-apply", "../escape", "--apply", chdir: root)
  abort "path traversal accepted" if unsafe.success?
  _output, overwrite = Open3.capture2e(CLI, "runtime-profile-apply", "profile", "--apply", chdir: root)
  abort "non-empty destination accepted" if overwrite.success?
  abort "user file changed" unless File.read(File.join(root, "keep.txt")) == "keep"
end
puts "Dora runtime profile apply test passed (dry-run, safe apply, traversal rejection, and preservation)."
