# frozen_string_literal: true

require_relative "vertical_slice_proposal"

module Dora
  class VerticalSliceGenerator
    REQUIRED_DECISIONS = {"data_safety" => "data-safety", "workflow" => "workflow", "permission" => "permission", "technical" => "technical"}.freeze

    def self.generate!(context)
      fail!("confirmed capability context is invalid") unless context.is_a?(Hash) && context["kind"] == "dora_confirmed_capability_context" && context["version"].to_i == 1
      capability = context["capability"]
      fail!("confirmed capability context must contain one confirmed capability") unless capability.is_a?(Hash) && identifier?(capability["id"]) && statement?(capability["title"]) && capability["confirmed"] == true

      id = capability.fetch("id")
      proposal = {
        "kind" => "dora_vertical_slice_proposal",
        "version" => 1,
        "capability" => capability.slice("id", "title", "confirmed"),
        "surfaces" => proposed_surfaces(id),
        "atomic_work" => proposed_work(id, capability.fetch("title")),
        "gaps" => decision_gaps(context["decisions"])
      }
      VerticalSliceProposal.validate!(proposal)
    end

    def self.generate_report!(context)
      proposal = generate!(context)
      {"kind" => "dora_vertical_slice_report", "version" => 1, "proposal" => proposal, "completion_boundary" => "A vertical-slice report proposes declared technical surfaces only; it does not create source code, a work plan, or implementation evidence."}.freeze
    end

    def self.proposed_surfaces(id)
      {
        "migration" => ["backend/src/main/resources/db/migration/V__#{id.tr('-', '_')}.sql"],
        "backend" => ["backend/src/main/java/<product-package>/#{id}/"],
        "api" => ["backend/src/main/java/<product-package>/#{id}/controller/"],
        "frontend" => ["frontend/src/features/#{id}/"],
        "tests" => ["backend/src/test/java/<product-package>/#{id}/"],
        "runtime_evidence" => ["docs/runtime-evidence/#{id}.json"],
        "documentation" => ["docs/capabilities/#{id}.md"]
      }
    end
    private_class_method :proposed_surfaces

    def self.proposed_work(id, title)
      {"plan" => "docs/work/#{id}.yaml", "task" => {"id" => "implement-#{id}", "title" => "Implement #{title}", "observable_outcome" => "One confirmed capability is prepared for project-owned implementation review.", "required_paths" => ["docs/capabilities/#{id}.md"], "validation" => "project-owned leaf validation", "evidence_boundary" => ["project-owned implementation evidence"]}}
    end
    private_class_method :proposed_work

    def self.decision_gaps(decisions)
      decision_map = decisions.is_a?(Hash) ? decisions : {}
      REQUIRED_DECISIONS.map { |field, label| "Missing confirmed #{label} decision." unless statement?(decision_map[field]) }.compact
    end
    private_class_method :decision_gaps

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
