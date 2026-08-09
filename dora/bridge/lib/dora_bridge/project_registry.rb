# frozen_string_literal: true

require "yaml"

require_relative "../../../lib/dora/project_read_model"

module DoraBridge
  # Trusted server configuration, intentionally separate from client input.
  # A client supplies only a public ID; it can never supply a project root.
  class ProjectRegistry
    PROJECT_ID = /\A[a-z][a-z0-9-]*\z/.freeze

    def self.load!(config_path)
      document = YAML.load_file(config_path)
      fail!("bridge project registry is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_bridge_projects" && document["version"].to_i == 1 && document["projects"].is_a?(Array)

      entries = document.fetch("projects").each_with_object({}) do |entry, registered|
        fail!("bridge project registry entry is invalid") unless entry.is_a?(Hash) && entry["id"].is_a?(String) && entry["id"].match?(PROJECT_ID) && entry["adapter_path"].is_a?(String) && !entry["adapter_path"].empty?
        fail!("bridge project registry has duplicate project ID") if registered.key?(entry.fetch("id"))

        registered[entry.fetch("id")] = entry.slice("id", "name", "adapter_path")
      end
      new(entries, File.dirname(File.expand_path(config_path)))
    rescue Psych::Exception
      fail!("bridge project registry is invalid")
    end

    def initialize(entries, config_root)
      @entries = entries.freeze
      @config_root = config_root
    end

    def list
      @entries.values.map { |entry| entry.slice("id", "name").compact }.sort_by { |entry| entry.fetch("id") }
    end

    def read_model!(project_id)
      entry = @entries[project_id]
      fail!("unknown or unallowed bridge project") unless entry

      Dora::ProjectReadModel.load!(adapter_path: File.expand_path(entry.fetch("adapter_path"), @config_root))
    end

    def self.fail!(message)
      raise ArgumentError, message
    end

    private_class_method :fail!

    private

    def fail!(message)
      self.class.send(:fail!, message)
    end
  end
end
