# frozen_string_literal: true

require "time"
require "yaml"

require_relative "adapter"
require_relative "agent_next"
require_relative "decision_log"
require_relative "project_doctor"
require_relative "project_knowledge"
require_relative "project_memory"

module Dora
  # Read-only, sanitized aggregation of declared Dora project artifacts.
  #
  # This is intentionally independent from transport and client identity. A bridge
  # selects an allowed adapter before constructing this object; this model then
  # reads only files under that adapter's declared project root and work-plan root.
  class ProjectReadModel
    SCHEMA_PATH = File.expand_path("../../project-adapter.schema.yaml", __dir__)
    CONTROL_SCHEMA_PATH = File.expand_path("../../project-control.schema.yaml", __dir__)
    STATUS = %w[HEALTHY WARNING INVALID].freeze

    def self.load!(adapter_path:, adapter_schema_path: SCHEMA_PATH, control_schema_path: CONTROL_SCHEMA_PATH)
      new(adapter_path: adapter_path, adapter_schema_path: adapter_schema_path, control_schema_path: control_schema_path)
    end

    def initialize(adapter_path:, adapter_schema_path:, control_schema_path:)
      @adapter_path = File.expand_path(adapter_path)
      @adapter = Adapter.validate!(@adapter_path, adapter_schema_path)
      @root = File.realpath(@adapter.fetch("root"))
      @control_schema_path = control_schema_path
      @work_root = resolve_declared_root!("work_plans")
      @docs_root = resolve_declared_root!("docs")
      @runtime_evidence_root = @adapter.fetch("paths").fetch("runtime_evidence")
    end

    def summary
      inconsistencies = []
      health = ProjectDoctor.run(@adapter_path, schema_path: SCHEMA_PATH, control_schema_path: @control_schema_path)
      knowledge = load_knowledge(inconsistencies)
      memory = load_memory(inconsistencies)
      inventories = load_inventories(inconsistencies)
      deliveries = resolve_deliveries(inventories, inconsistencies)
      decisions = resolve_open_decisions(knowledge, memory, inconsistencies)

      {
        "kind" => "dora_project_read_model",
        "version" => 1,
        "project" => {"id" => @adapter.fetch("project"), "name" => knowledge.dig("product_brief", "product") || @adapter.fetch("project"), "adapter" => ".dora/project.yaml"},
        "state" => state_for(health, inconsistencies),
        "health" => safe_health(health),
        "inconsistencies" => inconsistencies,
        "delivery" => deliveries,
        "next_task" => resolve_next_task(inventories, inconsistencies),
        "open_decisions" => decisions,
        "references" => summary_references(knowledge, memory, deliveries)
      }.freeze
    end

    def plan(plan_path)
      plan = load_plan!(plan_path)
      safe_plan(plan, plan_path)
    end

    def task(plan_path, task_id)
      plan = load_plan!(plan_path)
      task = Array(plan["tasks"]).find { |candidate| candidate.is_a?(Hash) && candidate["id"] == task_id }
      fail!("task is not declared: #{task_id}") unless task

      safe_task(task, plan_path)
    end

    def task_evidence(plan_path, task_id)
      plan = load_plan!(plan_path)
      task(plan_path, task_id)
      evidence = Array(plan["evidence"]).find { |candidate| candidate.is_a?(Hash) && candidate["task"] == task_id }
      return {"task" => task_id, "status" => "not_recorded", "reference" => plan_path}.freeze unless evidence

      safe_evidence(evidence, task_id: task_id, plan_path: plan_path)
    end

    private

    def resolve_declared_root!(key)
      relative = @adapter.fetch("paths").fetch(key)
      resolve_under_root!(relative, "declared #{key} root")
    end

    def load_knowledge(inconsistencies)
      ProjectKnowledge::ARTIFACTS.each_value { |path| resolve_under_root!(path, "project knowledge artifact") }
      ProjectKnowledge.validate!(@root)
    rescue ArgumentError => error
      inconsistencies << issue("INVALID", "project_knowledge", "project knowledge is invalid", ["docs/product-brief.yaml", "docs/domain-library.yaml", ".dora/agent-project-profile.yaml"])
      {}
    end

    def load_memory(inconsistencies)
      path = File.join(@docs_root, "project-memory.yaml")
      return nil unless File.file?(path)

      relative = File.join(@adapter.fetch("paths").fetch("docs"), "project-memory.yaml")
      ProjectMemory.load!(resolve_under_root!(relative, "project memory"))
    rescue ArgumentError => error
      inconsistencies << issue("INVALID", "project_memory", "project memory is invalid", [relative_to_root(path)])
      nil
    end

    def load_inventories(inconsistencies)
      inventory_paths = Dir[File.join(@work_root, "**", "*inventory*.yaml")].sort
      inventory_paths.each_with_object([]) do |absolute, inventories|
        contained = resolve_under_root!(relative_to_root(absolute), "execution inventory")
        fail!("execution inventory is outside declared work-plans root") unless contained.start_with?("#{@work_root}/")
        document = YAML.load_file(contained)
        next unless document.is_a?(Hash) && document["kind"] == "execution_inventory" && document["version"].to_i == 1

        inventories << safe_inventory(document, relative_to_root(absolute))
      rescue Psych::Exception, ArgumentError => error
        inconsistencies << issue("INVALID", "execution_inventory", "execution inventory is invalid", [relative_to_root(absolute)])
      end
    end

    def safe_inventory(document, path)
      items = document["items"]
      fail!("execution inventory items are invalid: #{path}") unless items.is_a?(Array)
      normalized = items.map do |item|
        fail!("execution inventory item is invalid: #{path}") unless item.is_a?(Hash) && identifier?(item["id"]) && safe_relative_path?(item["plan"]) && identifier?(item["task"]) && statement?(item["status"])
        item.slice("id", "order", "plan", "task", "status", "started_at", "verified_at", "evidence")
      end
      state = document["state"]
      fail!("execution inventory state is invalid: #{path}") unless state.nil? || statement?(state)

      {"id" => document["id"], "path" => path, "master_plan" => safe_relative_path?(document["master_plan"]) ? document["master_plan"] : nil, "state" => state, "items" => normalized}.compact.freeze
    end

    def resolve_deliveries(inventories, inconsistencies)
      eligible_inventories = inventories.reject { |inventory| non_delivery_inventory?(inventory) }
      active = eligible_inventories.select { |inventory| inventory.fetch("items").any? { |item| item["status"] == "in_progress" } }
      active_delivery = if active.length == 1
                          delivery_summary(active.first, "active", inconsistencies)
                        elsif active.length > 1
                          inconsistencies << issue("WARNING", "active_delivery", "multiple execution inventories contain in-progress work", active.map { |item| item.fetch("path") })
                          {"status" => "ambiguous", "references" => active.map { |item| item.fetch("path") }}
                        end

      verified = eligible_inventories.flat_map do |inventory|
        inventory.fetch("items").each_with_object([]) do |item, entries|
          next unless item["status"] == "verified" && parse_time(item["verified_at"])

          entries << {"inventory" => inventory, "item" => item, "verified_at" => parse_time(item["verified_at"])}
        end
      end
      latest = verified.max_by { |entry| entry.fetch("verified_at") }
      latest_delivery = latest ? delivery_summary(latest.fetch("inventory"), "latest_verified", inconsistencies, latest.fetch("item")) : nil
      {"active" => active_delivery, "latest_verified" => latest_delivery}.compact.freeze
    end

    def delivery_summary(inventory, role, inconsistencies, terminal_item = nil)
      master_path = inventory["master_plan"]
      master = master_path ? load_plan(master_path) : nil
      if master_path && !master
        inconsistencies << issue("WARNING", "delivery_master", "execution inventory references a missing or invalid master plan", [inventory.fetch("path"), master_path])
      end
      selected = terminal_item || inventory.fetch("items").find { |item| item["status"] == "in_progress" }
      {
        "role" => role,
        "id" => master && master["id"],
        "title" => master && master["title"],
        "status" => master && master["status"],
        "master_plan" => master_path,
        "inventory" => inventory.fetch("path"),
        "task" => selected && selected.slice("id", "plan", "task", "status", "verified_at")
      }.compact.freeze
    end

    def resolve_next_task(inventories, inconsistencies)
      eligible_inventories = inventories.reject { |inventory| non_delivery_inventory?(inventory) }
      active = eligible_inventories.select { |inventory| inventory.fetch("items").any? { |item| item["status"] == "in_progress" } }
      return nil if active.length > 1
      candidate = active.first || eligible_inventories.find { |inventory| inventory.fetch("items").any? { |item| item["status"] == "pending" } }
      return nil unless candidate

      AgentNext.next!(project_root: @root, inventory_path: candidate.fetch("path"))
    rescue ArgumentError => error
      inconsistencies << issue("WARNING", "next_task", "next eligible task cannot be resolved", candidate ? [candidate.fetch("path")] : [])
      nil
    end

    def resolve_open_decisions(knowledge, memory, inconsistencies)
      records = Array(knowledge.dig("product_brief", "unanswered_decisions")).map do |statement|
        {"source" => "product_brief", "statement" => statement, "reference" => "docs/product-brief.yaml"}
      end
      memory_records = Array(memory && memory["open_decisions"])
      canonical = records.map { |record| record.fetch("statement") }.sort
      remembered = memory_records.map { |record| record.fetch("statement") }.sort
      if memory && canonical != remembered
        inconsistencies << issue("WARNING", "open_decisions", "project memory and product brief declare different open decisions", ["docs/product-brief.yaml", "docs/project-memory.yaml"])
      end
      records.concat(memory_records.reject { |record| canonical.include?(record.fetch("statement")) }.map { |record| record.slice("id", "statement", "source").merge("source" => "project_memory", "reference" => record.fetch("source")) })
      decision_log_entries(inconsistencies).each do |entry|
        next unless entry["status"] == "proposed"

        records << {"id" => entry.fetch("id"), "source" => "decision_log", "statement" => entry.fetch("decision"), "reference" => entry.fetch("reference")}
      end
      records.uniq { |record| record["statement"] }.freeze
    end

    def decision_log_entries(inconsistencies)
      decision_log_paths.each_with_object([]) do |path, entries|
        next unless File.file?(File.join(@root, path))

        log = DecisionLog.load!(resolve_under_root!(path, "decision log"))
        entries.concat(log.fetch("entries").map { |entry| entry.slice("id", "decision", "status").merge("reference" => path) })
      rescue ArgumentError => error
        inconsistencies << issue("INVALID", "decision_log", "decision log is invalid", [path])
      end
    end

    def decision_log_paths
      [File.join(@adapter.fetch("paths").fetch("docs"), "decision-log.yaml"), ".dora/decision-log.yaml"].select { |path| safe_relative_path?(path) }.uniq
    end

    def summary_references(knowledge, memory, deliveries)
      references = [".dora/project.yaml", "docs/product-brief.yaml", "docs/domain-library.yaml"]
      references << "docs/project-memory.yaml" if memory
      references << deliveries.dig("active", "master_plan") if deliveries.dig("active", "master_plan")
      references << deliveries.dig("latest_verified", "master_plan") if deliveries.dig("latest_verified", "master_plan")
      references.compact.uniq.select { |path| safe_relative_path?(path) }.freeze
    end

    def load_plan(path)
      load_plan!(path)
    rescue ArgumentError
      nil
    end

    def load_plan!(path)
      absolute = resolve_under_root!(path, "work plan")
      fail!("work plan is outside the declared work-plans root: #{path}") unless absolute.start_with?("#{@work_root}/")
      fail!("work plan is missing: #{path}") unless File.file?(absolute)
      document = YAML.load_file(absolute)
      fail!("work plan is invalid: #{path}") unless document.is_a?(Hash) && %w[work master].include?(document["kind"]) && document["version"].to_i == 1
      document
    rescue Psych::Exception => error
      fail!("work plan YAML is invalid: #{error.message}")
    end

    def safe_plan(plan, path)
      tasks = Array(plan["tasks"]).each_with_object([]) do |task, projected|
        projected << safe_task(task, path) if task.is_a?(Hash)
      end
      {"id" => plan["id"], "title" => plan["title"], "kind" => plan["kind"], "status" => plan["status"], "reference" => path, "tasks" => tasks}.freeze
    end

    def safe_task(task, plan_path)
      fail!("work task is invalid: #{plan_path}") unless identifier?(task["id"]) && statement?(task["title"]) && statement?(task["status"])
      {"id" => task.fetch("id"), "title" => task.fetch("title"), "status" => task.fetch("status"), "observable_outcome" => task["observable_outcome"], "dependencies" => Array(task["dependencies"]).select { |item| statement?(item) }, "reference" => plan_path}.compact.freeze
    end

    def safe_evidence(evidence, task_id:, plan_path:)
      result = evidence["result"] == "passed" ? "passed" : evidence["result"] == "failed" ? "failed" : "recorded"
      {"task" => task_id, "status" => result, "verified_at" => evidence["ranAt"], "revision" => sha?(evidence["revision"]) ? evidence["revision"] : nil, "exit_code" => evidence["exitCode"].is_a?(Integer) ? evidence["exitCode"] : nil, "validation" => "declared_leaf_validation", "runtime_evidence" => safe_evidence_paths(evidence["runtimeEvidencePaths"]), "visual_evidence" => safe_evidence_paths(evidence["visualEvidencePaths"]), "reference" => plan_path}.compact.freeze
    end

    def safe_evidence_paths(paths)
      Array(paths).select do |path|
        next false unless safe_relative_path?(path)

        path == @runtime_evidence_root || path.start_with?("#{@runtime_evidence_root}/")
      end
    end

    def non_delivery_inventory?(inventory)
      %w[superseded control_reconciled].include?(inventory["state"])
    end

    def resolve_under_root!(path, label)
      fail!("#{label} must be a safe relative path") unless safe_relative_path?(path)
      absolute = File.expand_path(path, @root)
      fail!("#{label} resolves outside project root") unless absolute.start_with?("#{@root}/")
      return absolute unless File.exist?(absolute)

      resolved = File.realpath(absolute)
      fail!("#{label} resolves outside project root") unless resolved.start_with?("#{@root}/")
      resolved
    end

    def relative_to_root(path)
      File.expand_path(path).delete_prefix("#{@root}/")
    end

    def state_for(health, inconsistencies)
      return "INVALID" if inconsistencies.any? { |item| item["severity"] == "INVALID" }
      return "WARNING" unless health.fetch("healthy")
      return "WARNING" if inconsistencies.any?

      "HEALTHY"
    end

    def safe_health(health)
      {
        "healthy" => health.fetch("healthy"),
        "checks" => Array(health["checks"]).each_with_object([]) do |check, projected|
          projected << check.slice("id", "status") if check.is_a?(Hash) && identifier?(check["id"]) && statement?(check["status"])
        end
      }.freeze
    end


    def issue(severity, code, message, references)
      fail!("inconsistency severity is invalid") unless STATUS.include?(severity)
      {"severity" => severity, "code" => code, "message" => message, "references" => references.select { |path| safe_relative_path?(path) }.uniq}.freeze
    end

    def parse_time(value)
      Time.iso8601(value.to_s).utc
    rescue ArgumentError
      nil
    end

    def identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end

    def statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end

    def safe_relative_path?(path)
      statement?(path) && !path.start_with?("/") && !path.split("/").include?("..")
    end

    def sha?(value)
      value.is_a?(String) && value.match?(/\A[0-9a-f]{7,40}\z/i)
    end

    def fail!(message)
      raise ArgumentError, message
    end
  end
end
