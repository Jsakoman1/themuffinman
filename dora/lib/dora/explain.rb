# frozen_string_literal: true

require_relative "capability_package"
require_relative "project_knowledge"

module Dora
  class Explain
    def self.project!(project_root)
      knowledge = ProjectKnowledge.validate!(project_root)
      brief = knowledge.fetch("product_brief")
      {"kind" => "dora_explanation", "version" => 1, "subject" => "project", "summary" => "#{brief.fetch("product")} serves #{brief.fetch("primary_users").join(", ")} by addressing: #{brief.fetch("user_problem")}", "citations" => ["docs/product-brief.yaml", "docs/domain-library.yaml", ".dora/agent-project-profile.yaml", "AGENTS.md"], "omissions" => brief.fetch("unanswered_decisions"), "completion_boundary" => "This explanation restates declared project knowledge only; it does not prove design completeness, implementation, or release readiness."}.freeze
    end

    def self.capability!(package)
      capability = CapabilityPackage.validate!(package)
      unresolved = capability.fetch("unresolved").map { |item| item.fetch("reason") }
      {"kind" => "dora_explanation", "version" => 1, "subject" => "capability", "summary" => "#{capability.fetch("title")} is declared to address: #{capability.dig("intent", "problem")}", "citations" => [capability.dig("intent", "source_reference"), capability.dig("work", "plan")], "omissions" => unresolved, "completion_boundary" => "This explanation restates declared capability intent and evidence gaps only; it does not prove implementation, runtime acceptance, or release readiness."}.freeze
    end
  end
end
