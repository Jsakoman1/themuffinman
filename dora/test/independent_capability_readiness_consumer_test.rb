#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/capability_readiness"

fixture = YAML.load_file(File.join(__dir__, "fixtures/capability-readiness-consumers.yaml"))
abort "readiness consumer fixture is invalid" unless fixture["kind"] == "dora_capability_readiness_consumers"

def package(consumer)
  unresolved = consumer.fetch("state") == "ready" ? [] : [{"id" => "runtime", "reason" => "Runtime evidence is pending."}]
  runtime_status = consumer.fetch("state") == "ready" ? "recorded" : "unresolved"
  {"kind" => "dora_capability_package", "version" => 1, "id" => consumer.fetch("capability"), "title" => consumer.fetch("capability"), "intent" => {"problem" => "Declared consumer problem.", "capability" => consumer.fetch("capability"), "source_reference" => "docs/idea.yaml"}, "domain" => {"entity_ids" => [consumer.fetch("entity")], "invariant_ids" => ["owner"], "permission_rule_ids" => [consumer.fetch("permission")], "workflow_id" => consumer.fetch("workflow")}, "api" => {"service_owner" => "consumer", "operations" => [{"id" => "record", "purpose" => "Record declared capability."}]}, "tests" => {"scenarios" => [{"id" => "test", "action" => "Test.", "expected" => "Pass.", "status" => "recorded"}]}, "runtime" => {"scenarios" => [{"id" => "runtime", "action" => "Run.", "expected" => "Observe.", "status" => runtime_status}]}, "work" => {"plan" => "docs/work/first.yaml", "task" => "implement-first"}, "unresolved" => unresolved}
end

reports = fixture.fetch("consumers").map do |consumer|
  permissions = consumer.fetch("state") == "blocked_permission" ? [] : [{"id" => consumer.fetch("permission")}]
  domain = {"entities" => [{"id" => consumer.fetch("entity")}], "invariants" => [{"id" => "owner"}], "permission_rules" => permissions, "workflows" => [{"id" => consumer.fetch("workflow")}]} 
  [consumer, Dora::CapabilityReadiness.report!(capability: package(consumer), domain: domain)]
end
garden = reports.find { |consumer, _report| consumer.fetch("id") == "garden" }[1]
roster = reports.find { |consumer, _report| consumer.fetch("id") == "roster" }[1]
abort "ready consumer was blocked" unless garden.fetch("ready_to_implement") && garden.fetch("blocking_gaps").empty?
abort "blocked consumer was accepted" if roster.fetch("ready_to_implement")
abort "blocked consumer omitted its own permission" unless roster.fetch("blocking_gaps").any? { |gap| gap.include?("record-availability") }
abort "consumer guidance leaked across projects" if garden.to_s.include?("availability") || roster.to_s.include?("garden-note")
puts "Dora independent capability readiness consumer test passed (two consumers receive only their own ready state or exact blockers)."
