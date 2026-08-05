# frozen_string_literal: true

require "open3"
require "yaml"
require_relative "plugin_contract"

module Dora
  class PluginRunner
    def self.run!(manifest_path, plugin_id:, project_root: Dir.pwd)
      PluginContract.validate!(manifest_path)
      manifest = YAML.load_file(manifest_path)
      plugin = Array(manifest["plugins"]).find { |candidate| candidate["id"] == plugin_id }
      fail!("plugin is not declared: #{plugin_id}") unless plugin

      root = File.expand_path(project_root)
      entrypoint = plugin.fetch("entrypoint")
      fail!("plugin entrypoint is invalid") unless safe_relative_path?(entrypoint)
      path = File.expand_path(entrypoint, root)
      fail!("plugin entrypoint is missing") unless path.start_with?("#{root}/") && File.file?(path)

      output, status = Open3.capture2e({"DORA_PLUGIN_RUNNER" => "1"}, "ruby", path, chdir: root)
      fail!("plugin failed: #{plugin_id}\n#{output}") unless status.success?
      {"id" => plugin_id, "entrypoint" => entrypoint, "source_roots" => plugin.fetch("source_roots"), "output" => output}
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
