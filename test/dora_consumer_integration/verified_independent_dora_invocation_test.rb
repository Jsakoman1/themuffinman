#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"

ROOT = File.expand_path("../..", __dir__)
LAUNCHER = File.join(ROOT, "bin/dora")

launcher = File.read(LAUNCHER)
abort "MuffinMan launcher does not use the vendored release package" unless launcher.include?("package_root=\"$project_root/dora\"")
abort "MuffinMan launcher retains an obsolete local-source override" if launcher.include?("DORA_SOURCE_PATH") || launcher.include?("dora-source.yaml")

help, help_status = Open3.capture2e(LAUNCHER, "help", chdir: ROOT)
abort "MuffinMan release-pinned Dora launcher did not execute: #{help}" unless help_status.success? && help.include?("Dora commands:")

doctor, doctor_status = Open3.capture2e(LAUNCHER, "doctor", ".dora/project.yaml", chdir: ROOT)
abort "MuffinMan release-pinned Dora Doctor did not accept its declared lazy output paths: #{doctor}" unless doctor_status.success? && doctor.include?("PASSED project-root") && doctor.include?("PASSED path:audit_output")

puts "MuffinMan release-pinned Dora invocation test passed (vendored package only, no local-source fallback)."
