# frozen_string_literal: true

require "open3"
require "time"
require "yaml"

module Dora
  # A bounded, advisory comparison between a task's start-time dirty path set and
  # the current dirty path set. It intentionally does not attribute edits made to
  # an already-dirty path after start.
  class TaskChangeSet
    def self.workspace_paths!(project_root:, baseline:)
      root = File.realpath(project_root)
      git_root, status = Open3.capture2("git", "-C", root, "rev-parse", "--show-toplevel")
      raise ArgumentError, "cannot read Git worktree root" unless status.success?

      changed, changed_status = Open3.capture2e("git", "-C", root, "diff", "--name-only", baseline)
      raise ArgumentError, changed unless changed_status.success?
      untracked, untracked_status = Open3.capture2e("git", "-C", root, "ls-files", "--others", "--exclude-standard")
      raise ArgumentError, untracked unless untracked_status.success?

      (changed.lines + untracked.lines).map(&:strip).reject(&:empty?).each_with_object([]) { |path, paths| relative = project_relative_path(path, root: root, git_root: File.realpath(git_root.strip)); paths << relative if relative }.uniq.sort
    end

    def self.build!(project_root:, work_plan:, task_id:)
      root = File.realpath(project_root)
      raise ArgumentError, "work plan must be project-relative" unless safe_relative_path?(work_plan)
      plan_path = File.expand_path(work_plan, root)
      raise ArgumentError, "work plan is missing: #{work_plan}" unless plan_path.start_with?("#{root}/") && File.file?(plan_path)

      plan = YAML.load_file(plan_path)
      task = Array(plan["tasks"]).find { |candidate| candidate.is_a?(Hash) && candidate["id"] == task_id }
      raise ArgumentError, "work plan task is missing: #{task_id}" unless task
      start_paths = Array(task["start_workspace_paths"])
      raise ArgumentError, "task has no start workspace path snapshot" unless task["started_at"].is_a?(String) && start_paths.all? { |path| safe_relative_path?(path) }

      current_paths = workspace_paths!(project_root: root, baseline: plan.fetch("baseline"))
      administrative_paths = [work_plan, plan["execution_inventory"]].select { |path| safe_relative_path?(path) }
      {"kind" => "dora_task_change_set", "version" => 1, "observed_at" => Time.now.utc.iso8601, "read_only" => true, "disposition" => "advisory", "task" => {"plan" => work_plan, "id" => task_id, "started_at" => task.fetch("started_at")}, "changed_since_start" => (current_paths - start_paths - administrative_paths).sort, "preexisting_dirty_paths" => start_paths, "excluded_administrative_paths" => administrative_paths, "attribution_boundary" => "Changes to paths already dirty at task start are not attributed to this task; work-plan and inventory bookkeeping paths are excluded from implementation closeout."}.freeze
    rescue Psych::Exception => error
      raise ArgumentError, "work plan YAML is invalid: #{error.message}"
    end

    def self.project_relative_path(path, root:, git_root:)
      absolute = File.expand_path(path, git_root)
      return nil unless absolute.start_with?("#{root}/")

      absolute.delete_prefix("#{root}/")
    end
    private_class_method :project_relative_path

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?
  end
end
