#!/usr/bin/env ruby
# frozen_string_literal: true

root = File.expand_path("..", __dir__)
readme = File.read(File.join(root, "README.md"))
guide = File.read(File.join(root, "docs/agent-first-application-guide.md"))
%w[create-app --interview --source checksum reviewed local source technical only capability package starting context].each do |phrase|
  abort "README is missing #{phrase}" unless readme.include?(phrase)
end
%w[codex-idea-interview.md create-app --interview --source diagnose next capability package does\ not\ create\ product\ implementation].each do |phrase|
  abort "agent-first guide is missing #{phrase}" unless guide.include?(phrase)
end
puts "Dora create-app guidance test passed (interview route, reviewed source, starter, readiness, and non-implementation boundaries are current)."
