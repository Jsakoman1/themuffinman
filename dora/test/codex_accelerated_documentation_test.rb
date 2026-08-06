#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
readme = File.read(File.join(ROOT, "README.md"))
guide = File.read(File.join(ROOT, "docs/codex-accelerated-route.md"))
registry = File.read(File.join(ROOT, "lib/dora/command_registry.rb"))

%w[guided-next codex-context capability-graph convention-check proof-packet].each do |command|
  abort "README does not document #{command}" unless readme.include?(command)
  abort "Codex guide does not document #{command}" unless guide.include?(command)
  abort "command registry does not expose #{command}" unless registry.include?(command)
end
%w[does\ not\ create consumer\ project explicit\ approval].each do |boundary|
  abort "Codex guide lacks boundary #{boundary}" unless guide.include?(boundary)
end
abort "Codex guide makes a completion claim" if guide.match?(/Dora.*proves.*release/i)

puts "Dora Codex accelerated documentation test passed (one guided route, declared command surface, ownership, approval, and completion boundaries are documented)."
