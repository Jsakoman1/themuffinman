# frozen_string_literal: true

require_relative "adapter"
require_relative "context_search"
require_relative "project_control"
require_relative "workspace_inventory"

module Dora
  class DiscoveryCommands
    def self.search(adapter_path, query, adapter_schema_path:, control_schema_path:)
      adapter, controls = adapter_and_controls(adapter_path, adapter_schema_path, control_schema_path)
      Dir.chdir(adapter.fetch("root")) { ContextSearch.search!(controls.fetch("context_search"), query) }
    end

    def self.inventory(adapter_path, paths, adapter_schema_path:, control_schema_path:)
      _adapter, controls = adapter_and_controls(adapter_path, adapter_schema_path, control_schema_path)
      WorkspaceInventory.classify!(controls.fetch("workspace_inventory"), paths)
    end

    def self.adapter_and_controls(adapter_path, adapter_schema_path, control_schema_path)
      adapter = Adapter.validate!(adapter_path, adapter_schema_path)
      control_path = File.join(File.dirname(File.expand_path(adapter_path)), "project-control.yaml")
      [adapter, ProjectControl.load!(control_path, schema_path: control_schema_path, project_root: adapter.fetch("root"))]
    end
    private_class_method :adapter_and_controls
  end
end
