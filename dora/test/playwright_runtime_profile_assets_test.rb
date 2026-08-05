#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/runtime_proof_profile"

ROOT = File.expand_path("..", __dir__)
pack = YAML.load_file(File.join(ROOT, "packs/playwright-runtime-proof.yaml"))
profile = Dora::RuntimeProofProfile.validate!(pack.fetch("profile"))
abort "runtime pack lost explicit browser boundary" unless profile.fetch("browser_installation") == "explicit_opt_in"
%w[package.json playwright.config.mjs tests/technical-health.spec.mjs].each do |relative|
  path = File.join(ROOT, "templates/runtime-profiles/playwright", relative)
  abort "runtime asset missing #{relative}" unless File.file?(path)
end
spec = File.read(File.join(ROOT, "templates/runtime-profiles/playwright/tests/technical-health.spec.mjs"))
abort "technical test does not assert neutral marker" unless spec.include?("technical-health")
abort "technical test contains product wording" if spec.match?(/supply|inventory|user|seed|production/i)
puts "Dora Playwright runtime proof assets test passed (static opt-in technical-health assets exclude product behavior)."
