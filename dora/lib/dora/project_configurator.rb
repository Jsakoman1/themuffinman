# frozen_string_literal: true

require "yaml"
require_relative "adapter"
require_relative "control_contracts"
require_relative "project_control"

module Dora
  class ProjectConfigurator
    def self.apply!(adapter_path, control_id:, source_path:, adapter_schema_path:, control_schema_path:)
      adapter = Adapter.validate!(adapter_path, adapter_schema_path)
      controls = ProjectControl.load!(File.join(File.dirname(File.expand_path(adapter_path)), "project-control.yaml"), schema_path: control_schema_path, project_root: adapter.fetch("root"))
      expected = ControlContracts::REQUIRED[control_id]
      fail!("unknown Dora control: #{control_id}") unless expected
      fail!("control source does not exist: #{source_path}") unless File.file?(source_path)
      config = YAML.load_file(source_path)
      fail!("control source kind is invalid for #{control_id}") unless config["kind"] == expected.first && config["version"].to_i == 1
      File.write(controls.fetch(control_id), YAML.dump(config).sub(/\A---\n/, ""))
      controls.fetch(control_id)
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
