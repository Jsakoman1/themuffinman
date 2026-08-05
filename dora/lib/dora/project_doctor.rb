# frozen_string_literal: true

require "shellwords"
require "yaml"
require_relative "adapter"
require_relative "control_contracts"
require_relative "project_control"

module Dora
  class ProjectDoctor
    def self.run(adapter_path, schema_path:, control_schema_path:)
      checks = []
      adapter = YAML.load_file(adapter_path)
      project_root = project_root(adapter_path, adapter, checks)
      validate_adapter(adapter_path, schema_path, checks)
      check_declared_paths(adapter, project_root, checks)
      check_declared_commands(adapter, project_root, checks)
      check_control_bundle(adapter_path, project_root, control_schema_path, checks)
      {"healthy" => checks.all? { |check| check.fetch("status") == "passed" }, "checks" => checks}
    rescue Psych::Exception => error
      {"healthy" => false, "checks" => [failed("adapter-file", "cannot read adapter: #{error.message}")]}
    end

    def self.project_root(adapter_path, adapter, checks)
      root = adapter.dig("project", "root")
      unless root.is_a?(String) && !root.empty? && !root.start_with?("/")
        checks << failed("project-root", "project.root must be a non-empty relative path")
        return nil
      end
      resolved = File.expand_path(root, File.dirname(File.expand_path(adapter_path)))
      unless Dir.exist?(resolved)
        checks << failed("project-root", "declared project root is missing: #{root}")
        return nil
      end
      checks << passed("project-root", "declared project root exists")
      resolved
    end
    private_class_method :project_root

    def self.validate_adapter(adapter_path, schema_path, checks)
      Adapter.validate!(adapter_path, schema_path)
      checks << passed("adapter", "adapter satisfies the Dora schema")
    rescue ArgumentError => error
      checks << failed("adapter", error.message)
    end
    private_class_method :validate_adapter

    def self.check_declared_paths(adapter, project_root, checks)
      paths = adapter["paths"]
      unless paths.is_a?(Hash) && project_root
        checks << failed("declared-paths", "declared paths cannot be checked")
        return
      end
      paths.each do |id, relative|
        absolute = File.expand_path(relative.to_s, project_root)
        if relative.is_a?(String) && !relative.start_with?("/") && (absolute == project_root || absolute.start_with?("#{project_root}/")) && File.exist?(absolute)
          checks << passed("path:#{id}", relative)
        else
          checks << failed("path:#{id}", "missing or invalid declared path: #{relative}")
        end
      end
    end
    private_class_method :check_declared_paths

    def self.check_declared_commands(adapter, project_root, checks)
      commands = adapter["commands"]
      unless commands.is_a?(Hash)
        checks << failed("declared-commands", "commands must be a mapping")
        return
      end
      commands.each do |id, command|
        executable = executable_for(command)
        if executable && executable_available?(executable, project_root)
          checks << passed("command:#{id}", "#{executable} is available")
        else
          checks << failed("command:#{id}", "required executable is unavailable: #{executable || "invalid command"}")
        end
      end
    end
    private_class_method :check_declared_commands

    def self.check_control_bundle(adapter_path, project_root, schema_path, checks)
      control_path = File.join(File.dirname(File.expand_path(adapter_path)), "project-control.yaml")
      unless project_root && File.file?(control_path)
        checks << failed("project-control", "control bundle is missing: #{control_path}")
        return
      end
      controls = ProjectControl.load!(control_path, schema_path: schema_path, project_root: project_root)
      checks << passed("project-control", "control bundle and declared control files exist")
      checks.concat(ControlContracts.validate(controls))
    rescue ArgumentError => error
      checks << failed("project-control", error.message)
    end
    private_class_method :check_control_bundle

    def self.executable_for(command)
      return nil unless command.is_a?(String) && !command.empty?

      Shellwords.split(command).first
    rescue ArgumentError
      nil
    end
    private_class_method :executable_for

    def self.executable_available?(executable, project_root)
      return true if executable == "dora"
      return File.executable?(File.expand_path(executable, project_root)) if executable.include?("/")

      ENV.fetch("PATH", "").split(File::PATH_SEPARATOR).any? { |directory| File.executable?(File.join(directory, executable)) }
    end
    private_class_method :executable_available?

    def self.passed(id, detail)
      {"id" => id, "status" => "passed", "detail" => detail}
    end
    private_class_method :passed

    def self.failed(id, detail)
      {"id" => id, "status" => "failed", "detail" => detail}
    end
    private_class_method :failed
  end
end
