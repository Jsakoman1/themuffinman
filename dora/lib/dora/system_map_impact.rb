# frozen_string_literal: true

require "yaml"

module Dora
  class SystemMapImpact
    def self.related!(config_path, changed_ids)
      config = YAML.load_file(config_path)
      raise ArgumentError, "system map impact config kind is invalid" unless config["kind"] == "dora_system_map" && config["version"].to_i == 1
      nodes = Array(config["nodes"]); raise ArgumentError, "system map has no nodes" if nodes.empty?
      edges = Array(config["edges"])
      related = edges.select { |edge| changed_ids.include?(edge["from"]) || changed_ids.include?(edge["to"]) }.flat_map { |edge| [edge.fetch("from"), edge.fetch("to")] }.uniq - changed_ids
      {"changed" => changed_ids, "related" => related.sort}
    end
  end
end
