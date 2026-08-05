# frozen_string_literal: true

require_relative "adapter"
require_relative "documentation_evidence"
require_relative "project_control"
require_relative "system_map_impact"

module Dora
  class AnalysisCommands
    def self.evidence(adapter_path, adapter_schema_path:, control_schema_path:)
      adapter, controls = adapter_and_controls(adapter_path, adapter_schema_path, control_schema_path)
      Dir.chdir(adapter.fetch("root")) { DocumentationEvidence.review!(controls.fetch("documentation_evidence")) }
    end

    def self.impact(adapter_path, changed_ids, adapter_schema_path:, control_schema_path:)
      _adapter, controls = adapter_and_controls(adapter_path, adapter_schema_path, control_schema_path)
      SystemMapImpact.related!(controls.fetch("system_map"), changed_ids)
    end

    def self.adapter_and_controls(adapter_path, adapter_schema_path, control_schema_path)
      adapter = Adapter.validate!(adapter_path, adapter_schema_path)
      control_path = File.join(File.dirname(File.expand_path(adapter_path)), "project-control.yaml")
      [adapter, ProjectControl.load!(control_path, schema_path: control_schema_path, project_root: adapter.fetch("root"))]
    end
    private_class_method :adapter_and_controls
  end
end
