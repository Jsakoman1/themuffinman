# frozen_string_literal: true

require "yaml"

module Dora
  class BacklogTodo
    TODO_PATTERN = /(?:TODO|FIXME)\(([A-Z][A-Z0-9_-]{2,})\)/.freeze

    def self.check!(config_path, source_files:)
      config = YAML.load_file(config_path)
      raise ArgumentError, "backlog config kind is invalid" unless config["kind"] == "dora_backlog" && config["version"].to_i == 1
      backlog_files = Array(config["sources"])
      raise ArgumentError, "backlog config has no sources" if backlog_files.empty?
      backlog_ids = backlog_files.flat_map { |path| File.read(path).scan(/\b[A-Z][A-Z0-9_-]{2,}\b/) }.uniq
      todo_ids = source_files.flat_map { |path| File.read(path).scan(TODO_PATTERN).flatten }.uniq
      missing = todo_ids - backlog_ids
      raise ArgumentError, "TODO ids missing from declared backlog sources: #{missing.join(", ")}" unless missing.empty?

      {"todo_ids" => todo_ids, "backlog_sources" => backlog_files}
    end
  end
end
