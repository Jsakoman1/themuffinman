#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
readme = File.read(File.join(ROOT, "README.md"))
guide = File.read(File.join(ROOT, "docs/agent-first-application-guide.md"))
%w[product\ brief domain\ library agent\ project\ profile capability\ blueprint voice\ blueprint atomic\ plan tests runtime\ evidence hand\ off].each do |phrase|
  abort "agent-first guide is missing #{phrase}" unless guide.downcase.include?(phrase)
end
abort "agent-first guide treats Dora as product authority" unless guide.downcase.include?("not the authority")
abort "README does not link the agent-first guide" unless readme.include?("agent-first-application-guide.md")

puts "Dora agent-first operator guide test passed (idea through knowledge, plan, implementation, validation, runtime evidence, and handoff)."
