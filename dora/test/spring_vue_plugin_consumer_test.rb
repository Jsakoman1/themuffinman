#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_contract"
require_relative "../lib/dora/plugins/spring_configuration_drift"
require_relative "../lib/dora/plugins/spring_mapper_usage"
require_relative "../lib/dora/plugins/http_contract_linker"
require_relative "../lib/dora/plugins/vue_navigation"
require_relative "../lib/dora/plugins/vue_surface_hygiene"
require_relative "../lib/dora/plugins/architecture_integrity"

Dir.mktmpdir("dora-spring-vue-consumer") do |root|
  %w[backend/config backend/src/mapper backend/src/controller backend/src/service backend/dto frontend].each { |path| FileUtils.mkdir_p(File.join(root, path)) }
  File.write(File.join(root, "backend/config/application.properties"), "app.token=${APP_TOKEN}\n")
  File.write(File.join(root, "backend/src/mapper/ItemMapper.java"), "public class ItemMapper { public String toDto() { return \"\"; } }")
  File.write(File.join(root, "backend/src/service/ItemService.java"), "class ItemService { private final ItemMapper mapper; void read() { mapper.toDto(); } }")
  File.write(File.join(root, "backend/dto/ItemDTO.java"), "private String name;")
  File.write(File.join(root, "backend/src/controller/ItemController.java"), "@RequestMapping(\"/items\")\nclass ItemController {\n@GetMapping(\"/{id}\")\npublic String get() { return \"\"; }\n}")
  File.write(File.join(root, "frontend/contracts.ts"), "export interface ItemDTO {\n  name: string;\n}\n")
  File.write(File.join(root, "frontend/api.ts"), "api.get(\"/items/${id}\")")
  File.write(File.join(root, "frontend/router.ts"), "{ path: '/items/:id' }")
  File.write(File.join(root, "frontend/nav.ts"), "items")
  File.write(File.join(root, "frontend/Item.vue"), "<button aria-label=\"Open\">Open</button>")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 1, "plugins" => [{"id" => "spring-vue", "entrypoint" => "dora/plugins", "source_roots" => [{"id" => "backend", "path" => "backend"}, {"id" => "frontend", "path" => "frontend"}], "inputs" => {"static" => true}, "output" => {"kind" => "static-analysis-report"}}]}
  File.write(File.join(root, "plugins.yaml"), YAML.dump(manifest))
  Dora::PluginContract.validate!(File.join(root, "plugins.yaml"))
  abort "configuration plugin failed" unless Dora::Plugins::SpringConfigurationDrift.analyze!(root: root, properties_path: "backend/config/application.properties", property_prefixes: ["app."]).fetch("unmapped").empty?
  abort "mapper plugin failed" unless Dora::Plugins::SpringMapperUsage.analyze!(root: root, mapper_glob: "backend/src/mapper/*.java", source_root: "backend/src").first.fetch("usage_count") == 1
  abort "HTTP plugin failed" unless Dora::Plugins::HttpContractLinker.endpoint_links!(root: root, controller_glob: "backend/src/controller/*Controller.java", client_glob: "frontend/*.ts").first.fetch("client_matches").length == 1
  abort "navigation plugin failed" unless Dora::Plugins::VueNavigation.analyze!(root: root, router_path: "frontend/router.ts", navigation_paths: ["frontend/nav.ts"], required_surfaces: ["items"]).fetch("surfaces").first.fetch("in_router")
  abort "hygiene plugin failed" unless Dora::Plugins::VueSurfaceHygiene.scan!(root: root, source_glob: "frontend/**/*.vue", required_markers: ["aria-label"]).fetch("missing_markers").empty?
  Dora::Plugins::ArchitectureIntegrity.validate_paths!(root: root, paths: ["frontend/router.ts", "backend/src/controller/ItemController.java"])
  content = Dir[File.join(root, "**/*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
  abort "consumer fixture refers to MuffinMan" if content.downcase.include?("muffinman")
end

puts "Dora Spring/Vue plugin consumer test passed (all declared v0.5 static plugins)."
