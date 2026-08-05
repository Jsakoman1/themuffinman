# frozen_string_literal: true

require "yaml"

module Dora
  class ChangeRouting
    def self.route!(config_path, paths)
      config = YAML.load_file(config_path)
      raise ArgumentError, "change routing config kind is invalid" unless config["kind"] == "dora_change_routing" && config["version"].to_i == 1
      rules = Array(config["rules"])
      raise ArgumentError, "change routing config has no rules" if rules.empty?
      matches = rules.select { |rule| Array(rule["path_prefixes"]).any? { |prefix| paths.any? { |path| path.start_with?(prefix) } } }
      {"classifications" => matches.map { |rule| rule.fetch("id") }, "commands" => matches.flat_map { |rule| Array(rule.fetch("commands")) }.uniq}
    end
  end
end
