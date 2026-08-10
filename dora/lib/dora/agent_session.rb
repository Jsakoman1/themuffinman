# frozen_string_literal: true

require_relative "adapter"
require_relative "agent_context"
require_relative "agent_next"
require_relative "project_status"
require "time"

module Dora
  class AgentSession
    def self.build!(adapter_path:, inventory_path:, work_plan:, task_id:, adapter_schema_path:, control_schema_path:)
      adapter = Adapter.validate!(adapter_path, adapter_schema_path)
      root = adapter.fetch("root")
      context = AgentContext.build!(project_root: root, work_plan: work_plan, task_id: task_id)
      status = ProjectStatus.report!(adapter_path: adapter_path, inventory_path: inventory_path, adapter_schema_path: adapter_schema_path, control_schema_path: control_schema_path)
      next_action = AgentNext.next!(project_root: root, inventory_path: inventory_path)
      citations = context.fetch("citations") + [{"id" => "adapter", "path" => ".dora/project.yaml", "reason" => "declared project root and controls"}, {"id" => "execution-inventory", "path" => inventory_path, "reason" => "declared work order and evidence state"}]
      {"kind" => "dora_agent_session", "version" => 1, "observed_at" => Time.now.utc.iso8601, "read_only" => true, "work" => {"selected" => context.fetch("task"), "next" => next_action}, "implementation_contract" => implementation_contract(adapter: adapter, work_plan: work_plan, task_id: task_id, task: context.fetch("task")), "health" => status.fetch("health"), "tools" => context.dig("knowledge", "agent", "stack_commands") || [], "gaps" => status.fetch("evidence_gaps"), "decisions" => {"open" => status.fetch("open_decisions"), "recorded" => status.fetch("decision_log")}, "approval_boundary" => context.dig("knowledge", "agent", "authority_limits"), "citations" => citations, "omitted" => (context.fetch("omitted") + ["work mutation", "approval decision", "undeclared tool selection"]).uniq, "completion_boundary" => "Agent session is read-only navigation context; only project work verification records completion evidence."}.freeze
    end

    def self.implementation_contract(adapter:, work_plan:, task_id:, task:)
      {
        "start" => {"command_template" => adapter.fetch("commands").fetch("work_start"), "plan" => work_plan, "task" => task_id, "invoked" => false},
        "validation" => task.fetch("validation").dup,
        "evidence_boundary" => Array(task.fetch("evidence_boundary")).dup,
        "closeout" => {"changed_paths" => "must be supplied explicitly after implementation", "status_mutation" => false, "verification_authority" => "project work verifier"}
      }.freeze
    end
    private_class_method :implementation_contract
  end
end
