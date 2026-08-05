#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/starter_compatibility"

def compatibility(requirements: nil)
  requirements ||= [
    {"id" => "java", "command" => "java -version", "required" => true, "purpose" => "Compile and run the backend."},
    {"id" => "maven", "command" => "mvn -version", "required" => true, "purpose" => "Build the backend."},
    {"id" => "node", "command" => "node --version", "required" => true, "purpose" => "Run frontend tooling."},
    {"id" => "npm", "command" => "npm --version", "required" => true, "purpose" => "Install and run frontend packages."},
    {"id" => "docker", "command" => "docker version", "required" => true, "purpose" => "Run local PostgreSQL."},
    {"id" => "compose", "command" => "docker compose version", "required" => true, "purpose" => "Coordinate local services."},
    {"id" => "playwright-browser", "command" => "npx playwright --version", "required" => false, "purpose" => "Run an opt-in browser proof."}
  ]
  {"kind" => "dora_starter_compatibility", "version" => 1, "starter_id" => "spring-vue-postgres-buildable", "requirements" => requirements, "observation_boundary" => "read_only", "completion_boundary" => "A compatibility declaration is not proof that a host is ready or that a starter has been applied."}
end

valid = Dora::StarterCompatibility.validate!(compatibility)
abort "valid compatibility lost the starter id" unless valid.fetch("starter_id") == "spring-vue-postgres-buildable"

missing = compatibility(requirements: compatibility.fetch("requirements").reject { |requirement| requirement.fetch("id") == "docker" })
begin
  Dora::StarterCompatibility.validate!(missing)
  abort "missing Docker prerequisite was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("docker")
end

unsafe = compatibility.merge("observation_boundary" => "install_tools")
begin
  Dora::StarterCompatibility.validate!(unsafe)
  abort "mutating compatibility contract was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("read_only")
end

browser_required = compatibility(requirements: compatibility.fetch("requirements").map { |requirement| requirement.fetch("id") == "playwright-browser" ? requirement.merge("required" => true) : requirement })
begin
  Dora::StarterCompatibility.validate!(browser_required)
  abort "required browser tooling was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("optional tool playwright-browser")
end

puts "Dora starter compatibility test passed (declared technical prerequisites are validated without host probing or mutation)."
