# frozen_string_literal: true

require "yaml"

module Dora
  class Adapter
    def self.validate!(path, schema_path)
      schema = YAML.load_file(schema_path)
      adapter = YAML.load_file(path)
      fail!("schema kind is invalid") unless schema["kind"] == "dora_project_adapter_schema" && schema["version"].to_i == 1
      required_fields!(adapter, schema.fetch("adapter_required_fields"), "adapter")
      fail!("adapter kind is invalid") unless adapter["kind"] == "dora_project_adapter"
      fail!("adapter version is invalid") unless adapter["version"].to_i == schema["version"].to_i

      project_schema = schema.fetch("project")
      project = adapter.fetch("project")
      required_fields!(project, project_schema.fetch("required_fields"), "project")
      relative_path!(project.fetch("root"), "project.root")
      project_root = File.expand_path(project.fetch("root"), File.dirname(File.expand_path(path)))
      fail!("project.root does not exist: #{project.fetch("root")}") unless Dir.exist?(project_root)

      paths_schema = schema.fetch("paths")
      paths = adapter.fetch("paths")
      required_fields!(paths, paths_schema.fetch("required_fields"), "paths")
      paths_schema.fetch("required_fields").each { |field| resolve_inside_root!(project_root, paths.fetch(field), "paths.#{field}") }

      commands_schema = schema.fetch("commands")
      commands = adapter.fetch("commands")
      required_fields!(commands, commands_schema.fetch("required_fields"), "commands")

      extensions = adapter.fetch("extensions")
      fail!("extensions must be a non-empty list") unless extensions.is_a?(Array) && !extensions.empty?
      extension_ids = extensions.map do |extension|
        required_fields!(extension, schema.fetch("extensions").fetch("item_required_fields"), "extension")
        category = extension.fetch("category")
        fail!("extension #{extension.fetch("id")} has invalid category") unless schema.fetch("extensions").fetch("allowed_categories").include?(category)
        declared_paths = extension.fetch("paths")
        fail!("extension #{extension.fetch("id")} paths must be a non-empty list") unless declared_paths.is_a?(Array) && !declared_paths.empty?
        declared_paths.each { |value| resolve_inside_root!(project_root, value, "extension #{extension.fetch("id")} path") }
        extension.fetch("id")
      end
      fail!("extension ids are duplicated") unless extension_ids.uniq.length == extension_ids.length

      {
        "project" => project.fetch("id"),
        "root" => project_root,
        "paths" => paths,
        "commands" => commands,
        "extensions" => extensions.length
      }
    end

    def self.required_fields!(value, fields, label)
      fail!("#{label} must be a mapping") unless value.is_a?(Hash)
      missing = fields.reject { |field| value.key?(field) && !value[field].to_s.empty? }
      fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :required_fields!

    def self.relative_path!(value, label)
      fail!("#{label} must be a non-empty relative path") unless value.is_a?(String) && !value.empty? && !value.start_with?("/")
    end
    private_class_method :relative_path!

    def self.resolve_inside_root!(project_root, value, label)
      relative_path!(value, label)
      resolved = File.expand_path(value, project_root)
      fail!("#{label} resolves outside project.root") unless resolved == project_root || resolved.start_with?("#{project_root}/")
      fail!("#{label} does not exist: #{value}") unless File.exist?(resolved)
    end
    private_class_method :resolve_inside_root!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
