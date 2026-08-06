#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
readme = File.read(File.join(ROOT, "README.md"))
guide = File.read(File.join(ROOT, "docs/compiled-feature-route.md"))
abort "README does not expose compiled feature preview" unless readme.include?("compiled-feature-preview")
abort "guide does not state confirmed input boundary" unless guide.include?("explicitly confirmed")
abort "guide does not state safe apply boundary" unless guide.include?("rejects traversal, collisions")
abort "guide does not state external approval boundary" unless guide.include?("requires explicit approval")
abort "guide implies generation is completion" if guide.match?(/generated feature is complete|automatic release/i)
puts "Dora compiled feature documentation test passed (Codex route, confirmation, safe apply, approval, and completion boundaries are documented)."
