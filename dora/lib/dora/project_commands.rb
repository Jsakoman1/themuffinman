# frozen_string_literal: true

require "yaml"

module Dora
  class ProjectCommands
    REQUIRED = %w[setup test build].freeze

    def self.load!(path)
      config = YAML.load_file(path)
      fail!("project commands kind is invalid") unless config["kind"] == "dora_project_commands" && config["version"].to_i == 1
      commands = config["commands"]
      fail!("project commands must be a mapping") unless commands.is_a?(Hash)
      missing = REQUIRED.reject { |id| commands[id].is_a?(String) && !commands[id].empty? }
      fail!("project commands are missing #{missing.join(", ")}") unless missing.empty?
      fail!("project commands must not invoke Dora recursively") if commands.values.any? { |command| command.match?(/\bdora\b/) }
      commands.slice(*REQUIRED)
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
