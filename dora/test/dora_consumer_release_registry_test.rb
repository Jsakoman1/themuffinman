#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
registry = YAML.load_file(File.join(ROOT, "docs/dora-consumer-release-registry.yaml"))
template = YAML.load_file(File.join(ROOT, "templates/dora-release-manifest.yaml"))

abort "consumer release registry kind is invalid" unless registry["kind"] == "dora_consumer_release_registry" && registry["version"] == 1
abort "consumer release registry authority boundary is missing" unless registry["authority_boundary"].is_a?(String) && registry["authority_boundary"].include?("cannot discover, mutate, deploy, verify")
contract = registry.fetch("update_contract")
abort "consumer release registry pin record is invalid" unless contract["consumer_pin_record"] == ".dora/bootstrap-source.yaml" && contract["package_path"] == "dora"
required_flow = %w[owner_approval reviewed_local_source upgrade_preview upgrade_apply consumer_validation isolated_commit]
abort "consumer release registry flow is invalid" unless contract["required_flow"] == required_flow
consumers = registry.fetch("consumers")
ids = consumers.map { |consumer| consumer.fetch("id") }
abort "consumer release registry ids are not unique" unless ids.uniq == ids && ids.sort == ids
consumers.each do |consumer|
  abort "consumer repository must be a URL" unless consumer.fetch("repository").start_with?("https://")
  abort "consumer release branch is invalid" unless consumer.fetch("release_branch") == "main"
  abort "consumer adapter/package path is invalid" unless consumer.fetch("adapter") == ".dora/project.yaml" && consumer.fetch("package_path") == "dora"
  abort "consumer migration status is invalid" unless consumer.fetch("migration_status") == "snapshot-pin-migration-required"
  abort "consumer registry leaked a local path" if consumer.values.any? { |value| value.is_a?(String) && value.start_with?("/") }
end
abort "release manifest template kind is invalid" unless template["kind"] == "dora_release_manifest" && template["version"] == 1
abort "release manifest template boundary is missing" unless template.fetch("boundaries").any? { |line| line.include?("does not claim") }

puts "Dora consumer release registry test passed (explicit consumers, pinned update contract, and no fleet authority)."
