# frozen_string_literal: true

require "yaml"
require_relative "artifact_policy"
require_relative "tool_catalog"

module Dora
  class ControlContracts
    REQUIRED = {
      "tool_catalog" => ["dora_tool_catalog", "commands"],
      "change_routing" => ["dora_change_routing", "rules"],
      "context_search" => ["dora_context_search", "roots"],
      "workspace_inventory" => ["dora_workspace_inventory", "categories"],
      "documentation_evidence" => ["dora_documentation_evidence", "claims"],
      "system_map" => ["dora_system_map", "nodes"],
      "artifact_policy" => ["dora_artifact_policy", "generated_roots"],
      "backlog" => ["dora_backlog", "sources"]
    }.freeze

    def self.validate(controls)
      REQUIRED.map do |id, (kind, required_key)|
        validate_control(id, controls.fetch(id), kind, required_key)
      end
    end

    def self.validate_control(id, path, kind, required_key)
      config = YAML.load_file(path)
      fail!("kind is invalid") unless config["kind"] == kind && config["version"].to_i == 1
      value = config[required_key]
      fail!("#{required_key} is incomplete") unless value.is_a?(Array) && !value.empty?
      ToolCatalog.load!(path) if id == "tool_catalog"
      ArtifactPolicy.load!(path) if id == "artifact_policy"
      {"id" => "control:#{id}", "status" => "passed", "detail" => "#{kind} is configured"}
    rescue ArgumentError, KeyError, Psych::Exception => error
      {"id" => "control:#{id}", "status" => "failed", "detail" => error.message}
    end
    private_class_method :validate_control

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
