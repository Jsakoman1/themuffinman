#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("../..", __dir__)
require File.join(ROOT, "dora/lib/dora/control_ownership")
INVENTORY_PATH = File.join(ROOT, "docs/dora-control-ownership-inventory.yaml")

def relative(path)
  path.delete_prefix("#{ROOT}/")
end

def discovered_scripts
  paths = Dir[
    File.join(ROOT, "scripts/**/*.rb"),
    File.join(ROOT, "scripts/*.java"),
    File.join(ROOT, "apps/themuffinman/frontend/scripts/*.mjs"),
    File.join(ROOT, "apps/themuffinman/frontend/scripts/*.js")
  ].select { |path| File.file?(path) }.sort
  paths.map { |path| "script:#{relative(path)}" }
end

def discovered_make_targets
  File.readlines(File.join(ROOT, "Makefile"), chomp: true).each_with_object([]) do |line, targets|
    targets << "make:#{line.split(":", 2).first}" if line.match?(/\A[-A-Za-z0-9_]+:/)
  end
end

inventory = YAML.load_file(INVENTORY_PATH)
abort "Dora ownership inventory kind is invalid" unless inventory["kind"] == "dora_control_ownership_inventory"
abort "Dora ownership inventory version is invalid" unless inventory["version"].to_i == 1

failures = []

scripts = discovered_scripts
targets = discovered_make_targets
expected = inventory.fetch("baseline_counts")
failures << "script inventory changed: expected #{expected["scripts"]}, found #{scripts.length}" unless expected["scripts"].to_i == scripts.length
failures << "Make target inventory changed: expected #{expected["make_targets"]}, found #{targets.length}" unless expected["make_targets"].to_i == targets.length

bindings = Array(inventory["consumer_adapter_bindings"])
failures << "consumer adapter bindings are missing" if bindings.empty?
bindings.each do |binding|
  %w[project adapter dora_entrypoint rule].each do |field|
    failures << "consumer adapter binding is missing #{field}" if binding[field].to_s.empty?
  end
  adapter_path = binding["adapter"].to_s
  entrypoint_path = binding["dora_entrypoint"].to_s
  failures << "consumer adapter is missing: #{adapter_path}" unless adapter_path.empty? || File.file?(File.join(ROOT, adapter_path))
  failures << "Dora entrypoint is missing: #{entrypoint_path}" unless entrypoint_path.empty? || File.file?(File.join(ROOT, entrypoint_path))
  next unless File.file?(File.join(ROOT, adapter_path))

  adapter = YAML.load_file(File.join(ROOT, adapter_path))
  failures << "consumer adapter kind is invalid: #{adapter_path}" unless adapter["kind"] == "dora_project_adapter"
  failures << "consumer adapter project id does not match binding: #{adapter_path}" unless adapter.dig("project", "id") == binding["project"]
  extension_ids = Array(adapter["extensions"]).map { |extension| extension["id"] }
  required_extension_ids = %w[muffinman-java-spring-source-map muffinman-typescript-vue-source-map muffinman-runtime-harness]
  missing_extensions = required_extension_ids - extension_ids
  failures << "consumer adapter is missing declared product extensions: #{missing_extensions.join(", ")}" unless missing_extensions.empty?
end

subjects = scripts + targets
begin
  classifications = Dora::ControlOwnership.classify!(inventory, subjects)
rescue ArgumentError => error
  failures << error.message
  classifications = []
end

missing_owners = %w[dora_core dora_adapter muffinman_extension] - classifications.map { |row| row["owner"] }.uniq
failures << "ownership classes have no current subject: #{missing_owners.join(", ")}" unless missing_owners.empty?

core_subjects = classifications.select { |row| row["owner"] == "dora_core" }.map { |row| row["subject"] }
core_subjects.each do |subject|
  path = subject.delete_prefix("script:")
  next unless subject.start_with?("script:")

  content = File.read(File.join(ROOT, path))
  failures << "Dora core subject names MuffinMan path: #{path}" if content.include?("apps/themuffinman")
end

abort "Dora control ownership audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
summary = classifications.group_by { |row| row["owner"] }.transform_values(&:length)
puts "Dora control ownership audit passed (#{subjects.length} subjects; #{summary.sort.map { |owner, count| "#{owner}=#{count}" }.join(", ")})."
