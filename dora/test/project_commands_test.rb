#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/project_commands"

Dir.mktmpdir("dora-project-commands") do |sandbox|
  valid = File.join(sandbox, "valid.yaml")
  invalid = File.join(sandbox, "invalid.yaml")
  File.write(valid, YAML.dump({"kind" => "dora_project_commands", "version" => 1, "commands" => {"setup" => "npm ci", "test" => "npm test", "build" => "npm run build"}}))
  File.write(invalid, YAML.dump({"kind" => "dora_project_commands", "version" => 1, "commands" => {"setup" => "npm ci", "test" => "dora doctor", "build" => ""}}))
  abort "valid project command declarations failed" unless Dora::ProjectCommands.load!(valid).fetch("build") == "npm run build"
  begin
    Dora::ProjectCommands.load!(invalid)
    abort "invalid project command declarations passed"
  rescue ArgumentError
    nil
  end
end

puts "Dora project command test passed (explicit valid and invalid command declarations)."
