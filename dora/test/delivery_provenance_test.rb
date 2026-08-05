#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/delivery_provenance"

Dir.mktmpdir("dora-delivery-provenance") do |root|
  FileUtils.mkdir_p(File.join(root, "docs"))
  File.write(File.join(root, "docs/evidence.txt"), "evidence")
  system("git", "init", "-q", root) || abort("cannot initialize fixture Git repository")
  system("git", "-C", root, "add", ".") || abort("cannot stage fixture")
  system("git", "-C", root, "-c", "user.name=Dora", "-c", "user.email=dora@example.test", "commit", "-qm", "fixture") || abort("cannot commit fixture")
  config = File.join(root, "provenance.yaml")
  File.write(config, YAML.dump({"kind" => "dora_delivery_provenance", "version" => 1, "validation_command" => "ruby test.rb", "evidence_paths" => ["docs/evidence.txt"]}))
  record = Dora::DeliveryProvenance.record!(config, "docs/provenance.yaml", project_root: root)
  abort "provenance record lacks revision" unless record.fetch("revision").match?(/\A[0-9a-f]{40}\z/)
  abort "provenance record made a completion claim" unless record.fetch("claims") == ["Records declared validation and evidence inputs only."]
end

puts "Dora delivery provenance test passed (portable revision and evidence record)."
