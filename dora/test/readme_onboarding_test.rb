#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
readme = File.read(File.join(ROOT, "README.md"))
guide = File.read(File.join(ROOT, "docs/new-project.md"))

abort "README does not link the beginner guide" unless readme.include?("docs/new-project.md")
["dora_bootstrap_source", "forty-character", "dora bootstrap", "--source", "--starter blank", "--starter spring-vue", "./bin/dora doctor"].each do |required|
  abort "beginner guide is missing #{required}" unless guide.include?(required)
end
abort "beginner guide permits implicit downloads" if guide.match?(/curl|wget|git clone/i)
abort "beginner guide does not state product boundaries" unless guide.include?("business domain") && guide.include?("authentication")

puts "Dora onboarding guide test passed (explicit local beginner bootstrap path)."
