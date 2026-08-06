# frozen_string_literal: true

require "yaml"

module Dora
  class DomainCapabilityGraph
    SCHEMA_PATH = File.expand_path("../../domain-capability-graph.schema.yaml", __dir__)

    def self.report!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("capability graph schema is invalid") unless schema["kind"] == "dora_domain_capability_graph_schema" && schema["version"].to_i == 1
      fail!("capability graph must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_domain_capability_graph" && document["version"].to_i == 1
      required!(document, schema.fetch("required_fields"), "capability graph")
      decisions = validate_decisions!(document.fetch("decisions"), schema)
      capabilities = validate_capabilities!(document.fetch("capabilities"), schema, decisions)
      detect_cycles!(capabilities)
      states = capabilities.map { |capability| state_for(capability, decisions, capabilities) }
      next_capability = states.find { |state| state.fetch("status") == "ready_to_plan" }
      {"kind" => "dora_domain_capability_graph_report", "version" => 1, "capabilities" => states, "blocking_gaps" => states.flat_map { |state| state.fetch("blocking_gaps") }, "next_safe_capability" => next_capability && next_capability.slice("id", "title"), "completion_boundary" => "A capability graph reports declared ordering and gaps only; it does not create work, infer a decision, start implementation, or prove completion."}.freeze
    rescue Psych::Exception => error
      fail!("capability graph YAML is invalid: #{error.message}")
    end

    def self.required!(document, fields, label)
      missing = fields.reject { |field| document.key?(field) }
      fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
    end
    private_class_method :required!

    def self.validate_decisions!(decisions, schema)
      fail!("capability graph decisions must be a list") unless decisions.is_a?(Array)
      rows = decisions.map do |decision|
        fail!("capability graph decision must be a mapping") unless decision.is_a?(Hash)
        required!(decision, schema.fetch("decision_required_fields"), "capability graph decision")
        fail!("capability graph decision id is invalid") unless identifier?(decision.fetch("id"))
        fail!("capability graph decision status is invalid") unless schema.fetch("decision_statuses").include?(decision.fetch("status"))
        decision.slice(*schema.fetch("decision_required_fields"))
      end
      ids = rows.map { |row| row.fetch("id") }
      fail!("capability graph decision ids must be unique") unless ids.uniq.length == ids.length
      rows.to_h { |row| [row.fetch("id"), row.fetch("status")] }.freeze
    end
    private_class_method :validate_decisions!

    def self.validate_capabilities!(capabilities, schema, decisions)
      fail!("capability graph capabilities must be a non-empty list") unless capabilities.is_a?(Array) && !capabilities.empty?
      rows = capabilities.map do |capability|
        fail!("capability graph capability must be a mapping") unless capability.is_a?(Hash)
        required!(capability, schema.fetch("capability_required_fields"), "capability graph capability")
        fail!("capability graph capability id is invalid") unless identifier?(capability.fetch("id"))
        fail!("capability graph capability title is invalid") unless statement?(capability.fetch("title"))
        fail!("capability graph capability confirmed must be boolean") unless [true, false].include?(capability.fetch("confirmed"))
        %w[dependencies required_decisions].each do |field|
          values = capability.fetch(field)
          fail!("capability graph capability #{field} must be a list") unless values.is_a?(Array) && values.all? { |value| identifier?(value) } && values.uniq.length == values.length
        end
        capability.slice(*schema.fetch("capability_required_fields"))
      end
      ids = rows.map { |row| row.fetch("id") }
      fail!("capability graph capability ids must be unique") unless ids.uniq.length == ids.length
      rows.each do |row|
        missing_dependencies = row.fetch("dependencies") - ids
        fail!("capability graph has missing dependency #{missing_dependencies.join(", ")}") unless missing_dependencies.empty?
        missing_decisions = row.fetch("required_decisions") - decisions.keys
        fail!("capability graph has missing decision #{missing_decisions.join(", ")}") unless missing_decisions.empty?
      end
      rows.freeze
    end
    private_class_method :validate_capabilities!

    def self.detect_cycles!(capabilities)
      by_id = capabilities.to_h { |capability| [capability.fetch("id"), capability] }
      visiting = {}; visited = {}
      visit = lambda do |id|
        fail!("capability graph has dependency cycle at #{id}") if visiting[id]
        return if visited[id]
        visiting[id] = true
        by_id.fetch(id).fetch("dependencies").each { |dependency| visit.call(dependency) }
        visiting.delete(id); visited[id] = true
      end
      by_id.keys.each { |id| visit.call(id) }
    end
    private_class_method :detect_cycles!

    def self.state_for(capability, decisions, capabilities)
      gaps = []
      gaps << gap(capability, "capability_not_confirmed", "Capability is not confirmed.") unless capability.fetch("confirmed")
      capability.fetch("required_decisions").each { |id| gaps << gap(capability, "open_decision", "Decision #{id} is open.") unless decisions.fetch(id) == "confirmed" }
      capability.fetch("dependencies").each do |id|
        dependency = capabilities.find { |candidate| candidate.fetch("id") == id }
        gaps << gap(capability, "prerequisite_not_completed", "Prerequisite capability #{id} must be implemented and evidenced by the consumer project.") unless dependency
      end
      status = gaps.empty? ? "ready_to_plan" : "blocked"
      {"id" => capability.fetch("id"), "title" => capability.fetch("title"), "status" => status, "blocking_gaps" => gaps}.freeze
    end
    private_class_method :state_for

    def self.gap(capability, category, message)
      {"capability_id" => capability.fetch("id"), "category" => category, "message" => message}
    end
    private_class_method :gap

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
