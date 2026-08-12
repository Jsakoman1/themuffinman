# frozen_string_literal: true

require "fileutils"
require "yaml"
require_relative "ci_pack"
require_relative "project_launcher"
require_relative "stack_pack"
require_relative "package_copy"

module Dora
  class ProjectInitializer
    def self.initialize!(destination, project_id:, manifest_path:, stack_pack_path: nil, ci_pack_path: nil, ci_template_path: nil, ci_commands_path: nil, dora_source: nil)
      manifest = YAML.load_file(manifest_path)
      fail!("init manifest kind is invalid") unless manifest["kind"] == "dora_init_manifest" && manifest["version"].to_i == 1
      fail!("project id is invalid") unless project_id.match?(/\A[a-z][a-z0-9-]*\z/)
      root = File.expand_path(destination)
      FileUtils.mkdir_p(root)
      fail!("Dora control directory already exists: #{root}/.dora") if Dir.exist?(File.join(root, ".dora"))
      copy_dora_package!(root, dora_source) if dora_source

      Array(manifest.fetch("directories")).each { |relative| FileUtils.mkdir_p(File.join(root, relative)) }
      write_yaml(File.join(root, ".dora/project.yaml"), adapter(project_id))
      write_yaml(File.join(root, ".dora/project-control.yaml"), project_control)
      write_yaml(File.join(root, ".dora/bootstrap-source.yaml"), {"kind" => "dora_bootstrap_record", "version" => 1, "source" => dora_source, "package_path" => "dora"}) if dora_source
      control_files.each { |relative, content| write_yaml(File.join(root, relative), content) }
      files = manifest.fetch("files").dup
      agent_knowledge_files.each do |relative, template|
        FileUtils.mkdir_p(File.dirname(File.join(root, relative)))
        FileUtils.cp(template, File.join(root, relative))
      end
      files << ProjectLauncher.write!(root, template_path: File.expand_path("../../templates/project-launcher", __dir__))
      files += StackPack.apply!(stack_pack_path, project_root: root).fetch("files") if stack_pack_path
      if ci_pack_path
        fail!("CI project command declarations are required") unless ci_commands_path && File.file?(ci_commands_path)
        commands_destination = File.join(root, ".dora/project-commands.yaml")
        File.write(commands_destination, File.read(ci_commands_path))
        files << ".dora/project-commands.yaml"
        files << CiPack.apply!(ci_pack_path, template_path: ci_template_path, project_root: root, project_commands_path: commands_destination)
      end
      files
    end

    def self.copy_dora_package!(root, source)
      fail!("Dora source must declare a verified local path") unless source.is_a?(Hash) && source.dig("source", "path").is_a?(String)
      source_root = File.realpath(source.dig("source", "path"))
      package_root = File.join(root, "dora")
      fail!("Dora package directory already exists: #{package_root}") if File.exist?(package_root)
      fail!("project destination cannot contain its Dora source") if nested?(root, source_root) || nested?(source_root, root)

      FileUtils.mkdir_p(package_root)
      PackageCopy.copy!(source_root: source_root, destination: package_root)
    rescue Errno::ENOENT
      fail!("Dora source path does not exist")
    end
    private_class_method :copy_dora_package!

    def self.nested?(candidate, parent)
      expanded_candidate = File.expand_path(candidate)
      expanded_parent = File.expand_path(parent)
      expanded_candidate == expanded_parent || expanded_candidate.start_with?("#{expanded_parent}/")
    end
    private_class_method :nested?

    def self.adapter(project_id)
      {"kind" => "dora_project_adapter", "version" => 1, "project" => {"id" => project_id, "root" => ".."}, "paths" => {"docs" => "docs", "work_plans" => "docs/work", "audit_output" => "docs/audit-output", "runtime_evidence" => "docs/runtime-evidence"}, "commands" => {"work_start" => "./bin/dora work-start .dora/project.yaml plan=<work-plan> task=<task-id>", "work_verify" => "./bin/dora work-verify .dora/project.yaml plan=<work-plan> task=<task-id>", "control_check" => "./bin/dora doctor .dora/project.yaml"}, "extensions" => [{"id" => "project-documentation", "category" => "documentation", "paths" => ["docs"], "invocation" => "project-defined documentation checks"}]}
    end
    private_class_method :adapter

    def self.project_control
      {"kind" => "dora_project_control", "version" => 1, "controls" => control_files.keys.to_h { |relative| [File.basename(relative, ".yaml").tr("-", "_"), relative] }}
    end
    private_class_method :project_control

    def self.control_files
      {".dora/controls/tool-catalog.yaml" => {"kind" => "dora_tool_catalog", "version" => 1, "commands" => [{"id" => "control-check", "target" => "control-check", "purpose" => "Run project-defined control checks.", "preconditions" => ["project setup complete"], "expected_cost" => "short"}]}, ".dora/controls/change-routing.yaml" => {"kind" => "dora_change_routing", "version" => 1, "rules" => []}, ".dora/controls/context-search.yaml" => {"kind" => "dora_context_search", "version" => 1, "roots" => ["docs"], "exclusions" => []}, ".dora/controls/workspace-inventory.yaml" => {"kind" => "dora_workspace_inventory", "version" => 1, "categories" => []}, ".dora/controls/documentation-evidence.yaml" => {"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "project-knowledge", "match" => "project", "evidence" => ["docs/product-brief.yaml"]}]}, ".dora/controls/system-map.yaml" => {"kind" => "dora_system_map", "version" => 1, "nodes" => [{"id" => "project"}], "edges" => []}, ".dora/controls/capability-inventory.yaml" => {"kind" => "dora_capability_inventory", "version" => 1, "component" => {"id" => "project-component", "owner" => "project-owner"}, "capabilities" => []}, ".dora/controls/artifact-policy.yaml" => {"kind" => "dora_artifact_policy", "version" => 1, "generated_roots" => ["docs/audit-output"], "deletion_authority" => "project_authorized_command"}, ".dora/controls/backlog.yaml" => {"kind" => "dora_backlog", "version" => 1, "sources" => []}}
    end
    private_class_method :control_files

    def self.agent_knowledge_files
      templates = File.expand_path("../../templates", __dir__)
      {
        "docs/product-brief.yaml" => File.join(templates, "product-brief.yaml"),
        "docs/domain-library.yaml" => File.join(templates, "domain-library.yaml"),
        ".dora/agent-project-profile.yaml" => File.join(templates, "agent-project-profile.yaml"),
        "AGENTS.md" => File.join(templates, "codex-project-entrypoint.md")
      }
    end
    private_class_method :agent_knowledge_files

    def self.write_yaml(path, value)
      File.write(path, YAML.dump(value).sub(/\A---\n/, ""))
    end
    private_class_method :write_yaml

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
