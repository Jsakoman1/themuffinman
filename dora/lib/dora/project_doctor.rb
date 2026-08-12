# frozen_string_literal: true

require "shellwords"
require "yaml"
require_relative "adapter"
require_relative "artifact_policy"
require_relative "control_contracts"
require_relative "project_control"
require_relative "project_knowledge"
require_relative "work_artifact_audit"

module Dora
  class ProjectDoctor
    def self.run(adapter_path, schema_path:, control_schema_path:)
      checks = []
      adapter = YAML.load_file(adapter_path)
      project_root = project_root(adapter_path, adapter, checks)
      validated_adapter = validate_adapter(adapter_path, schema_path, checks)
      check_declared_paths(adapter, project_root, checks, generated_output_paths: validated_adapter&.dig("context", "generated_output_paths"))
      check_declared_commands(adapter, project_root, checks)
      check_control_bundle(adapter_path, project_root, control_schema_path, checks)
      check_project_knowledge(project_root, checks)
      check_work_artifacts(adapter_path, project_root, checks)
      {"healthy" => checks.none? { |check| check.fetch("status") == "failed" }, "checks" => checks}
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
      adapter = Adapter.validate!(adapter_path, schema_path)
      checks << passed("adapter", "adapter satisfies the Dora schema")
      adapter
    rescue ArgumentError => error
      checks << failed("adapter", error.message)
      nil
    end
    private_class_method :validate_adapter

    def self.check_declared_paths(adapter, project_root, checks, generated_output_paths: [])
      paths = adapter["paths"]
      unless paths.is_a?(Hash) && project_root
        checks << failed("declared-paths", "declared paths cannot be checked")
        return
      end
      generated_output_paths = Array(generated_output_paths).map(&:to_s)
      paths.each do |id, relative|
        absolute = File.expand_path(relative.to_s, project_root)
        if relative.is_a?(String) && !relative.start_with?("/") && (absolute == project_root || absolute.start_with?("#{project_root}/")) && File.exist?(absolute)
          checks << passed("path:#{id}", relative)
        elsif relative.is_a?(String) && !relative.start_with?("/") && (absolute == project_root || absolute.start_with?("#{project_root}/")) && generated_output_paths.include?(id.to_s)
          checks << passed("path:#{id}", "declared generated output path is not materialized yet: #{relative}")
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
      contract_checks = ControlContracts.validate(controls)
      checks.concat(contract_checks)
    rescue ArgumentError => error
      checks << failed("project-control", error.message)
    end
    private_class_method :check_control_bundle

    def self.check_project_knowledge(project_root, checks)
      unless project_root
        checks << failed("project-knowledge", "project knowledge cannot be checked")
        return
      end
      ProjectKnowledge.validate!(project_root)
      checks << passed("project-knowledge", "product brief, domain library, agent profile, and agent entrypoint are valid")
    rescue ArgumentError => error
      checks << failed("project-knowledge", error.message)
    end
    private_class_method :check_project_knowledge

    def self.check_work_artifacts(adapter_path, project_root, checks)
      policy_path = File.join(File.dirname(File.expand_path(adapter_path)), "controls", "artifact-policy.yaml")
      unless File.file?(policy_path)
        checks << failed("work-artifact-audit", "required artifact policy is missing: .dora/controls/artifact-policy.yaml; add the declared dora_artifact_policy control file before running the work artifact audit")
        return
      end

      audit_config = ArtifactPolicy.work_artifact_audit_config!(policy_path)
      return if audit_config.fetch("paths").empty?

      schema_path = File.expand_path("../../templates/work-artifact-schema.yaml", __dir__)
      report = WorkArtifactAudit.inspect!(project_root: project_root, paths: audit_config.fetch("paths"), non_executable_paths: audit_config.fetch("non_executable_paths"), schema_path: schema_path)
      report.fetch("findings").reject { |finding| finding.fetch("classification") == "valid" }.each do |finding|
        checks << advisory("work-artifact:#{finding.fetch("classification")}:#{finding.fetch("source_reference")}", "work artifact is #{finding.fetch("classification")}", references: [finding.fetch("source_reference")], observed_at: finding.fetch("observed_at"))
      end
      checks.concat(verified_work_inventory_contradictions(report, project_root))
    rescue ArgumentError, Psych::Exception => error
      checks << failed("work-artifact-audit", error.message)
    end
    private_class_method :check_work_artifacts

    def self.verified_work_inventory_contradictions(report, project_root)
      report.fetch("findings").each_with_object([]) do |finding, contradictions|
        next unless finding.fetch("classification") == "valid"

        work_path = File.join(project_root, finding.fetch("source_reference"))
        work = YAML.load_file(work_path)
        next unless work["kind"] == "work" && work["status"] == "verified"

        inventory_reference = work["execution_inventory"]
        next unless safe_project_path?(inventory_reference)

        inventory_path = File.expand_path(inventory_reference, project_root)
        next unless inventory_path.start_with?("#{project_root}/") && File.file?(inventory_path)

        inventory = YAML.load_file(inventory_path)
        next unless inventory.is_a?(Hash) && inventory["kind"] == "execution_inventory" && inventory["state"] != "verified"

        master_reference = inventory["master_plan"]
        unless safe_project_path?(master_reference)
          contradictions << advisory("work-artifact:invalid-inventory-master-reference:#{finding.fetch("source_reference")}", "execution inventory has no valid canonical master reference", references: [finding.fetch("source_reference"), inventory_reference], observed_at: finding.fetch("observed_at"))
          next
        end
        master_path = File.expand_path(master_reference, project_root)
        unless master_path.start_with?("#{project_root}/") && File.file?(master_path)
          contradictions << advisory("work-artifact:missing-inventory-master:#{finding.fetch("source_reference")}", "execution inventory canonical master is missing", references: [finding.fetch("source_reference"), inventory_reference, master_reference], observed_at: finding.fetch("observed_at"))
          next
        end

        master = YAML.load_file(master_path)
        next unless master.is_a?(Hash) && %w[master work].include?(master["kind"]) && master["status"] == "verified"

        contradictions << advisory("work-artifact:verified-work-active-inventory:#{finding.fetch("source_reference")}", "verified work references an execution inventory whose terminal canonical master is verified but whose state is not verified", references: [finding.fetch("source_reference"), inventory_reference, master_reference], observed_at: finding.fetch("observed_at"))
      end
    rescue Psych::Exception
      []
    end
    private_class_method :verified_work_inventory_contradictions

    def self.safe_project_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_project_path?

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

    def self.advisory(id, detail, references:, observed_at:)
      {"id" => id, "status" => "advisory", "detail" => detail, "source_references" => references, "observed_at" => observed_at, "read_only" => true, "disposition" => "advisory"}
    end
    private_class_method :advisory
  end
end
