# frozen_string_literal: true

require "yaml"
require_relative "plugins/java_spring"
require_relative "plugins/typescript_vue"

module Dora
  class RepositoryMap
    PLUGINS = {"java_spring" => Plugins::JavaSpring, "typescript_vue" => Plugins::TypeScriptVue}.freeze

    def self.emit!(config_path, project_root:)
      config = YAML.load_file(config_path)
      fail!("repository map config kind is invalid") unless config["kind"] == "dora_repository_map" && config["version"].to_i == 1
      roots = Array(config["source_roots"])
      fail!("repository map has no source roots") if roots.empty?
      sources = roots.flat_map { |source| discover_source(source, project_root) }
      {"sources" => sources.sort_by { |source| source.fetch("path") }, "relationships" => Array(config["relationships"])}
    end

    def self.discover_source(source, project_root)
      id = source.fetch("id")
      relative = source.fetch("path")
      fail!("source root path must be relative") unless relative.is_a?(String) && !relative.empty? && !relative.start_with?("/") && !relative.split("/").include?("..")
      root = File.join(project_root, relative)
      fail!("declared source root does not exist: #{relative}") unless Dir.exist?(root)
      Array(source.fetch("plugins")).flat_map do |plugin_id|
        plugin = PLUGINS[plugin_id]
        fail!("unsupported repository-map plugin: #{plugin_id}") unless plugin
        plugin.discover(root).map { |row| row.merge("path" => File.join(relative, row.fetch("path")), "source_root" => id, "plugin" => plugin_id) }
      end
    end
    private_class_method :discover_source

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
