#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/plan_coverage"

DORA_BIN = File.expand_path("../bin/dora", __dir__)

def write(root, relative, value)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(value))
  path
end

Dir.mktmpdir("dora-plan-coverage") do |root|
  alpha = File.join(root, "alpha")
  beta = File.join(root, "beta")
  write(alpha, "docs/work/current.yaml", {"kind" => "work", "version" => 1, "status" => "active"})
  write(beta, "delivery.yaml", {"kind" => "master", "version" => 1, "status" => "planned"})
  alpha_registry = write(alpha, "plans.yaml", {"kind" => "dora_plan_registry", "version" => 1, "plans" => [{"id" => "alpha-delivery", "path" => "docs/work/current.yaml", "allowed_statuses" => ["active"]}]})
  beta_registry = write(beta, "plans.yaml", {"kind" => "dora_plan_registry", "version" => 1, "plans" => [{"id" => "beta-delivery", "path" => "delivery.yaml", "allowed_statuses" => ["planned"]}]})

  abort "alpha plan coverage is wrong" unless Dora::PlanCoverage.review!(alpha_registry).map { |row| row["id"] } == ["alpha-delivery"]
  abort "beta plan coverage is wrong" unless Dora::PlanCoverage.review!(beta_registry).map { |row| row["status"] } == ["planned"]

  stdout, stderr, status = Open3.capture3(DORA_BIN, "plan-coverage", beta_registry)
  abort "CLI did not review declared registry: #{stderr}" unless status.success? && stdout.include?("beta-delivery")

  invalid = {"kind" => "dora_plan_registry", "version" => 1, "plans" => [{"id" => "outside", "path" => "../outside.yaml", "allowed_statuses" => ["active"]}]}
  File.write(beta_registry, YAML.dump(invalid))
  begin
    Dora::PlanCoverage.review!(beta_registry)
    abort "unsafe registry path passed"
  rescue ArgumentError => error
    abort "wrong unsafe registry failure" unless error.message.include?("invalid path")
  end
end

puts "Dora plan coverage test passed (two declared project registries)."
