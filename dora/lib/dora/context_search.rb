# frozen_string_literal: true

require "open3"
require "yaml"

module Dora
  class ContextSearch
    def self.search!(config_path, query)
      config = YAML.load_file(config_path)
      raise ArgumentError, "context search config kind is invalid" unless config["kind"] == "dora_context_search" && config["version"].to_i == 1
      roots = Array(config["roots"]); exclusions = Array(config["exclusions"])
      raise ArgumentError, "context search config has no roots" if roots.empty?
      command = ["rg", "-l", "-i", "--fixed-strings", query]
      exclusions.each { |path| command.concat(["--glob", "!#{path}"]) }
      command.concat(roots)
      stdout, _stderr, status = Open3.capture3(*command)
      return [] if status.exitstatus == 1
      raise ArgumentError, "context search failed" unless status.success?
      stdout.lines.map(&:strip).reject(&:empty?).sort
    end
  end
end
