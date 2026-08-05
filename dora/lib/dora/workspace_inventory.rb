# frozen_string_literal: true

require "yaml"

module Dora
  class WorkspaceInventory
    def self.classify!(config_path, paths)
      config = YAML.load_file(config_path)
      raise ArgumentError, "workspace inventory config kind is invalid" unless config["kind"] == "dora_workspace_inventory" && config["version"].to_i == 1
      categories = Array(config["categories"])
      raise ArgumentError, "workspace inventory config has no categories" if categories.empty?
      paths.to_h do |path|
        category = categories.find { |entry| Array(entry["path_prefixes"]).any? { |prefix| path.start_with?(prefix) } }
        [path, category ? category.fetch("id") : "other"]
      end
    end
  end
end
