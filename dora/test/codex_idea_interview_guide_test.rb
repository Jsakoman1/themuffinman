#!/usr/bin/env ruby
# frozen_string_literal: true

guide = File.read(File.expand_path("../docs/codex-idea-interview.md", __dir__))
%w[target_users first_problem first_capability domain_concepts permission_intent workflow_intent forbidden_outcomes source:\ user user_confirmed unanswered decision Do\ not\ convert].each do |phrase|
  abort "Codex interview guide is missing #{phrase}" unless guide.include?(phrase)
end
abort "Codex interview guide promises implementation" if guide.include?("approves implementation")
puts "Dora Codex idea interview guide test passed (required user provenance, open decisions, and no-inference boundary are explicit)."
