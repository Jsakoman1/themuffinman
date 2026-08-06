# frozen_string_literal: true

require "yaml"
require_relative "adapter"

module Dora
  class CommandRegistry
    CORE_COMMANDS = [
      {"usage" => "dora init <destination> --project <project-id>", "purpose" => "Create a declared Dora control structure."},
      {"usage" => "dora new <destination> --answers <project-new.yaml>", "purpose" => "Create neutral project knowledge and one explicit first work declaration from supplied answers."},
      {"usage" => "dora create-app <destination> --bundle <create-app.yaml> [--format json|yaml]", "purpose" => "Create a new Dora project from a cited idea interview, an explicit first capability, and a reviewed local Dora source."},
      {"usage" => "dora codex-integrate <project-root> [--format json|yaml]", "purpose" => "Install optional project-local Dora navigation for Codex without changing user-owned AGENTS.md instructions."},
      {"usage" => "dora explain <project|capability> <path> [--format json|yaml]", "purpose" => "Explain declared project or capability state in plain language with citations and omissions."},
      {"usage" => "dora next <adapter-path> <execution-inventory-path> [--format json|yaml]", "purpose" => "Return one cited declared safe next action without starting work."},
      {"usage" => "dora guided-next <guided-agent-entrypoint.yaml> [--format json|yaml]", "purpose" => "Return exactly one confirmed interview question or a cited review-only handoff."},
      {"usage" => "dora codex-context <codex-context-packet.yaml> [--format json|yaml]", "purpose" => "Validate and emit one bounded cited Codex task context without starting work."},
      {"usage" => "dora capability-graph <domain-capability-graph.yaml> [--format json|yaml]", "purpose" => "Report declared capability blockers and one safe next capability without completion claims."},
      {"usage" => "dora convention-check <project-convention-profile.yaml> <generated-feature-manifest.yaml> [--format json|yaml]", "purpose" => "Check declared additive output paths against a confirmed project convention profile."},
      {"usage" => "dora proof-packet <capability-proof-matrix.yaml> [--format json|yaml]", "purpose" => "Emit declared proof obligations and approval gates without executing them."},
      {"usage" => "dora diagnose <adapter-path> [--format json|yaml]", "purpose" => "Return declared project blockers and remediation without repairing files or starting work."},
      {"usage" => "dora evidence-explain <revision-evidence-trace.yaml> [--format json|yaml]", "purpose" => "Explain what one declared capability revision is and is not evidenced by, with citations."},
      {"usage" => "dora bootstrap <destination> --project <project-id> --source <bootstrap-source.yaml> [--starter <starter-id>] [--ci <ci-pack>]", "purpose" => "Copy an explicitly declared local Dora source, optionally apply a technical starter and CI pack, and initialize a project-local launcher."},
      {"usage" => "dora upgrade-apply <consumer-root> --source <bootstrap-source.yaml> --approval <approval-record.yaml>", "purpose" => "Apply a reviewed local Dora upgrade only with an explicit scoped approval and backup."},
      {"usage" => "dora upgrade-rollback <consumer-root> --backup <recorded-backup-path> --approval <approval-record.yaml>", "purpose" => "Restore a recorded local Dora package backup only with explicit rollback approval."},
      {"usage" => "dora configure <adapter-path> --control <control-id> --from <yaml-path>", "purpose" => "Apply one explicit project-owned control configuration."},
      {"usage" => "dora help [adapter-path] [--format json|yaml]", "purpose" => "List Dora commands and optional project extensions; an explicit format returns the stable command envelope."},
      {"usage" => "dora readiness <project-root> [--initialize-git] [--format json|yaml]", "purpose" => "Report Git baseline readiness; initialization is an explicit local mutation."},
      {"usage" => "dora memory-refresh <project-root> [--format json|yaml]", "purpose" => "Propose review of declared memory drift without overwriting project-owned knowledge."},
      {"usage" => "dora capability-trace <trace-path> [--format json|yaml]", "purpose" => "Read one declared capability trace with citations and without inferring completion."},
      {"usage" => "dora lease-inspect <lease-registry-path> [--format json|yaml]", "purpose" => "Read advisory local task lease records without changing project work."},
      {"usage" => "dora lease-acquire <lease-registry-path> <work-plan> <task> <holder> --expires-at <ISO-8601>", "purpose" => "Record one advisory local task lease; it does not start work or grant authority."},
      {"usage" => "dora lease-handoff <lease-registry-path> <lease-id> <holder> --reason <reason>", "purpose" => "Record one explicit advisory task handoff without changing work status or approval authority."},
      {"usage" => "dora doctor <adapter-path>", "purpose" => "Diagnose declared project paths, commands, and controls."},
      {"usage" => "dora tools <adapter-path>", "purpose" => "List the project-configured tool catalog."},
      {"usage" => "dora route <adapter-path> <changed-path>...", "purpose" => "Select configured validation commands for changed paths."},
      {"usage" => "dora search <adapter-path> <query>", "purpose" => "Search only project-configured context roots."},
      {"usage" => "dora inventory <adapter-path> <path>...", "purpose" => "Classify paths through the project workspace inventory."},
      {"usage" => "dora evidence <adapter-path>", "purpose" => "Review project-declared documentation evidence."},
      {"usage" => "dora impact <adapter-path> <node-id>...", "purpose" => "Find configured System Map relationships for changed nodes."},
      {"usage" => "dora retention <adapter-path> --config <path>", "purpose" => "Classify generated files through an explicit retention policy."},
      {"usage" => "dora cleanup-dry-run <adapter-path> --config <path>", "purpose" => "List explicit cleanup targets without deleting files."},
      {"usage" => "dora cleanup <adapter-path> --config <path> --approve <path>...", "purpose" => "Delete only the exact targets previously reported by dry-run."},
      {"usage" => "dora templates <adapter-path> --config <path>", "purpose" => "Check configured template references."},
      {"usage" => "dora repository-map <adapter-path> --config <path>", "purpose" => "Emit a configured source and relationship map."},
      {"usage" => "dora validate-adapter <adapter-path>", "purpose" => "Validate a project adapter."},
      {"usage" => "dora validate-work-plan <adapter-path> <work-plan-path>", "purpose" => "Validate a project work plan."},
      {"usage" => "dora plan-contract <work-plan-path>", "purpose" => "Validate reusable atomic task fields without project assumptions."},
      {"usage" => "dora plugin-contract <plugin-manifest-path>", "purpose" => "Validate declared optional plugin roots, inputs, and report contract."},
      {"usage" => "dora plugin-run <plugin-manifest-path> <plugin-id>", "purpose" => "Execute one declared local Ruby plugin without shell interpolation."},
      {"usage" => "dora work-start <adapter-path> plan=<path> task=<id>", "purpose" => "Start one project-owned work item."},
      {"usage" => "dora work-verify <adapter-path> plan=<path> [task=<id>]", "purpose" => "Verify one project-owned work item."},
      {"usage" => "dora agent-context <adapter-path> <work-plan-path> <task-id>", "purpose" => "Emit cited declared context for one bounded task without inferring completion."},
      {"usage" => "dora agent-session <adapter-path> <execution-inventory-path> <work-plan-path> <task-id> [--format json|yaml]", "purpose" => "Emit one bounded read-only Codex session with declared work, health, tools, gaps, decisions, and approval boundary."},
      {"usage" => "dora agent-next <adapter-path> <execution-inventory-path>", "purpose" => "Select the next declared pending work item without starting it."},
      {"usage" => "dora status <adapter-path> <execution-inventory-path>", "purpose" => "Report declared project health, open decisions, and evidence gaps without a completion claim."},
      {"usage" => "dora findings-export <plugin-report.json>", "purpose" => "Convert standard plugin findings into read-only portable annotations."},
      {"usage" => "dora agent-closeout <adapter-path> <work-plan-path> <task-id> <change-impact-path> <changed-path>...", "purpose" => "Report declared closeout gaps for one task without recording verification or approval."}
    ].freeze

    def self.list(adapter_path: nil, schema_path:)
      extensions = adapter_path ? extensions_for(adapter_path, schema_path) : []
      {"commands" => CORE_COMMANDS, "extensions" => extensions}
    end

    def self.render(adapter_path: nil, schema_path:)
      registry = list(adapter_path: adapter_path, schema_path: schema_path)
      lines = ["Dora commands:"]
      registry.fetch("commands").each { |command| lines << "  #{command.fetch("usage")} — #{command.fetch("purpose")}" }
      unless registry.fetch("extensions").empty?
        lines << "Declared project extensions:"
        registry.fetch("extensions").each { |extension| lines << "  #{extension.fetch("id")} (#{extension.fetch("category")}): #{extension.fetch("invocation")}" }
      end
      lines.join("\n")
    end

    def self.extensions_for(adapter_path, schema_path)
      Adapter.validate!(adapter_path, schema_path)
      adapter = YAML.load_file(adapter_path)
      adapter.fetch("extensions").map do |extension|
        extension.slice("id", "category", "invocation")
      end
    end
    private_class_method :extensions_for
  end
end
