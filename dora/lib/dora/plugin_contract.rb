# frozen_string_literal: true

require "yaml"

module Dora
  class PluginContract
    def self.validate!(manifest_path)
      manifest = YAML.load_file(manifest_path)
      fail!("plugin manifest kind is invalid") unless manifest.is_a?(Hash) && manifest["kind"] == "dora_plugin_manifest" && manifest["version"].to_i == 1
      plugins = Array(manifest["plugins"])
      fail!("plugin manifest has no plugins") if plugins.empty?
      ids = plugins.map { |plugin| plugin["id"] }
      fail!("plugin ids must be unique and non-empty") if ids.any? { |id| id.to_s.strip.empty? } || ids.uniq.length != ids.length

      plugins.each do |plugin|
        %w[id source_roots inputs output].each { |field| fail!("plugin #{plugin["id"] || "<unknown>"} is missing #{field}") unless present?(plugin[field]) }
        entrypoint = plugin["entrypoint"]
        builtin = plugin["builtin"]
        fail!("plugin #{plugin["id"]} must declare exactly one of entrypoint or builtin") unless present?(entrypoint) ^ present?(builtin)
        fail!("plugin #{plugin["id"]} entrypoint is invalid") if present?(entrypoint) && !safe_relative_path?(entrypoint)
        fail!("plugin #{plugin["id"]} builtin is invalid") if present?(builtin) && !builtin.match?(/\A[a-z][a-z0-9-]*\z/)
        fail!("plugin #{plugin["id"]} source_roots must be a list") unless plugin["source_roots"].is_a?(Array)
        plugin["source_roots"].each do |root|
          fail!("plugin #{plugin["id"]} source root is invalid") unless root.is_a?(Hash) && root["id"].to_s.match?(/\A[a-z0-9][a-z0-9_-]*\z/) && safe_relative_path?(root["path"])
        end
        fail!("plugin #{plugin["id"]} inputs must be a mapping") unless plugin["inputs"].is_a?(Hash)
        fail!("plugin #{plugin["id"]} output must be a mapping") unless plugin["output"].is_a?(Hash) && plugin["output"]["kind"].to_s.match?(/\A[a-z0-9][a-z0-9_-]*\z/)
      end

      {"plugins" => ids}
    rescue Psych::Exception => error
      fail!("plugin manifest YAML is invalid: #{error.message}")
    end

    def self.present?(value)
      value.is_a?(Array) || value.is_a?(Hash) ? !value.empty? : !value.to_s.strip.empty?
    end
    private_class_method :present?

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
