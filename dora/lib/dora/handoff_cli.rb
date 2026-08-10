# frozen_string_literal: true

require_relative "handoff"
require_relative "../../bridge/lib/dora_bridge/project_registry"

module Dora
  # Local-only service boundary used by Codex. It validates the private registry
  # and normal Dora evidence instead of exposing handoff files for direct edits.
  class HandoffCLI
    def self.run!(command:, registry_path:, state_root:, project:, id: nil, master_plan: nil, work_plan: nil, reason: nil, verification_plan: nil, verification_task: nil, feedback: nil, owner_decision: nil, completion_result: nil, runner_failure: nil)
      registry = DoraBridge::ProjectRegistry.load!(registry_path)
      registry.handoff_authorized!(project)
      store = Handoff.new(state_root: state_root)
      case command
      when "next" then store.next_ready!(project: project)
      when "get" then store.get!(id: required!(id, "handoff id"), project: project)
      when "claim" then store.claim!(id: required!(id, "handoff id"), project: project, claimed_by: "codex")
      when "link"
        model = registry.read_model!(project)
        master = required!(master_plan, "master plan")
        model.plan(master)
        store.link_delivery!(id: required!(id, "handoff id"), project: project, master_plan: master, work_plan: work_plan)
      when "block" then store.block!(id: required!(id, "handoff id"), project: project, reason: required!(reason, "blocked reason"))
      when "block_runner_failure" then store.block_runner_failure!(id: required!(id, "handoff id"), project: project, failure: runner_failure)
      when "feedback" then store.feedback!(id: required!(id, "handoff id"), project: project, feedback: feedback)
      when "block_owner_decision" then store.block_owner_decision!(id: required!(id, "handoff id"), project: project, owner_decision: owner_decision)
      when "complete"
        plan = required!(verification_plan, "verification plan")
        task = required!(verification_task, "verification task")
        evidence = registry.read_model!(project).task_evidence(plan, task)
        fail!("handoff completion requires passing Dora verification evidence") unless evidence.fetch("status") == "passed"
        store.complete!(id: required!(id, "handoff id"), project: project, verification_references: ["#{plan}##{task}"], completion_result: completion_result)
      else
        fail!("handoff command is invalid")
      end
    end

    def self.required!(value, label)
      fail!("#{label} is required") unless value.is_a?(String) && !value.empty?
      value
    end
    private_class_method :required!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
