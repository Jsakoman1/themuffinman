#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

fixture = YAML.load_file(File.expand_path("../fixtures/source-analysis/http-contract.yaml", __dir__))
Dir.mktmpdir("dora-http-builtin") do |root|
  %w[backend/controller backend/dto frontend].each { |path| FileUtils.mkdir_p(File.join(root, path)) }
  File.write(File.join(root, "backend/dto/ItemDTO.java"), "private String name;\n")
  File.write(File.join(root, "backend/controller/ItemController.java"), "@RequestMapping(\"/items\")\nclass ItemController {\n@GetMapping(\"/{id}\")\npublic String get() { return \"\"; }\n}")
  File.write(File.join(root, "frontend/contracts.ts"), "export interface ItemDTO {\n  name: string;\n}\n")
  File.write(File.join(root, "frontend/api.ts"), "api.get(\"/items/${id}\")")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "http-contract", "builtin" => "http-contract-linker", "source_roots" => fixture.fetch("source_roots"), "inputs" => fixture.fetch("inputs"), "output" => fixture.fetch("output")}]} 
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))

  report = Dora::PluginRunner.run!(manifest_path, plugin_id: "http-contract", project_root: root)
  finding = report.fetch("findings").first
  abort "HTTP built-in did not link declared endpoint and client" unless finding.fetch("endpoint_links").first.fetch("client_matches").length == 1
  abort "HTTP built-in did not inspect declared DTO contract" unless finding.fetch("dto_drift").first.fetch("generated_contract_present")
  abort "HTTP built-in report is missing" unless File.file?(File.join(root, "reports", "http-contract.json"))
end

puts "Dora HTTP built-in plugin test passed (declared DTO, controller, and client contract)."
