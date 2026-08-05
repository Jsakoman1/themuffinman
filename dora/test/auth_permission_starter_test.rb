#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/auth_permission_starter"

input = {
  "kind" => "dora_auth_permission_starter", "version" => 1,
  "authentication" => {"mode" => "local-login", "confirmed" => true},
  "roles" => [{"id" => "owner", "description" => "May manage household data.", "confirmed" => true}, {"id" => "viewer", "description" => "May view permitted data.", "confirmed" => true}],
  "ownership" => {"rule" => "A record belongs to one declared household.", "confirmed" => true},
  "audit_events" => [{"id" => "supply-created", "description" => "Record creation is auditable.", "confirmed" => true}],
  "confirmation" => true
}
valid = Dora::AuthPermissionStarter.validate!(input)
abort "role was not preserved" unless valid.fetch("roles").map { |role| role.fetch("id") } == %w[owner viewer]
invalid = Marshal.load(Marshal.dump(input)); invalid.fetch("ownership")["confirmed"] = false
begin
  Dora::AuthPermissionStarter.validate!(invalid)
  abort "unconfirmed ownership was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("ownership")
end
puts "Dora auth permission starter test passed (only explicit roles, ownership, auth mode, and audit events are accepted)."
