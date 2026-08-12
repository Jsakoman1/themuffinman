# frozen_string_literal: true

require "yaml"

module Dora
  class ProjectControl
    def self.load!(path, schema_path:, project_root:)
      schema = YAML.load_file(schema_path)
      control = YAML.load_file(path)
      fail!("project control schema kind is invalid") unless schema["kind"] == "dora_project_control_schema" && schema["version"].to_i == 1
      required = schema.fetch("required_fields")
      missing = required.reject { |field| control.key?(field) && !control[field].to_s.empty? }
      fail!("project control is missing #{missing.join(", ")}") unless missing.empty?
      fail!("project control kind is invalid") unless control["kind"] == "dora_project_control" && control["version"].to_i == schema["version"].to_i

      controls = control.fetch("controls")
      fail!("project control controls must be a mapping") unless controls.is_a?(Hash)
      required_keys = schema.fetch("controls").fetch("required_keys")
      missing_keys = required_keys.reject { |key| controls[key].is_a?(String) && !controls[key].empty? }
      fail!("project control controls are missing #{missing_keys.join(", ")}") unless missing_keys.empty?

      optional_keys = Array(schema.fetch("controls").fetch("optional_keys", []))
      selected_keys = required_keys + optional_keys.select { |key| controls[key].is_a?(String) && !controls[key].empty? }
      resolved = controls.slice(*selected_keys).transform_values do |relative|
        fail!("project control path must be relative: #{relative}") if relative.start_with?("/")
        absolute = File.expand_path(relative, project_root)
        fail!("project control path resolves outside project root: #{relative}") unless absolute.start_with?("#{project_root}/")
        fail!("project control file does not exist: #{relative}") unless File.file?(absolute)
        absolute
      end
      resolved.freeze
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
