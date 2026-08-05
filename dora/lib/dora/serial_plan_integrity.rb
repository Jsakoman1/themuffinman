# frozen_string_literal: true

require "set"
require "yaml"

module Dora
  class SerialPlanIntegrity
    def self.validate!(master_path, inventory_path, project_root:)
      master = YAML.load_file(master_path)
      inventory = YAML.load_file(inventory_path)
      fail!("master kind is invalid") unless master["kind"] == "master"
      fail!("execution inventory kind is invalid") unless inventory["kind"] == "execution_inventory"
      children = Array(master["children"])
      items = Array(inventory["items"])
      fail!("master must declare child plans") if children.empty?
      fail!("execution inventory must declare items") if items.empty?
      sequence = items.map { |item| item["sequence"] || item["order"] }
      fail!("inventory sequence must be contiguous from 1") unless sequence == (1..items.length).to_a
      ids = items.map { |item| item["id"] }
      fail!("inventory ids are duplicated") unless ids.uniq.length == ids.length

      mapped_tasks = items.each_with_index.map do |item, index|
        plan_path = item.fetch("plan")
        fail!("inventory references non-child plan: #{plan_path}") unless children.include?(plan_path)
        plan = YAML.load_file(resolve_under!(project_root, plan_path))
        task = Array(plan["tasks"]).find { |candidate| candidate["id"] == item["task"] }
        fail!("inventory task is missing: #{plan_path}##{item["task"]}") unless task
        fail!("task inventory mapping differs: #{item["id"]}") unless task["inventory_item"] == item["id"]
        expected_dependencies = index.zero? ? [] : [items[index - 1]["id"]]
        fail!("task serial dependencies differ: #{item["id"]}") unless Array(task["dependencies"]) == expected_dependencies
        required_paths = Array(task["required_paths"])
        fail!("task required paths are missing: #{item["id"]}") if required_paths.empty? || Array(task["paths"]) != required_paths
        fail!("task evidence boundary is missing: #{item["id"]}") if Array(task["evidence_boundary"]).empty?
        fail!("task has recursive validation: #{item["id"]}") if recursive_validation?(task["validation"])
        [plan_path, task.fetch("id")]
      end
      fail!("inventory task mappings are duplicated") unless mapped_tasks.uniq.length == mapped_tasks.length
      child_tasks = children.flat_map do |child|
        plan = YAML.load_file(resolve_under!(project_root, child))
        Array(plan["tasks"]).map { |task| [child, task.fetch("id")] }
      end
      fail!("inventory does not map every child task exactly once") unless child_tasks.to_set == mapped_tasks.to_set
      true
    end

    def self.resolve_under!(root, relative)
      fail!("plan path must be relative") unless relative.is_a?(String) && !relative.empty? && !relative.start_with?("/")
      path = File.expand_path(relative, root)
      fail!("plan path resolves outside project root") unless path.start_with?("#{File.expand_path(root)}/")
      fail!("plan does not exist: #{relative}") unless File.file?(path)
      path
    end
    private_class_method :resolve_under!

    def self.recursive_validation?(command)
      command.to_s.match?(/\bwork-verify\b/) || command.to_s.match?(%r{scripts/verify-work\.rb})
    end
    private_class_method :recursive_validation?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
