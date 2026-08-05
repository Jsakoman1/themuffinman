# frozen_string_literal: true

require "yaml"
require "fileutils"
require_relative "project_commands"

module Dora
  class StarterPack
    def self.apply!(path, project_root:)
      pack = load!(path)
      Array(pack["directories"]).each { |relative| FileUtils.mkdir_p(File.join(project_root, relative)) }
      apply_template!(path, pack, project_root)
      pack.fetch("files").each do |relative, content|
        destination = File.join(project_root, relative)
        FileUtils.mkdir_p(File.dirname(destination))
        File.write(destination, content.is_a?(String) ? content : YAML.dump(content).sub(/\A---\n/, ""))
      end
      commands = {"kind" => "dora_project_commands", "version" => 1, "commands" => pack.fetch("commands")}
      File.write(File.join(project_root, ".dora/project-commands.yaml"), YAML.dump(commands).sub(/\A---\n/, ""))
      {"id" => pack.fetch("id"), "files" => pack.fetch("files").keys}
    end

    def self.apply_template!(pack_path, pack, project_root)
      template_root = pack["template_root"]
      return unless template_root

      fail!("starter pack template root is invalid") unless safe_relative_path?(template_root)
      package_root = File.expand_path("..", File.dirname(pack_path))
      source = File.expand_path(template_root, package_root)
      fail!("starter pack template root is missing") unless source.start_with?("#{package_root}/") && Dir.exist?(source)
      Dir.children(source).reject { |name| name == "template.yaml" }.each { |name| FileUtils.cp_r(File.join(source, name), project_root) }
    end
    private_class_method :apply_template!

    def self.load!(path)
      pack = YAML.load_file(path)
      fail!("starter pack is invalid") unless pack.is_a?(Hash) && pack["kind"] == "dora_starter_pack" && pack["version"].to_i == 1
      fail!("starter pack id is invalid") unless pack["id"].to_s.match?(/\A[a-z][a-z0-9-]*\z/)
      fail!("starter pack must be technical only") unless pack["technical_only"] == true
      fail!("starter pack commands must be a mapping") unless pack["commands"].is_a?(Hash)
      validate_commands!(pack["commands"])
      validate_paths!(pack)
      fail!("starter pack boundaries are required") unless Array(pack["boundaries"]).all? { |boundary| boundary.is_a?(String) && !boundary.empty? } && !Array(pack["boundaries"]).empty?

      pack
    rescue Psych::Exception => error
      fail!("starter pack YAML is invalid: #{error.message}")
    end

    def self.validate_commands!(commands)
      missing = ProjectCommands::REQUIRED.reject { |id| commands[id].is_a?(String) && !commands[id].empty? }
      fail!("starter pack commands are missing #{missing.join(", ")}") unless missing.empty?
      fail!("starter pack commands must not invoke Dora recursively") if commands.values.any? { |command| command.match?(/\bdora\b/) }
    end
    private_class_method :validate_commands!

    def self.validate_paths!(pack)
      directories = Array(pack["directories"])
      files = pack["files"] || {}
      fail!("starter pack files must be a mapping") unless files.is_a?(Hash)
      (directories + files.keys).each do |path|
        fail!("starter pack path must be project-relative") unless safe_relative_path?(path)
      end
    end
    private_class_method :validate_paths!

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
