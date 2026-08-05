# frozen_string_literal: true

require "yaml"

module Dora
  class AgentNext
    def self.next!(project_root:, inventory_path:)
      root = File.expand_path(project_root)
      fail!("execution inventory must be project-relative") unless safe_relative_path?(inventory_path)
      path = File.expand_path(inventory_path, root)
      fail!("execution inventory is missing: #{inventory_path}") unless path.start_with?("#{root}/") && File.file?(path)
      inventory = YAML.load_file(path)
      items = Array(inventory["items"])
      fail!("execution inventory has no items") if items.empty?
      active = items.select { |item| item["status"] == "in_progress" }
      fail!("execution inventory has multiple active items") if active.length > 1
      return recommendation(root, active.first, "continue", inventory_path) if active.any?

      pending = items.find { |item| item["status"] == "pending" }
      return {"kind" => "dora_agent_next", "version" => 1, "action" => "none", "reason" => "No pending declared item remains; inspect the plan evidence instead of inferring product completion."}.freeze unless pending
      previous = items.take_while { |item| item != pending }.last
      return recommendation(root, pending, "start", inventory_path) unless previous && previous["status"] != "verified"

      {"kind" => "dora_agent_next", "version" => 1, "action" => "blocked", "item" => pending.fetch("id"), "blocker" => previous.fetch("id"), "reason" => "The direct predecessor is not verified.", "citation" => inventory_path}.freeze
    rescue Psych::Exception => error
      fail!("execution inventory YAML is invalid: #{error.message}")
    end

    def self.recommendation(root, item, action, inventory_path)
      plan_path = item.fetch("plan")
      fail!("next item plan is invalid") unless safe_relative_path?(plan_path) && File.file?(File.expand_path(plan_path, root))
      {"kind" => "dora_agent_next", "version" => 1, "action" => action, "item" => item.fetch("id"), "plan" => plan_path, "task" => item.fetch("task"), "reason" => action == "continue" ? "This is the only declared active item." : "This is the first pending item whose direct predecessor is verified.", "citation" => inventory_path}.freeze
    end
    private_class_method :recommendation

    def self.safe_relative_path?(path); path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?(".."); end
    private_class_method :safe_relative_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
