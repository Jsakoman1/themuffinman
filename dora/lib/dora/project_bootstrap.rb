# frozen_string_literal: true

require "fileutils"
require "yaml"
require_relative "bootstrap_source"
require_relative "package_copy"
require_relative "project_initializer"
require_relative "starter_pack"
require_relative "ci_pack"

module Dora
  class ProjectBootstrap
    PACKAGE_DIRECTORY = "dora"

    def self.bootstrap!(destination, project_id:, source_descriptor_path:, starter_pack_path: nil, ci_pack_path: nil)
      source = BootstrapSource.load!(source_descriptor_path)
      project_root = File.expand_path(destination)
      fail!("bootstrap destination must be empty") if File.exist?(project_root) && !Dir.children(project_root).empty?
      fail!("bootstrap destination cannot contain its source") if nested?(project_root, source.fetch("path")) || nested?(source.fetch("path"), project_root)

      FileUtils.mkdir_p(project_root)
      package_root = File.join(project_root, PACKAGE_DIRECTORY)
      FileUtils.mkdir_p(package_root)
      PackageCopy.copy!(source_root: source.fetch("path"), destination: package_root)

      ProjectInitializer.initialize!(
        project_root,
        project_id: project_id,
        manifest_path: File.join(package_root, "templates/init-manifest.yaml")
      )
      complete_control_defaults(project_root)
      StarterPack.apply!(starter_pack_path, project_root: project_root) if starter_pack_path
      apply_ci!(project_root, package_root, ci_pack_path) if ci_pack_path
      write_source_record(project_root, source)

      {"project_root" => project_root, "package_path" => PACKAGE_DIRECTORY, "source_ref" => source.fetch("ref"), "agent_profile" => ".dora/agent-project-profile.yaml"}
    end

    def self.apply_ci!(project_root, package_root, ci_pack_path)
      commands_path = File.join(project_root, ".dora/project-commands.yaml")
      fail!("a starter with declared project commands is required for CI") unless File.file?(commands_path)
      CiPack.apply!(ci_pack_path, template_path: File.join(package_root, "templates/github-actions-control.yml"), project_root: project_root, project_commands_path: commands_path)
    end
    private_class_method :apply_ci!

    def self.write_source_record(project_root, source)
      record = {"kind" => "dora_bootstrap_record", "version" => 1, "source" => source, "package_path" => PACKAGE_DIRECTORY}
      File.write(File.join(project_root, ".dora/bootstrap-source.yaml"), YAML.dump(record).sub(/\A---\n/, ""))
    end
    private_class_method :write_source_record

    def self.complete_control_defaults(project_root)
      controls = File.join(project_root, ".dora/controls")
      write_yaml(File.join(project_root, "docs/backlog.md"), "# Backlog\n")
      write_yaml(File.join(controls, "change-routing.yaml"), {"kind" => "dora_change_routing", "version" => 1, "rules" => [{"id" => "project", "path_prefixes" => [""], "commands" => ["./bin/dora doctor .dora/project.yaml"]}]})
      write_yaml(File.join(controls, "workspace-inventory.yaml"), {"kind" => "dora_workspace_inventory", "version" => 1, "categories" => [{"id" => "project", "path_prefixes" => [""]}]})
      write_yaml(File.join(controls, "documentation-evidence.yaml"), {"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "backlog", "match" => "Backlog", "evidence" => ["docs/backlog.md"]}]})
      write_yaml(File.join(controls, "system-map.yaml"), {"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "project"}], "edges" => []})
      write_yaml(File.join(controls, "backlog.yaml"), {"kind" => "dora_backlog", "version" => 1, "sources" => ["docs/backlog.md"]})
    end
    private_class_method :complete_control_defaults

    def self.write_yaml(path, value)
      File.write(path, value.is_a?(String) ? value : YAML.dump(value).sub(/\A---\n/, ""))
    end
    private_class_method :write_yaml

    def self.nested?(candidate, parent)
      expanded_candidate = File.expand_path(candidate)
      expanded_parent = File.expand_path(parent)
      expanded_candidate == expanded_parent || expanded_candidate.start_with?("#{expanded_parent}/")
    end
    private_class_method :nested?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
