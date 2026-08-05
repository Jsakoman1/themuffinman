#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "rbconfig"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

def compatibility
  requirements = [["java", "java -version"], ["maven", "mvn -version"], ["node", "node --version"], ["npm", "npm --version"], ["docker", "docker version"], ["compose", "docker compose version"], ["playwright-browser", "npx playwright --version"]]
  {"kind" => "dora_starter_compatibility", "version" => 1, "starter_id" => "spring-vue-postgres-buildable", "requirements" => requirements.map { |id, command| {"id" => id, "command" => command, "required" => id != "playwright-browser", "purpose" => "Fixture observation for #{id}."} }, "observation_boundary" => "read_only", "completion_boundary" => "A compatibility declaration is not proof that a host is ready or that a starter has been applied."}
end

Dir.mktmpdir("dora-app-readiness") do |sandbox|
  tools = File.join(sandbox, "tools")
  Dir.mkdir(tools)
  %w[java mvn node npm docker npx].each do |tool|
    path = File.join(tools, tool)
    File.write(path, "#!/bin/sh\nexit 0\n")
    File.chmod(0o755, path)
  end
  profile = File.join(sandbox, "compatibility.yaml")
  File.write(profile, YAML.dump(compatibility))
  environment = {"PATH" => "#{tools}:/usr/bin:/bin"}

  output, status = Open3.capture2e(environment, RbConfig.ruby, CLI, "app-readiness", profile, "--format", "json", chdir: ROOT)
  abort "app readiness command failed: #{output}" unless status.success?
  payload = JSON.parse(output).fetch("payload")
  abort "declared fixtures were not ready" unless payload.fetch("ready_to_start") && payload.fetch("blocking_gaps").empty?
  abort "optional browser status was not reported" unless payload.fetch("requirements").find { |row| row.fetch("id") == "playwright-browser" }.fetch("status") == "available"

  File.delete(File.join(tools, "docker"))
  output, status = Open3.capture2e(environment, RbConfig.ruby, CLI, "app-readiness", profile, "--format", "json", chdir: ROOT)
  abort "missing tool report failed: #{output}" unless status.success?
  missing = JSON.parse(output).fetch("payload")
  abort "missing Docker was accepted" if missing.fetch("ready_to_start")
  abort "exact Docker gap was omitted" unless missing.fetch("blocking_gaps").any? { |gap| gap.include?("docker") }
  abort "readiness claimed starter application" unless missing.fetch("completion_boundary").include?("does not apply a starter")
end

puts "Dora app readiness command test passed (declared local prerequisites are observed without installation or project mutation)."
