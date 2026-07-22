#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

registry = YAML.load_file("docs/delivery-dependency-registry.yaml")
paths = registry.fetch("build_paths")
abort "delivery provenance requires build paths" unless paths.is_a?(Array) && !paths.empty?
paths.each do |entry|
  %w[id source].each { |field| abort "build path missing #{field}" if entry[field].to_s.empty? }
  abort "missing build source: #{entry['source']}" unless File.file?(entry["source"])
  [entry["lockfile"], entry["output"]].compact.each do |path|
    next unless path.start_with?("apps/", "docs/", "scripts/")

    abort "missing provenance path: #{path}" unless File.file?(path)
  end
end

release = registry.fetch("release_provenance")
%w[repository_visible_ci repository_visible_containerization repository_visible_deployment_manifest repository_visible_rollback_automation].each do |field|
  abort "release provenance missing #{field}" unless release.key?(field)
end
abort "external release interpretation must remain explicit" unless release["interpretation"] == "unknown_not_absent_in_external_environment"
abort "future external evidence list is required" unless Array(registry["required_future_evidence"]).any?

puts "Delivery provenance audit passed"
puts "  build paths: #{paths.length}"
puts "  external release state: #{release['interpretation']}"
