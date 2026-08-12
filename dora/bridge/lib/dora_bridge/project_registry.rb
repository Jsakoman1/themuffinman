# frozen_string_literal: true

require "yaml"

require_relative "../../../lib/dora/idc_envelope"
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
        capabilities = entry["capabilities"] || {}
        fail!("bridge project registry capabilities are invalid") unless capabilities.is_a?(Hash) && capabilities.keys.all? { |key| key == "handoff_write" } && (!capabilities.key?("handoff_write") || [true, false].include?(capabilities["handoff_write"]))

        registered[entry.fetch("id")] = entry.slice("id", "name", "adapter_path").merge("capabilities" => {"handoff_write" => capabilities.fetch("handoff_write", false)}.freeze)
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
      Dora::ProjectReadModel.load!(adapter_path: adapter_path!(project_id))
    end

    # The registry resolves a configured project ID to its private adapter path.
    # Callers never receive that path and cannot choose a different project root.
    def idc_envelope!(project_id, selection:)
      Dora::IdcEnvelope.export!(adapter_path: adapter_path!(project_id), selection: selection)
    end

    # This is intentionally independent from read_model!: a readable project is
    # not a writable handoff target unless its owner has opted in explicitly.
    def handoff_authorized!(project_id)
      entry = @entries[project_id]
      fail!("unknown or unallowed bridge project") unless entry
      fail!("bridge handoff write is disabled for this project") unless entry.dig("capabilities", "handoff_write") == true

      entry.slice("id", "name").compact.freeze
    end

    def self.fail!(message)
      raise ArgumentError, message
    end

    private_class_method :fail!

    private

    def adapter_path!(project_id)
      entry = @entries[project_id]
      fail!("unknown or unallowed bridge project") unless entry

      File.expand_path(entry.fetch("adapter_path"), @config_root)
    end

    def fail!(message)
      self.class.send(:fail!, message)
    end
  end
end
