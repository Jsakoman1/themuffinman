#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("../..", __dir__)
catalog_path = File.join(ROOT, "docs/dora-muffinman-tool-ownership.yaml")
matrix_path = File.join(ROOT, "docs/dora-muffinman-compatibility-matrix.yaml")
catalog = YAML.load_file(catalog_path)
matrix = YAML.load_file(matrix_path)
failures = []

failures << "ownership catalog kind is invalid" unless catalog["kind"] == "dora_muffinman_tool_ownership" && catalog["version"].to_i == 1
groups = Array(catalog["groups"])
failures << "ownership catalog has no groups" if groups.empty?
allowed = %w[delegate extract product_retained]
declared = groups.flat_map { |group| Array(group["audits"]) }
actual = Dir.glob(File.join(ROOT, "scripts/audits/audit-*.rb")).map { |path| File.basename(path) }.sort
failures << "local audit classification is incomplete" unless declared.sort == actual
failures << "local audit classification has duplicates" unless declared.uniq.length == declared.length

groups.each do |group|
  id = group["id"].to_s
  decision = group["decision"].to_s
  failures << "ownership group #{id} has an invalid decision" unless allowed.include?(decision)
  failures << "ownership group #{id} has no reason" if group["reason"].to_s.empty?
  if %w[delegate extract].include?(decision)
    surfaces = Array(group["dora_surfaces"])
    failures << "reusable ownership group #{id} has no Dora surface" if surfaces.empty?
    surfaces.each { |surface| failures << "Dora surface is missing: #{surface}" unless File.file?(File.join(ROOT, surface)) }
  else
    failures << "product-retained group #{id} has no retention basis" if group["retention_basis"].to_s.empty?
  end
end

static_group = groups.find { |group| group["id"] == "reusable_static_analysis" }
manifest = YAML.load_file(File.join(ROOT, ".dora/plugins.yaml"))
plugins = Array(manifest["plugins"])
Array(static_group && static_group["audits"]).each do |audit|
  entrypoint = "scripts/audits/#{audit}"
  plugin = plugins.find { |candidate| candidate["entrypoint"] == entrypoint }
  failures << "delegated audit has no declared Dora plugin: #{audit}" unless plugin
  next unless plugin

  wrapper = File.read(File.join(ROOT, entrypoint))
  failures << "delegated audit does not route through Dora: #{audit}" unless wrapper.include?("plugin-run") && wrapper.include?(plugin.fetch("id"))
  failures << "delegated audit has no Dora report destination: #{audit}" unless plugin.dig("output", "path").to_s.start_with?("docs/audit-output/")
end

failures << "compatibility matrix kind is invalid" unless matrix["kind"] == "dora_muffinman_compatibility_matrix" && matrix["version"].to_i == 1
failures << "compatibility matrix does not identify the ownership catalog" unless matrix["ownership_catalog"] == "docs/dora-muffinman-tool-ownership.yaml"
failures << "compatibility matrix does not identify this boundary audit" unless matrix["product_boundary_audit"] == "scripts/audits/audit-dora-muffinman-product-boundary.rb"
failures << "compatibility matrix has no retained Dora command entries" if Array(matrix["entries"]).empty?

abort "Dora/MuffinMan product-boundary audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora/MuffinMan product-boundary audit passed (#{actual.length} audits classified; reusable mechanics route to Dora and retained controls keep product authority)."
