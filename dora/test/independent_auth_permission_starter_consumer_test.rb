#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require_relative "../lib/dora/starter_pack"

ROOT = File.expand_path("..", __dir__)
PACK = File.join(ROOT, "starters/auth-permission-neutral.yaml")

Dir.mktmpdir("dora-auth-permission-consumer") do |root|
  preview = Dora::StarterPack.load!(PACK)
  abort "starter is not technical only" unless preview.fetch("technical_only") == true
  result = Dora::StarterPack.apply!(PACK, project_root: root)
  readme = File.join(root, "README.md")
  marker = File.join(root, ".dora/auth-permission-starter.yaml")
  abort "neutral starter files are missing" unless result.fetch("id") == "auth-permission-neutral" && File.file?(readme) && File.file?(marker)
  generated = Dir.glob(File.join(root, "**", "*"), File::FNM_DOTMATCH).select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "starter introduced credentials" if generated.match?(/password\s*[:=]|default-user|secret/i)
  abort "starter introduced a product policy" if generated.match?(/owner\s+may|viewer\s+may|allow\s+/i)
end

puts "Dora independent auth permission starter consumer test passed (fresh consumer receives only neutral, credential-free, policy-free surfaces)."
