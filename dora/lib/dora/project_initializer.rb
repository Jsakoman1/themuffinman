# frozen_string_literal: true

require "fileutils"
require "yaml"
require_relative "ci_pack"
require_relative "stack_pack"

module Dora
  class ProjectInitializer
    def self.initialize!(destination, project_id:, manifest_path:, stack_pack_path: nil, ci_pack_path: nil, ci_template_path: nil)
      manifest = YAML.load_file(manifest_path)
      fail!("init manifest kind is invalid") unless manifest["kind"] == "dora_init_manifest" && manifest["version"].to_i == 1
      fail!("project id is invalid") unless project_id.match?(/\A[a-z][a-z0-9-]*\z/)
      root = File.expand_path(destination)
      FileUtils.mkdir_p(root)
      fail!("Dora control directory already exists: #{root}/.dora") if Dir.exist?(File.join(root, ".dora"))

      Array(manifest.fetch("directories")).each { |relative| FileUtils.mkdir_p(File.join(root, relative)) }
      write_yaml(File.join(root, ".dora/project.yaml"), adapter(project_id))
      write_yaml(File.join(root, ".dora/project-control.yaml"), project_control)
      control_files.each { |relative, content| write_yaml(File.join(root, relative), content) }
      files = manifest.fetch("files")
      files += StackPack.apply!(stack_pack_path, project_root: root).fetch("files") if stack_pack_path
      files << CiPack.apply!(ci_pack_path, template_path: ci_template_path, project_root: root) if ci_pack_path
      files
    end

    def self.adapter(project_id)
      {"kind" => "dora_project_adapter", "version" => 1, "project" => {"id" => project_id, "root" => ".."}, "paths" => {"docs" => "docs", "work_plans" => "docs/work", "audit_output" => "docs/audit-output", "runtime_evidence" => "docs/runtime-evidence"}, "commands" => {"work_start" => "dora work-start .dora/project.yaml plan=<work-plan> task=<task-id>", "work_verify" => "dora work-verify .dora/project.yaml plan=<work-plan> task=<task-id>", "control_check" => "dora doctor .dora/project.yaml"}, "extensions" => [{"id" => "project-documentation", "category" => "documentation", "paths" => ["docs"], "invocation" => "project-defined documentation checks"}]}
    end
    private_class_method :adapter

    def self.project_control
      {"kind" => "dora_project_control", "version" => 1, "controls" => control_files.keys.to_h { |relative| [File.basename(relative, ".yaml").tr("-", "_"), relative] }}
    end
    private_class_method :project_control

    def self.control_files
      {".dora/controls/tool-catalog.yaml" => {"kind" => "dora_tool_catalog", "version" => 1, "commands" => [{"id" => "control-check", "target" => "control-check", "purpose" => "Run project-defined control checks.", "preconditions" => ["project setup complete"], "expected_cost" => "short"}]}, ".dora/controls/change-routing.yaml" => {"kind" => "dora_change_routing", "version" => 1, "rules" => []}, ".dora/controls/context-search.yaml" => {"kind" => "dora_context_search", "version" => 1, "roots" => ["docs"], "exclusions" => []}, ".dora/controls/workspace-inventory.yaml" => {"kind" => "dora_workspace_inventory", "version" => 1, "categories" => []}, ".dora/controls/documentation-evidence.yaml" => {"kind" => "dora_documentation_evidence", "version" => 1, "claims" => []}, ".dora/controls/system-map.yaml" => {"kind" => "dora_system_map", "version" => 1, "nodes" => [], "edges" => []}, ".dora/controls/artifact-policy.yaml" => {"kind" => "dora_artifact_policy", "version" => 1, "generated_roots" => ["docs/audit-output"], "deletion_authority" => "project_authorized_command"}, ".dora/controls/backlog.yaml" => {"kind" => "dora_backlog", "version" => 1, "sources" => []}}
    end
    private_class_method :control_files

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
