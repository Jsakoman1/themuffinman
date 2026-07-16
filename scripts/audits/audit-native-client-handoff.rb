#!/usr/bin/env ruby
require "yaml"

contract = YAML.load_file("docs/native-client-handoff-contract.yaml")
raise "Native handoff contract must be contract_ready" unless contract.fetch("status") == "contract_ready"
raise "Native handoff contract needs backend authority" if contract.fetch("backend_authority").empty?
clients = contract.fetch("clients")
%w[iphone watch].each do |client|
  data = clients.fetch(client)
  raise "#{client} status is missing" unless data["status"]
  raise "#{client} required surfaces are missing" if data.fetch("required_surfaces").empty?
  raise "#{client} requirements are missing" if data.fetch("requirements").empty?
end
puts "Native client handoff audit passed (#{clients.length} clients, contract-ready)."
