# frozen_string_literal: true

require "fileutils"
require "yaml"
require_relative "project_commands"

module Dora
  class CiPack
    def self.apply!(pack_path, template_path:, project_root:, project_commands_path:)
      pack = YAML.load_file(pack_path)
      fail!("CI pack is invalid") unless pack["kind"] == "dora_ci_pack" && pack["version"].to_i == 1
      workflow_path = pack.fetch("workflow_path")
      fail!("CI workflow path must be project-relative") unless safe_path?(workflow_path)
      commands = pack.fetch("commands")
      required = %w[doctor adapter_validation]
      fail!("CI pack commands are incomplete") unless commands.is_a?(Hash) && required.all? { |key| commands[key].is_a?(String) && !commands[key].empty? }
      workflow_commands = commands.merge(ProjectCommands.load!(project_commands_path))
      workflow = File.read(template_path).gsub(/\{\{([^}]+)\}\}/) { workflow_commands.fetch(Regexp.last_match(1)) }
      destination = File.join(project_root, workflow_path)
      FileUtils.mkdir_p(File.dirname(destination))
      File.write(destination, workflow)
      workflow_path
    end

    def self.safe_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
