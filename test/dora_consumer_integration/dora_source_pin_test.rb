#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require File.expand_path("../../dora/lib/dora/bootstrap_source", __dir__)

ROOT = File.expand_path("../..", __dir__)
EXPECTED_REF = "64ddda6e0685eb1b79b401deb80ec5775461b57a"
EXPECTED_CHECKSUM = "1c6468ad3ee2d0d60d60c636706ebe20f9c4c0b92cce248e6113a772b78e93fa"

adapter = YAML.load_file(File.join(ROOT, ".dora/project.yaml"))
record = YAML.load_file(File.join(ROOT, ".dora/bootstrap-source.yaml"))
distribution = adapter.fetch("distribution")
source = record.fetch("source")
package = File.join(ROOT, record.fetch("package_path"))

abort "MuffinMan Dora runtime is not a release-pinned package" unless distribution.fetch("method") == "release_pinned_package"
abort "MuffinMan Dora adapter has the wrong release tag" unless distribution.fetch("source_ref") == "v1.11.4"
abort "MuffinMan Dora adapter and bootstrap record disagree" unless distribution.fetch("source_commit") == source.fetch("ref") && distribution.fetch("source_checksum") == source.fetch("checksum")
abort "MuffinMan Dora release ref changed unexpectedly" unless source.fetch("ref") == EXPECTED_REF
abort "MuffinMan Dora release checksum changed unexpectedly" unless source.fetch("checksum") == EXPECTED_CHECKSUM
abort "MuffinMan Dora package root is missing" unless File.file?(File.join(package, "bin/dora")) && File.directory?(File.join(package, "lib/dora"))
abort "MuffinMan Dora package retained source-control or IDE metadata" if File.exist?(File.join(package, ".git")) || File.exist?(File.join(package, ".idea"))
abort "MuffinMan retains obsolete local Dora source descriptor" if File.exist?(File.join(ROOT, "dora-source.yaml"))
abort "MuffinMan Dora package checksum does not match its reviewed release pin" unless Dora::BootstrapSource.checksum_for(package) == EXPECTED_CHECKSUM

puts "MuffinMan Dora release-pinned package test passed (immutable package pin, no local source descriptor, and no copied metadata)."
