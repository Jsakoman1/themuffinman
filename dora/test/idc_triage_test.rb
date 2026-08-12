#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/idc_triage"

def triage_request(overrides = {})
  {"kind" => "dora_idc_triage_request", "version" => 1, "id" => "owner-request", "request_shape" => "wide_research", "profile" => "research_dossier", "source_scope" => "explicit_owner_selected_only", "authorization_scope" => "current_request_only", "owner_authorization" => "not_granted"}.merge(overrides)
end

bounded = Dora::IdcTriage.evaluate!(request: triage_request("request_shape" => "bounded_delivery", "profile" => "none"))
abort "bounded delivery did not avoid IDC" unless bounded.fetch("outcome") == "NO_IDC_NEEDED" && bounded.fetch("profile") == "none" && !bounded.fetch("owner_confirmation_required")
abort "bounded delivery triage implied render authority" if bounded.fetch("next_action").include?("render")

confirmation = Dora::IdcTriage.evaluate!(request: triage_request)
abort "unapproved IDC request did not require owner confirmation" unless confirmation.fetch("outcome") == "IDC_OWNER_CONFIRMATION_REQUIRED" && confirmation.fetch("owner_confirmation_required")

authorized = Dora::IdcTriage.evaluate!(request: triage_request("request_shape" => "greenfield_discovery", "profile" => "greenfield_product_delivery_baseline", "owner_authorization" => "authorize_local_idc_render"))
abort "explicit owner authorization did not permit only local render" unless authorized.fetch("outcome") == "IDC_OWNER_AUTHORIZED_LOCAL_RENDER" && authorized.fetch("profile") == "greenfield_product_delivery_baseline" && !authorized.fetch("owner_confirmation_required")
abort "triage granted canonical authority" if authorized.fetch("authority_boundary").match?(/create or amend Dora decisions\/plans\/evidence\/verified status/).nil?
abort "triage readback is not a fixed Bridge-safe shape" unless authorized.keys.sort == %w[authority_boundary disposition kind next_action outcome owner_confirmation_required profile read_only version]
abort "triage readback retained request identity" if authorized.key?("id") || authorized.to_s.include?("owner-request")

[%w[authorization_scope standing_permission], %w[source_scope automatic_selection], %w[owner_authorization authorize_local_idc_render]].each do |field, value|
  invalid = triage_request(field => value)
  invalid["request_shape"] = "bounded_delivery" if field == "owner_authorization"
  invalid["profile"] = "none" if field == "owner_authorization"
  begin
    Dora::IdcTriage.evaluate!(request: invalid)
    abort "invalid triage input was accepted: #{field}"
  rescue ArgumentError
    nil
  end
end

malformed = triage_request.merge("path" => "../../.env")
begin
  Dora::IdcTriage.evaluate!(request: malformed)
  abort "triage accepted an authority/path field"
rescue ArgumentError
  nil
end

source = File.read(File.expand_path("../lib/dora/idc_triage.rb", __dir__))
abort "triage exposes process authority" if source.match?(/\bOpen3\b|\bsystem\s*\(|\bexec\s*\(|\bspawn\s*\(/)
abort "triage exposes external authority" if source.match?(/Net::HTTP|URI\.open|["']git["']/)

puts "Dora IDC triage test passed (bounded delivery, explicit owner gate, fail-closed inputs, and no runtime authority)."
