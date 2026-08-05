#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/runtime_proof_profile"

profile = {"kind" => "dora_runtime_proof_profile", "version" => 1, "id" => "playwright-technical-health", "browser_installation" => "explicit_opt_in", "test_command" => "npm run test:runtime", "evidence_destination" => "docs/runtime-evidence/technical-health.json", "non_guarantees" => ["No product flow is tested."], "product_boundary" => "technical_health_only"}
valid = Dora::RuntimeProofProfile.validate!(profile)
abort "valid profile lost opt-in browser boundary" unless valid.fetch("browser_installation") == "explicit_opt_in"
product = profile.merge("entity" => "supply")
begin; Dora::RuntimeProofProfile.validate!(product); abort "product profile was accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("product behavior"); end
implicit = profile.merge("browser_installation" => "automatic")
begin; Dora::RuntimeProofProfile.validate!(implicit); abort "implicit browser installation was accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("explicit opt_in"); end
puts "Dora runtime proof profile test passed (neutral technical proof requires explicit browser opt-in and rejects product behavior)."
