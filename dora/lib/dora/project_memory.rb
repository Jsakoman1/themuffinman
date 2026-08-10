# frozen_string_literal: true

require "yaml"
require_relative "project_knowledge"

module Dora
  class ProjectMemory
    REQUIRED_FIELDS = %w[project_intent canonical_knowledge open_decisions capability_intent current_work].freeze
    CURRENT_WORK_STATES = %w[planned active blocked none].freeze
    NAVIGABLE_INVENTORY_STATES = %w[pending in_progress blocked].freeze

    def self.load!(path)
      fail!("project memory file is missing") unless File.file?(path)

      validate!(YAML.load_file(path))
    end

    def self.validate!(document)
      fail!("project memory must be a mapping") unless document.is_a?(Hash)
      fail!("project memory kind is invalid") unless document["kind"] == "dora_project_memory" && document["version"].to_i == 1
      missing = REQUIRED_FIELDS.reject { |field| document.key?(field) }
      fail!("project memory is missing #{missing.join(", ")}") unless missing.empty?

      project_intent = validate_project_intent!(document.fetch("project_intent"))
      knowledge = validate_knowledge!(document.fetch("canonical_knowledge"))
      decisions = validate_decisions!(document.fetch("open_decisions"))
      capabilities = validate_capabilities!(document.fetch("capability_intent"), knowledge)
      current_work = validate_current_work!(document.fetch("current_work"))

      {
        "kind" => "dora_project_memory",
        "version" => 1,
        "project_intent" => project_intent,
        "canonical_knowledge" => knowledge,
        "open_decisions" => decisions,
        "capability_intent" => capabilities,
        "current_work" => current_work,
        "completion_boundary" => "Project memory is declared navigation context only and does not prove implementation, runtime acceptance, or release readiness."
      }.freeze
    end

    def self.drift!(project_root:, memory_path: "docs/project-memory.yaml")
      root = File.expand_path(project_root)
      path = File.expand_path(memory_path, root)
      fail!("project memory path is invalid") unless path.start_with?("#{root}/")
      memory = load!(path)
      knowledge = ProjectKnowledge.validate!(root)
      differences = []
      memory.fetch("canonical_knowledge").each { |entry| differences << {"kind" => "missing_knowledge_path", "id" => entry.fetch("id"), "path" => entry.fetch("path")} unless File.file?(File.join(root, entry.fetch("path"))) }
      declared = memory.fetch("open_decisions").map { |entry| entry.fetch("statement") }.sort
      canonical = knowledge.dig("product_brief", "unanswered_decisions").sort
      differences << {"kind" => "open_decisions", "memory" => declared, "canonical" => canonical} unless declared == canonical
      unless memory.dig("current_work", "state") == "none"
        work_path = File.join(root, memory.dig("current_work", "plan"))
        differences << {"kind" => "missing_work_plan", "path" => memory.dig("current_work", "plan")} unless File.file?(work_path)
      end
      {"kind" => "dora_project_memory_drift", "version" => 1, "drifted" => !differences.empty?, "differences" => differences, "memory_path" => memory_path, "mutation" => "none", "completion_boundary" => "Drift is diagnostic only; Dora does not overwrite product-owned memory or infer completion."}.freeze
    end

    # Project memory is navigation, while execution inventories are the authoritative
    # declaration of work state. Keep the two aligned without creating another status
    # store or inferring a current task from historical verification records.
    def self.validate_work_navigation!(memory:, inventories:)
      current_work = validate_current_work!(memory.fetch("current_work"))
      expected = expected_current_work!(inventories)
      return expected if current_work == expected

      if expected.fetch("state") == "none"
        fail!("project memory current_work is stale: no authoritative planned, active, or blocked task remains")
      end
      if current_work.fetch("state") == "none"
        fail!("project memory current_work is missing: authoritative #{expected.fetch("state")} work is declared")
      end

      fail!("project memory current_work is contradictory: it does not match authoritative #{expected.fetch("state")} work")
    end

    def self.expected_current_work!(inventories)
      candidates = Array(inventories).flat_map do |inventory|
        next [] unless inventory.is_a?(Hash)

        Array(inventory["items"]).each_with_object([]) do |item, navigable|
          next unless item.is_a?(Hash) && NAVIGABLE_INVENTORY_STATES.include?(item["status"])

          navigable << navigation_candidate!(item)
        end
      end
      active_or_blocked = candidates.reject { |candidate| candidate.fetch("state") == "planned" }
      return unambiguous_current_work!(active_or_blocked, "active or blocked") unless active_or_blocked.empty?
      return {"state" => "none"} if candidates.empty?

      planned_current_work!(candidates)
    end

    def self.navigation_candidate!(item)
      state = {"pending" => "planned", "in_progress" => "active", "blocked" => "blocked"}.fetch(item.fetch("status"))
      fail!("execution inventory navigation plan is invalid") unless safe_relative_path?(item["plan"])
      fail!("execution inventory navigation task is invalid") unless identifier?(item["task"])

      {"plan" => item.fetch("plan"), "task" => item.fetch("task"), "state" => state, "order" => item["order"]}
    end
    private_class_method :navigation_candidate!

    def self.unambiguous_current_work!(candidates, label)
      fail!("project memory current_work is ambiguous: multiple authoritative #{label} tasks are declared") unless candidates.length == 1

      candidates.first.slice("plan", "task", "state")
    end
    private_class_method :unambiguous_current_work!

    def self.planned_current_work!(candidates)
      return candidates.first.slice("plan", "task", "state") if candidates.length == 1

      fail!("project memory current_work is ambiguous: planned tasks require unique numeric inventory order") unless candidates.all? { |candidate| candidate["order"].is_a?(Integer) }
      ordered = candidates.sort_by { |candidate| candidate.fetch("order") }
      fail!("project memory current_work is ambiguous: planned tasks have duplicate inventory order") if ordered.each_cons(2).any? { |left, right| left.fetch("order") == right.fetch("order") }

      ordered.first.slice("plan", "task", "state")
    end
    private_class_method :planned_current_work!

    def self.validate_project_intent!(intent)
      fail!("project memory project_intent must be a mapping") unless intent.is_a?(Hash)
      %w[product_brief domain_library].each do |field|
        fail!("project memory project_intent #{field} is invalid") unless safe_relative_path?(intent[field])
      end

      intent.slice("product_brief", "domain_library")
    end
    private_class_method :validate_project_intent!

    def self.validate_knowledge!(entries)
      fail!("project memory canonical_knowledge must be a non-empty list") unless entries.is_a?(Array) && !entries.empty?

      entries.map do |entry|
        fail!("project memory knowledge entry must be a mapping") unless entry.is_a?(Hash)
        fail!("project memory knowledge id is invalid") unless identifier?(entry["id"])
        fail!("project memory knowledge path is invalid") unless safe_relative_path?(entry["path"])
        fail!("project memory knowledge purpose is invalid") unless statement?(entry["purpose"])

        entry.slice("id", "path", "purpose")
      end
    end
    private_class_method :validate_knowledge!

    def self.validate_decisions!(entries)
      fail!("project memory open_decisions must be a list") unless entries.is_a?(Array)

      entries.map do |entry|
        fail!("project memory open decision must be a mapping") unless entry.is_a?(Hash)
        fail!("project memory open decision id is invalid") unless identifier?(entry["id"])
        fail!("project memory open decision statement is invalid") unless statement?(entry["statement"])
        fail!("project memory open decision source is invalid") unless safe_relative_path?(entry["source"])

        entry.slice("id", "statement", "source")
      end
    end
    private_class_method :validate_decisions!

    def self.validate_capabilities!(entries, knowledge)
      fail!("project memory capability_intent must be a list") unless entries.is_a?(Array)
      knowledge_ids = knowledge.map { |entry| entry.fetch("id") }

      entries.map do |entry|
        fail!("project memory capability intent must be a mapping") unless entry.is_a?(Hash)
        fail!("project memory capability id is invalid") unless identifier?(entry["id"])
        fail!("project memory capability intended_outcome is invalid") unless statement?(entry["intended_outcome"])
        references = entry["knowledge_references"]
        fail!("project memory capability knowledge_references are invalid") unless references.is_a?(Array) && !references.empty? && references.all? { |reference| knowledge_ids.include?(reference) }

        entry.slice("id", "intended_outcome").merge("knowledge_references" => references)
      end
    end
    private_class_method :validate_capabilities!

    def self.validate_current_work!(work)
      fail!("project memory current_work must be a mapping") unless work.is_a?(Hash)
      state = work["state"]
      fail!("project memory current_work state is invalid") unless CURRENT_WORK_STATES.include?(state)
      if state == "none"
        extra = work.reject { |key, value| key == "state" || value.nil? }
        fail!("project memory current_work none must not declare a plan or task") unless extra.empty?

        return {"state" => "none"}
      end
      fail!("project memory current_work plan is invalid") unless safe_relative_path?(work["plan"])
      fail!("project memory current_work task is invalid") unless identifier?(work["task"])

      work.slice("plan", "task", "state")
    end
    private_class_method :validate_current_work!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.safe_relative_path?(path)
      statement?(path) && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
