#!/usr/bin/env ruby

require "json"
require "fileutils"
require "time"

ROOT = File.expand_path("../..", __dir__)
PROPERTIES = File.join(ROOT, "apps/themuffinman/src/main/resources/application.properties")
REGISTRY = File.join(ROOT, "docs/security-operations-recovery-registry.yaml")
lines = File.readlines(PROPERTIES, chomp: true)
properties = lines.reject { |line| line.strip.empty? || line.lstrip.start_with?("#") }.each_with_object([]) do |line, result|
  key, value = line.split("=", 2)
  next if key.to_s.empty?
  env = value.to_s[/\$\{([^}:]+)(?::[^}]*)?\}/, 1]
  result << {"name" => key, "environment_name" => env, "secret" => key.match?(/password|secret|api-key|access-key|secret-key/i)}
end
secret_names = properties.select { |property| property["secret"] }.map { |property| property["name"] }
unmapped = properties.select { |property| property["name"].start_with?("app.") && property["environment_name"].nil? }

unless File.file?(REGISTRY)
  abort "Configuration environment drift audit failed: security registry is missing"
end
unless unmapped.empty?
  warn "Configuration environment drift audit failed: app properties without environment mapping"
  unmapped.each { |property| warn "  #{property['name']}" }
  exit 1
end

report = {
  "generated_at" => Time.now.utc.iso8601,
  "advisory_only" => true,
  "property_count" => properties.size,
  "environment_mapped_count" => properties.count { |property| !property["environment_name"].nil? },
  "secret_property_names" => secret_names,
  "secret_values" => "redacted",
  "external_state" => "deployment, provider rotation, network egress, and production rollout unknown",
  "unmapped_app_properties" => unmapped.map { |property| property["name"] }
}
output_dir = File.join(ROOT, "docs/audit-output")
FileUtils.mkdir_p(output_dir)
File.write(File.join(output_dir, "configuration-environment-drift.json"), JSON.pretty_generate(report) + "\n")

puts "Configuration/environment drift audit passed"
puts "  properties checked: #{properties.size}"
puts "  environment mappings: #{report['environment_mapped_count']}"
puts "  secret property names classified: #{secret_names.size} (values redacted)"
puts "  external deployment/provider state: unknown"
