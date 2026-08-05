# frozen_string_literal: true

require "yaml"

module Dora
  class ToolCatalog
    REQUIRED_FIELDS = %w[id target purpose preconditions expected_cost].freeze

    def self.load!(path)
      catalog = YAML.load_file(path)
      raise ArgumentError, "tool catalog kind is invalid" unless catalog["kind"] == "dora_tool_catalog" && catalog["version"].to_i == 1

      commands = Array(catalog["commands"])
      raise ArgumentError, "tool catalog has no commands" if commands.empty?
      commands.each do |command|
        missing = REQUIRED_FIELDS.reject { |field| command[field].is_a?(String) || command[field].is_a?(Array) }
        raise ArgumentError, "tool catalog command is missing #{missing.join(", ")}" unless missing.empty?
      end
      ids = commands.map { |command| command.fetch("id") }
      targets = commands.map { |command| command.fetch("target") }
      raise ArgumentError, "tool catalog command ids are duplicated" unless ids.uniq.length == ids.length
      raise ArgumentError, "tool catalog command targets are duplicated" unless targets.uniq.length == targets.length

      commands.map { |command| command.dup.freeze }.freeze
    end

    def self.help_lines(path)
      load!(path).map { |command| "#{command.fetch("target")} [#{command.fetch("expected_cost")}] — #{command.fetch("purpose")}" }
    end
  end
end
