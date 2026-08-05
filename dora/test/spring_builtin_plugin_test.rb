#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

Dir.mktmpdir("dora-spring-builtin") do |root|
  %w[backend/config backend/src/mapper backend/src/service].each { |path| FileUtils.mkdir_p(File.join(root, path)) }
  File.write(File.join(root, "backend/config/application.properties"), "app.token=${APP_TOKEN}\n")
  File.write(File.join(root, "backend/src/mapper/ItemMapper.java"), "public class ItemMapper { public String toDto() { return \"\"; } }")
  File.write(File.join(root, "backend/src/service/ItemService.java"), "class ItemService { private final ItemMapper mapper; void read() { mapper.toDto(); } }")
  manifest = {
    "kind" => "dora_plugin_manifest", "version" => 1,
    "plugins" => [
      {"id" => "configuration", "builtin" => "spring-configuration-drift", "source_roots" => [{"id" => "config", "path" => "backend/config"}], "inputs" => {"properties_path" => "backend/config/application.properties", "property_prefixes" => ["app."]}, "output" => {"kind" => "static-analysis-report", "path" => "reports/configuration.json"}},
      {"id" => "mappers", "builtin" => "spring-mapper-usage", "source_roots" => [{"id" => "backend", "path" => "backend/src"}], "inputs" => {"mapper_glob" => "backend/src/mapper/*.java", "source_root" => "backend/src"}, "output" => {"kind" => "static-analysis-report", "path" => "reports/mappers.json"}}
    ]
  }
  manifest_path = File.join(root, "plugins.yaml")
  File.write(manifest_path, YAML.dump(manifest))

  configuration = Dora::PluginRunner.run!(manifest_path, plugin_id: "configuration", project_root: root)
  abort "Spring configuration built-in reported mapped property as drift" unless configuration.fetch("findings").first.fetch("unmapped").empty?
  mappers = Dora::PluginRunner.run!(manifest_path, plugin_id: "mappers", project_root: root)
  abort "Spring mapper built-in did not report mapper use" unless mappers.fetch("findings").first.fetch("usage_count") == 1
  abort "Spring built-in reports are missing" unless %w[configuration.json mappers.json].all? { |name| File.file?(File.join(root, "reports", name)) }
end

puts "Dora Spring built-in plugin test passed (declared configuration and mapper analysis)."
