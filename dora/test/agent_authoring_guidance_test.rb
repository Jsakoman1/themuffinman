#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
files = ["README.md", "docs/agent-first-application-guide.md", "docs/operator-guide.md"]
required = %w[interview-start interview-next interview-answer authoring-next app-readiness vertical-slice decision-record runtime-profile-apply user_confirmed explicit user approval]
files.each do |relative|
  text = File.read(File.join(ROOT, relative))
  required.each { |phrase| abort "#{relative} is missing #{phrase}" unless text.include?(phrase) }
  abort "#{relative} promises inferred product implementation" if text.match?(/Dora will infer business rules|automatically implement product/i)
end
puts "Dora agent authoring guidance test passed (guided commands, non-inference, browser approval, and product boundaries are documented)."
