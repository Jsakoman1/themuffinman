#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/operational_readiness"

declaration = {"id" => "app-config", "purpose" => "Declared by the consumer environment."}
input = {"kind" => "dora_operational_readiness", "version" => 1, "environment" => [declaration], "secret_references" => [{"id" => "database-credential", "reference" => "consumer-managed-secret-reference"}], "health" => "Declare a health endpoint.", "logging" => "Declare structured error logging.", "rate_limits" => "Declare rate-limit policy.", "dependencies" => [declaration], "deployment_runbook" => "Write and review a deployment runbook.", "confirmation" => true}
abort "valid operational profile was rejected" unless Dora::OperationalReadiness.validate!(input).fetch("health") == "Declare a health endpoint."
invalid = Marshal.load(Marshal.dump(input)); invalid.fetch("secret_references").first["value"] = "not-allowed"
begin
  Dora::OperationalReadiness.validate!(invalid)
  abort "literal secret value was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("secret")
end
template = YAML.load_file(File.join(File.expand_path("..", __dir__), "templates/operational-readiness.yaml"))
abort "operational template permits deployment" unless template.fetch("deployment_boundary").include?("explicit approval")
puts "Dora operational readiness test passed (declared prerequisites are validated without secret values or deployment behavior)."
