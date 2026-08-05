#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/starter_pack"

Dir.mktmpdir("dora-starter-pack") do |sandbox|
  pack_path = File.join(sandbox, "blank.yaml")
  pack = {
    "kind" => "dora_starter_pack", "version" => 1, "id" => "blank", "technical_only" => true,
    "commands" => {"setup" => "true", "test" => "true", "build" => "true"},
    "directories" => ["src"], "files" => {"README.md" => "# Starter\n"},
    "boundaries" => ["No product domain, authentication, database schema, or user-facing feature is generated."]
  }
  File.write(pack_path, YAML.dump(pack))
  abort "starter pack was not accepted" unless Dora::StarterPack.load!(pack_path).fetch("id") == "blank"

  pack["technical_only"] = false
  File.write(pack_path, YAML.dump(pack))
  begin
    Dora::StarterPack.load!(pack_path)
    abort "starter pack accepted a non-technical declaration"
  rescue ArgumentError
    # expected
  end
end

puts "Dora starter pack contract test passed (technical skeletons only)."
