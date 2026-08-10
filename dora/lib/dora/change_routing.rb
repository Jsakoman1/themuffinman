# frozen_string_literal: true

require "yaml"
require "time"
require_relative "tool_catalog"

module Dora
  class ChangeRouting
    def self.route!(config_path, paths, catalog_path: nil)
      config = YAML.load_file(config_path)
      raise ArgumentError, "change routing config kind is invalid" unless config["kind"] == "dora_change_routing" && config["version"].to_i == 1
      rules = Array(config["rules"])
      raise ArgumentError, "change routing config has no rules" if rules.empty?
      changed_paths = Array(paths).uniq.sort
      raise ArgumentError, "changed paths must be a non-empty project-relative list" if changed_paths.empty? || changed_paths.any? { |path| !safe_relative_path?(path) }

      catalog = catalog_path ? ToolCatalog.load!(catalog_path).to_h { |command| [command.fetch("id"), command] } : {}
      path_routes = changed_paths.map do |path|
        matches = rules.select { |rule| Array(rule["path_prefixes"]).any? { |prefix| path.start_with?(prefix) } }
        tool_ids = matches.flat_map { |rule| Array(rule["tool_ids"]) }.uniq
        unknown = tool_ids.reject { |id| catalog.key?(id) }
        raise ArgumentError, "change routing references unknown tool ids: #{unknown.join(", ")}" unless unknown.empty?
        commands = matches.flat_map { |rule| Array(rule.fetch("commands", [])) } + tool_ids.map { |id| catalog.fetch(id).fetch("target") }
        {"path" => path, "rule_ids" => matches.map { |rule| rule.fetch("id") }, "tool_ids" => tool_ids, "commands" => commands.uniq}
      end
      matched = path_routes.reject { |route| route.fetch("rule_ids").empty? }
      {"kind" => "dora_change_routing_result", "version" => 1, "observed_at" => Time.now.utc.iso8601, "read_only" => true, "disposition" => "advisory", "changed_paths" => changed_paths, "path_routes" => path_routes, "classifications" => matched.flat_map { |route| route.fetch("rule_ids") }.uniq, "tool_ids" => matched.flat_map { |route| route.fetch("tool_ids") }.uniq, "commands" => matched.flat_map { |route| route.fetch("commands") }.uniq, "unmatched_paths" => path_routes.select { |route| route.fetch("rule_ids").empty? }.map { |route| route.fetch("path") }, "completion_boundary" => "Routing recommends declared leaf validations only; it does not execute commands, select work, or record verification."}.freeze
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?
  end
end
