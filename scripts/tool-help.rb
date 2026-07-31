#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
CATALOG = YAML.load_file(File.join(ROOT, "docs/local-tool-catalog.yaml"))
COMMANDS = Array(CATALOG["commands"])

def make_target_exists?(target)
  File.read(File.join(ROOT, "Makefile")).match?(/^#{Regexp.escape(target)}:/)
end

if ARGV == ["--check"]
  abort "Tool help catalog kind is invalid" unless CATALOG["kind"] == "local_tool_catalog_metadata"
  abort "Tool help has no commands" if COMMANDS.empty?
  missing = COMMANDS.reject { |command| make_target_exists?(command.fetch("target")) }
  abort "Tool help references missing Make targets: #{missing.map { |command| command.fetch("target") }.join(", ")}" unless missing.empty?
  puts "Tool help check passed (#{COMMANDS.length} catalog commands map to Make targets)."
  exit 0
end

abort "usage: ruby scripts/tool-help.rb [--check]" unless ARGV.empty?
puts "Local developer commands"
COMMANDS.each do |command|
  puts "  make #{command.fetch("target")} [#{command.fetch("expected_cost")}] — #{command.fetch("purpose")}" 
end
