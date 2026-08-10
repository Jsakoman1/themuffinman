# frozen_string_literal: true

require "yaml"
require_relative "project_knowledge"

module Dora
  class AgentContext
    def self.build!(project_root:, work_plan:, task_id:)
      root = File.expand_path(project_root)
      fail!("work plan must be project-relative") unless safe_relative_path?(work_plan)
      plan_path = File.expand_path(work_plan, root)
      fail!("work plan is missing: #{work_plan}") unless plan_path.start_with?("#{root}/") && File.file?(plan_path)
      plan = YAML.load_file(plan_path)
      task = Array(plan["tasks"]).find { |candidate| candidate.is_a?(Hash) && candidate["id"] == task_id }
      fail!("work plan task is missing: #{task_id}") unless task
      knowledge = ProjectKnowledge.validate!(root)
      citations = [
        {"id" => "product-brief", "path" => "docs/product-brief.yaml", "reason" => "product intent and open decisions"},
        {"id" => "domain-library", "path" => "docs/domain-library.yaml", "reason" => "domain rules and workflows"},
        {"id" => "agent-profile", "path" => ".dora/agent-project-profile.yaml", "reason" => "authority, commands, evidence, and work order"},
        {"id" => "work-plan", "path" => work_plan, "reason" => "selected bounded task"}
      ]
      {"kind" => "dora_agent_context", "version" => 1, "task" => task.slice("id", "title", "observable_outcome", "dependencies", "required_paths", "validation", "evidence_boundary", "implementation_contract"), "knowledge" => {"product" => knowledge.fetch("product_brief").slice("product", "user_problem", "intended_outcomes", "non_goals", "unanswered_decisions"), "domain" => knowledge.fetch("domain_library").slice("vocabulary", "invariants", "permission_rules", "workflows"), "agent" => knowledge.fetch("agent_profile").slice("stack_commands", "authority_limits", "evidence_requirements", "implementation_order")}, "citations" => citations, "omitted" => ["undeclared repository files", "product inference", "completion conclusion"]}.freeze
    rescue Psych::Exception => error
      fail!("work plan YAML is invalid: #{error.message}")
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
