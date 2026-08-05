# frozen_string_literal: true

require "yaml"
require_relative "plugin_contract"

module Dora
  class PluginDiscovery
    def self.catalog!(manifest_path: nil, adapter_path: File.expand_path("../../plugin-adapter.schema.yaml", __dir__))
      adapter = YAML.load_file(adapter_path)
      fail!("plugin adapter schema is invalid") unless adapter["id"] == "plugin-adapter" && adapter["version"].to_i == 1 && adapter["supported_ids"].is_a?(Array)
      result = {"kind" => "dora_plugin_catalog", "version" => 1, "builtins" => adapter.fetch("supported_ids").sort}
      return result.freeze unless manifest_path

      PluginContract.validate!(manifest_path)
      manifest = YAML.load_file(manifest_path)
      result["declared_plugins"] = Array(manifest.fetch("plugins")).map do |plugin|
        {"id" => plugin.fetch("id"), "execution" => plugin["builtin"] ? {"builtin" => plugin.fetch("builtin")} : {"entrypoint" => plugin.fetch("entrypoint")}, "source_roots" => plugin.fetch("source_roots"), "inputs" => plugin.fetch("inputs"), "output" => plugin.fetch("output")}
      end
      result.freeze
    rescue Psych::Exception => error
      fail!("plugin discovery YAML is invalid: #{error.message}")
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
