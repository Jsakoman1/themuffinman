#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/http_contract_linker"

Dir.mktmpdir("dora-http-contract") do |root|
  FileUtils.mkdir_p(File.join(root, "backend/controller")); FileUtils.mkdir_p(File.join(root, "backend/dto")); FileUtils.mkdir_p(File.join(root, "frontend"))
  File.write(File.join(root, "backend/dto/ItemDTO.java"), "private String name;\nprivate String missing;")
  File.write(File.join(root, "frontend/contracts.ts"), "export interface ItemDTO {\n  name: string;\n}\n")
  File.write(File.join(root, "frontend/use.ts"), "item.name")
  File.write(File.join(root, "backend/controller/ItemController.java"), "@RequestMapping(\"/items\")\nclass ItemController {\n@GetMapping(\"/{id}\")\npublic String get() { return \"\"; }\n}")
  File.write(File.join(root, "frontend/api.ts"), "api.get(\"/items/${id}\")")
  drift = Dora::Plugins::HttpContractLinker.dto_drift!(root: root, dto_glob: "backend/dto/*DTO.java", contract_path: "frontend/contracts.ts", frontend_glob: "frontend/**/*.{ts,vue}")
  abort "DTO drift is wrong" unless drift.first.fetch("fields").map { |field| field.fetch("drift_category") }.sort == %w[missing_in_generated_contract used]
  links = Dora::Plugins::HttpContractLinker.endpoint_links!(root: root, controller_glob: "backend/controller/*Controller.java", client_glob: "frontend/*.ts")
  abort "endpoint linking is wrong" unless links.first.fetch("client_matches").length == 1
end

puts "Dora HTTP contract linker plugin test passed (declared DTO, endpoint, and client roots)."
