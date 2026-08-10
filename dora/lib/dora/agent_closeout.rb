# frozen_string_literal: true

require_relative "agent_context"
require_relative "change_check"
require_relative "task_change_set"

module Dora
  class AgentCloseout
    def self.review_started_task!(project_root:, work_plan:, task_id:, impact_path:)
      change_set = TaskChangeSet.build!(project_root: project_root, work_plan: work_plan, task_id: task_id)
      review!(project_root: project_root, work_plan: work_plan, task_id: task_id, impact_path: impact_path, changed_paths: change_set.fetch("changed_since_start")).merge("change_set" => change_set).freeze
    end

    def self.review!(project_root:, work_plan:, task_id:, impact_path:, changed_paths:)
      root = File.expand_path(project_root)
      fail!("change impact path must be project-relative") unless safe_relative_path?(impact_path)
      impact_config = File.expand_path(impact_path, root)
      fail!("change impact config is missing: #{impact_path}") unless impact_config.start_with?("#{root}/") && File.file?(impact_config)

      context = AgentContext.build!(project_root: root, work_plan: work_plan, task_id: task_id)
      impact = ChangeCheck.check!(impact_config, changed_paths)
      required_paths = Array(context.dig("task", "required_paths"))
      missing_paths = required_paths - changed_paths

      {
        "kind" => "dora_agent_closeout", "version" => 1, "read_only" => true,
        "task" => context.fetch("task").slice("id", "title", "validation", "evidence_boundary"),
        "required_paths" => {"declared" => required_paths, "missing_from_change_set" => missing_paths},
        "impact_obligations" => impact.slice("validations", "documentation", "runtime_evidence", "decisions", "companion_findings", "unmatched_paths"),
        "completion" => {"status_mutation" => false, "verified" => false, "reason" => "Dora reports declared gaps; the project verifier records completion evidence."},
        "next_steps" => next_steps(missing_paths, impact)
      }.freeze
    end

    def self.next_steps(missing_paths, impact)
      steps = []
      steps << "Add declared required paths: #{missing_paths.join(", ")}" unless missing_paths.empty?
      steps << "Run and record declared validation: #{impact.fetch("validations").join(", ")}" unless impact.fetch("validations").empty?
      steps << "Update declared documentation: #{impact.fetch("documentation").join(", ")}" unless impact.fetch("documentation").empty?
      steps << "Capture declared runtime evidence: #{impact.fetch("runtime_evidence").join(", ")}" unless impact.fetch("runtime_evidence").empty?
      steps << "Review declared decisions: #{impact.fetch("decisions").join(", ")}" unless impact.fetch("decisions").empty?
      steps << "Classify declared companion findings before expanding scope." unless impact.fetch("companion_findings").empty?
      steps << "Classify unmatched changed paths: #{impact.fetch("unmatched_paths").join(", ")}" unless impact.fetch("unmatched_paths").empty?
      steps << "Use the project verifier to record evidence after these gaps are resolved."
      steps
    end
    private_class_method :next_steps

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
