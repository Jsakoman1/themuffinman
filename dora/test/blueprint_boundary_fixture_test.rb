#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/blueprint_validator"

ROOT = File.expand_path("..", __dir__)
fixture_path = File.join(ROOT, "fixtures/agent-first-project/blueprints.yaml")
result = Dora::BlueprintValidator.validate!(fixture_path)
abort "blueprint validator lost neutral capability" unless result.fetch("capability") == "shared-note"

Dir.mktmpdir("dora-blueprint-boundary") do |root|
  invalid = YAML.load_file(fixture_path)
  invalid.fetch("voice_blueprint").fetch("execution")["rule"] = "execute immediately"
  path = File.join(root, "invalid.yaml")
  File.write(path, YAML.dump(invalid))
  begin
    Dora::BlueprintValidator.validate!(path)
    abort "blueprint validator accepted unconfirmed execution"
  rescue ArgumentError => error
    abort "wrong execution boundary failure: #{error.message}" unless error.message.include?("conditional on confirmation")
  end

  invalid = YAML.load_file(fixture_path)
  invalid.fetch("capability_blueprint")["capability"] = "muffinman-note"
  File.write(path, YAML.dump(invalid))
  begin
    Dora::BlueprintValidator.validate!(path)
    abort "blueprint validator accepted a product-specific default"
  rescue ArgumentError => error
    abort "wrong product boundary failure: #{error.message}" unless error.message.include?("product-specific")
  end
end

puts "Dora blueprint boundary fixture test passed (neutral defaults, no credentials, no raw memory, and no unconfirmed execution)."
