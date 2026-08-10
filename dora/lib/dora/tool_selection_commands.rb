# frozen_string_literal: true

require_relative "adapter"
require_relative "change_routing"
require_relative "project_control"
require_relative "tool_catalog"

module Dora
  class ToolSelectionCommands
    def self.catalog(adapter_path, adapter_schema_path:, control_schema_path:)
      controls = controls_for(adapter_path, adapter_schema_path, control_schema_path)
      ToolCatalog.help_lines(controls.fetch("tool_catalog"))
    end

    def self.route(adapter_path, changed_paths, adapter_schema_path:, control_schema_path:)
      controls = controls_for(adapter_path, adapter_schema_path, control_schema_path)
      ChangeRouting.route!(controls.fetch("change_routing"), changed_paths, catalog_path: controls.fetch("tool_catalog"))
    end

    def self.controls_for(adapter_path, adapter_schema_path, control_schema_path)
      adapter = Adapter.validate!(adapter_path, adapter_schema_path)
      control_path = File.join(File.dirname(File.expand_path(adapter_path)), "project-control.yaml")
      ProjectControl.load!(control_path, schema_path: control_schema_path, project_root: adapter.fetch("root"))
    end
    private_class_method :controls_for
  end
end
