#!/usr/bin/env ruby
# frozen_string_literal: true
require "yaml"

policy = YAML.load_file(File.expand_path("../docs/agent-request-routing-policy.yaml", __dir__))
abort "routing policy kind is invalid" unless policy.values_at("kind", "version", "default") == ["dora_agent_request_routing_policy", 1, "classify_before_planning"]
routes = policy.fetch("routes").to_h { |route| [route.fetch("request_shape"), route] }
abort "bounded delivery route is unsafe" unless routes.fetch("bounded_delivery").values_at("route", "idc_profile") == ["dora_direct", "none"]
%w[wide_research greenfield_discovery].each { |shape| abort "IDC route lacks current owner gate" unless routes.fetch(shape).fetch("owner_authorization") == "current_request_required_before_render" }
abort "master plan route lacks hardening" unless policy.dig("master_plan", "required").include?("atomic_hardening")
abort "goal pursuing lacks owner authorization" unless policy.dig("goal_pursuing", "required") == ["explicit_owner_authorization_after_readback"]
abort "routing policy grants authority" if policy.fetch("boundaries").join(" ").match?(/can select|can render|can start/)
puts "Dora agent request routing policy test passed (IDC triage is advisory and goal pursuing remains owner-gated)."
