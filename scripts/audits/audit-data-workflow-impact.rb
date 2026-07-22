#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

migrations = Dir["apps/themuffinman/src/main/resources/db/migration/V*__*.sql"]
abort "no Flyway migrations found" if migrations.empty?
versions = migrations.map { |path| File.basename(path)[/\AV(\d+)__/, 1].to_i }
abort "duplicate Flyway migration versions" unless versions.uniq.length == versions.length

data = YAML.load_file("docs/data-ownership-registry.yaml")
domains = data.fetch("domains")
abort "data ownership registry must contain domains" unless domains.is_a?(Array) && !domains.empty?
ids = domains.map { |domain| domain["id"] }
abort "duplicate data ownership domain IDs" unless ids.uniq.length == ids.length
domains.each do |domain|
  %w[id owner_package repositories aggregates cross_domain_role].each { |field| abort "data domain missing #{field}" if domain[field].nil? || Array(domain[field]).empty? }
end

workflow = YAML.load_file("docs/workflow-invariant-coverage.yaml")
rows = workflow.fetch("workflows")
abort "workflow coverage must contain workflows" unless rows.is_a?(Array) && !rows.empty?
rows.each do |row|
  %w[id canonical_source owner evidence_class coverage confidence].each { |field| abort "workflow #{row['id'] || 'unknown'} missing #{field}" if row[field].to_s.empty? }
  abort "workflow source missing: #{row['canonical_source']}" unless File.file?(row["canonical_source"])
end

puts "Data and workflow impact audit passed"
puts "  Flyway migrations: #{migrations.length}"
puts "  ownership domains: #{domains.length}"
puts "  workflow coverage rows: #{rows.length}"
