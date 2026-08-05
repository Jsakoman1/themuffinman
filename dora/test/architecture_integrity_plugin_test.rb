#!/usr/bin/env ruby
# frozen_string_literal: true
require "fileutils"; require "tmpdir"; require_relative "../lib/dora/plugins/architecture_integrity"
Dir.mktmpdir("dora-architecture") do |root|
  FileUtils.mkdir_p(File.join(root, "src")); File.write(File.join(root, "src/a.rb"), "require 'allowed'")
  Dora::Plugins::ArchitectureIntegrity.validate_paths!(root: root, paths: ["src/a.rb"])
  abort "forbidden scan is wrong" unless Dora::Plugins::ArchitectureIntegrity.scan_forbidden!(root: root, rules: [{"id" => "forbid", "source_glob" => "src/**/*.rb", "forbidden_pattern" => "forbidden"}]).empty?
end
puts "Dora architecture integrity plugin test passed (declared paths and rules)."
