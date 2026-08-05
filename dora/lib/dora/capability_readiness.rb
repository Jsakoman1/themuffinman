# frozen_string_literal: true

require_relative "domain_compiler"

module Dora
  class CapabilityReadiness
    def self.report!(capability:, domain:)
      compilation = DomainCompiler.compile!(capability: capability, domain: domain)
      gaps = compilation.fetch("findings").map { |finding| finding.fetch("message") }
      ready = gaps.empty?
      {"kind" => "dora_capability_readiness", "version" => 1, "capability_id" => compilation.fetch("capability_id"), "ready_to_implement" => ready, "blocking_gaps" => gaps, "recommended_next_action" => ready ? declared_work_action(capability) : "Resolve declared gap: #{gaps.first}", "compilation" => compilation, "completion_boundary" => "Capability readiness reports declared implementation gaps only; it is not a completion, runtime acceptance, or release claim."}.freeze
    end

    def self.declared_work_action(capability)
      package = CapabilityPackage.validate!(capability)
      "Start declared work task #{package.dig("work", "task")} in #{package.dig("work", "plan")} only through the project work-start command."
    end
    private_class_method :declared_work_action
  end
end
