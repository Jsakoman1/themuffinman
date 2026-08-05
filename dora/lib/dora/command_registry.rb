# frozen_string_literal: true

require "yaml"
require_relative "adapter"

module Dora
  class CommandRegistry
    CORE_COMMANDS = [
      {"usage" => "dora init <destination> --project <project-id>", "purpose" => "Create a declared Dora control structure."},
      {"usage" => "dora bootstrap <destination> --project <project-id> --source <bootstrap-source.yaml> [--starter <starter-id>] [--ci <ci-pack>]", "purpose" => "Copy an explicitly declared local Dora source, optionally apply a technical starter and CI pack, and initialize a project-local launcher."},
      {"usage" => "dora configure <adapter-path> --control <control-id> --from <yaml-path>", "purpose" => "Apply one explicit project-owned control configuration."},
      {"usage" => "dora help [adapter-path]", "purpose" => "List Dora commands and optional project extensions."},
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
      {"usage" => "dora work-verify <adapter-path> plan=<path> [task=<id>]", "purpose" => "Verify one project-owned work item."}
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
