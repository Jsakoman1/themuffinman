# frozen_string_literal: true

require "json"

require_relative "project_registry"
require_relative "../../../lib/dora/handoff"
require_relative "../../../lib/dora/idc_triage"

module DoraBridge
  # JSON-RPC framing for the small, read-only MCP surface. This contains no Dora
  # project semantics: it authorizes a configured public ID, then delegates to
  # Dora::ProjectReadModel.
  class Server
    PROTOCOL_VERSION = "2025-06-18"
    INTENT_PLAN_TOOL = "align_intent_plan"
    IDC_ENVELOPE_TOOL = "get_idc_envelope"
    IDC_TRIAGE_TOOL = "evaluate_idc_triage"
    IDC_ENVELOPE_SELECTION = {
      "kind" => Dora::IdcEnvelope::SELECTION_KIND,
      "version" => 1,
      "project_fields" => %w[project state health integrity delivery current_goal next_task open_decisions references],
      "decision_ids" => [],
      "artifact_references" => []
    }.freeze
    TOOL_DEFINITIONS = [
      ["list_projects", "List the bridge's explicitly allowed Dora projects.", {}],
      ["get_project_summary", "Get a compact, sanitized current Dora project summary.", {"project" => {"type" => "string"}}],
      ["get_project_health", "Get sanitized Dora project health and inconsistencies.", {"project" => {"type" => "string"}}],
      ["get_current_delivery", "Get active and latest verified Dora delivery state.", {"project" => {"type" => "string"}}],
      ["get_next_task", "Get Dora's next eligible task, if it resolves unambiguously.", {"project" => {"type" => "string"}}],
      ["get_open_decisions", "Get declared unresolved product decisions.", {"project" => {"type" => "string"}}],
      ["get_plan", "Get a sanitized declared Dora work plan.", {"project" => {"type" => "string"}, "plan" => {"type" => "string"}}],
      ["get_task_evidence", "Get a sanitized verification summary for a declared task.", {"project" => {"type" => "string"}, "plan" => {"type" => "string"}, "task" => {"type" => "string"}}],
      [IDC_ENVELOPE_TOOL, "Get the fixed, sanitized, read-only Dora envelope approved for an IDC advisory request.", {"project" => {"type" => "string", "maxLength" => 80}}],
      [IDC_TRIAGE_TOOL, "Evaluate one bounded IDC triage request and return only a transient advisory local-next-action readback. It never renders or starts IDC.", {"project" => {"type" => "string", "maxLength" => 80}, "triage_request" => {"type" => "object"}}],
      [INTENT_PLAN_TOOL, "Evaluate one bounded non-canonical ChatGPT Intent Plan against current Dora state. It never persists a proposal or creates work.", {"project" => {"type" => "string", "maxLength" => 80}, "proposal" => {"type" => "object"}}]
    ].freeze
    HANDOFF_TOOL_DEFINITIONS = [
      ["create_handoff", "Create an owner-requested, structured implementation handoff for Codex. This only queues validated work; it never edits files, runs commands, or starts Codex.", {"project" => {"type" => "string", "maxLength" => 80}, "title" => {"type" => "string", "maxLength" => 200}, "objective" => {"type" => "string", "maxLength" => 8000}, "acceptance_criteria" => {"type" => "array", "maxItems" => 20, "items" => {"type" => "string", "maxLength" => 1000}}, "constraints" => {"type" => "array", "maxItems" => 20, "items" => {"type" => "string", "maxLength" => 1000}}, "references" => {"type" => "array", "maxItems" => 20, "items" => {"type" => "string", "maxLength" => 300}}, "brief" => {"type" => "object"}, "client_request_id" => {"type" => "string", "maxLength" => 128}, "supersedes" => {"type" => "string", "maxLength" => 64}, "follows_up" => {"type" => "string", "maxLength" => 64}}],
      ["list_handoffs", "List sanitized handoffs for one handoff-enabled project.", {"project" => {"type" => "string", "maxLength" => 80}}],
      ["get_handoff", "Get one sanitized structured handoff by ID.", {"project" => {"type" => "string", "maxLength" => 80}, "handoff_id" => {"type" => "string", "maxLength" => 64}}],
      ["get_next_handoff", "Get the deterministically oldest READY handoff for one handoff-enabled project.", {"project" => {"type" => "string", "maxLength" => 80}}],
      ["get_handoff_status", "Get a sanitized handoff lifecycle status, current collaboration milestone, owner-decision block, and Dora evidence linkage.", {"project" => {"type" => "string", "maxLength" => 80}, "handoff_id" => {"type" => "string", "maxLength" => 64}}],
      ["get_handoff_lifecycle_readback", "Get one deterministic lifecycle-only handoff readback without immutable handoff content or raw lifecycle events.", {"project" => {"type" => "string", "maxLength" => 80}, "handoff_id" => {"type" => "string", "maxLength" => 64}}]
    ].freeze

    def initialize(registry, handoff_store: nil)
      @registry = registry
      @handoff_store = handoff_store
    end

    def run(input: $stdin, output: $stdout)
      input.each_line do |line|
        request = JSON.parse(line)
        response = handle(request)
        output.puts(JSON.generate(response)) if response
        output.flush
      rescue JSON::ParserError
        output.puts(JSON.generate(error_response(nil, -32700, "invalid JSON-RPC request")))
        output.flush
      end
    end

    def handle(request)
      return error_response(nil, -32600, "invalid JSON-RPC request") unless request.is_a?(Hash) && request["jsonrpc"] == "2.0" && request["method"].is_a?(String)

      result = case request.fetch("method")
               when "initialize" then initialization
               when "notifications/initialized" then return nil
               when "tools/list" then {"tools" => tools}
               when "tools/call" then call_tool(request["params"])
               else raise ProtocolError.new(-32601, "unsupported read-only MCP operation")
               end
      request.key?("id") ? {"jsonrpc" => "2.0", "id" => request["id"], "result" => result} : nil
    rescue ProtocolError => error
      error_response(request && request["id"], error.code, error.message)
    rescue ArgumentError
      error_response(request && request["id"], -32001, "bridge request could not be authorized or resolved")
    end

    private

    class ProtocolError < StandardError
      attr_reader :code

      def initialize(code, message)
        @code = code
        super(message)
      end
    end

    def initialization
      instructions = if @handoff_store
                       "Dora Bridge V2 permits only owner-requested structured handoff creation for explicitly handoff-enabled projects. It never grants shell, filesystem, patch, git, source-editing, process-execution, or Codex-start authority."
                     else
                       "Dora Bridge is read-only. Use only declared project IDs and project-relative artifact references."
                     end
      {"protocolVersion" => PROTOCOL_VERSION, "capabilities" => {"tools" => {"listChanged" => false}}, "serverInfo" => {"name" => "dora-bridge", "version" => @handoff_store ? "2.0.0" : "1.0.0"}, "instructions" => instructions}
    end

    def tools
      (TOOL_DEFINITIONS + (@handoff_store ? HANDOFF_TOOL_DEFINITIONS : [])).map do |name, description, properties|
        required = name == "create_handoff" ? %w[project title objective acceptance_criteria constraints references brief client_request_id] : properties.keys
        definition = {"name" => name, "description" => description, "inputSchema" => {"type" => "object", "properties" => properties, "required" => required, "additionalProperties" => false}, "annotations" => {"readOnlyHint" => name != "create_handoff", "destructiveHint" => false, "openWorldHint" => false}}
        definition["outputSchema"] = {"type" => "object"} if HANDOFF_TOOL_DEFINITIONS.any? { |candidate| candidate.first == name }
        definition
      end
    end

    def call_tool(params)
      fail ProtocolError.new(-32602, "invalid read-only tool request") unless params.is_a?(Hash) && params["name"].is_a?(String)

      arguments = params["arguments"] || {}
      fail ProtocolError.new(-32602, "invalid read-only tool arguments") unless arguments.is_a?(Hash)
      name = params.fetch("name")
      value = case name
              when "list_projects" then {"projects" => @registry.list}
              when "get_project_summary" then summary_for(arguments)
              when "get_project_health" then summary_for(arguments).slice("project", "state", "health", "inconsistencies")
              when "get_current_delivery" then summary_for(arguments).slice("project", "state", "delivery", "references")
              when "get_next_task" then summary_for(arguments).slice("project", "state", "next_task", "inconsistencies")
              when "get_open_decisions" then summary_for(arguments).slice("project", "state", "open_decisions", "inconsistencies")
              when "get_plan" then model_for(arguments).plan(required_argument!(arguments, "plan"))
              when "get_task_evidence" then model_for(arguments).task_evidence(required_argument!(arguments, "plan"), required_argument!(arguments, "task"))
              when IDC_ENVELOPE_TOOL then idc_envelope_for(arguments)
              when IDC_TRIAGE_TOOL then idc_triage_for(arguments)
              when INTENT_PLAN_TOOL then align_intent_plan(arguments)
              when "create_handoff" then create_handoff(arguments)
              when "list_handoffs" then {"handoffs" => handoff_store!.list(project: handoff_project!(arguments))}
              when "get_handoff" then handoff_store!.get!(id: required_argument!(arguments, "handoff_id"), project: handoff_project!(arguments))
              when "get_next_handoff" then {"handoff" => handoff_store!.next_ready!(project: handoff_project!(arguments))}
              when "get_handoff_status" then handoff_store!.status_readback!(id: required_argument!(arguments, "handoff_id"), project: handoff_project!(arguments))
              when "get_handoff_lifecycle_readback" then lifecycle_readback(arguments)
              else fail ProtocolError.new(-32601, "unsupported read-only Dora tool")
              end
      {"content" => [{"type" => "text", "text" => JSON.generate(value)}], "structuredContent" => value}
    end

    def summary_for(arguments)
      model_for(arguments).summary
    end

    def model_for(arguments)
      @registry.read_model!(required_argument!(arguments, "project"))
    end

    def required_argument!(arguments, key)
      value = arguments[key]
      fail ProtocolError.new(-32602, "invalid read-only tool arguments") unless value.is_a?(String) && !value.empty?

      value
    end

    def create_handoff(arguments)
      require_exact_arguments!(arguments, %w[project title objective acceptance_criteria constraints references brief client_request_id], %w[supersedes follows_up])
      project = handoff_project!(arguments)
      handoff_store!.create!(project: project, title: arguments.fetch("title"), objective: arguments.fetch("objective"), acceptance_criteria: arguments.fetch("acceptance_criteria"), constraints: arguments.fetch("constraints"), references: arguments.fetch("references"), brief: arguments.fetch("brief"), created_by: "chatgpt", client_request_id: arguments.fetch("client_request_id"), supersedes: arguments["supersedes"], follows_up: arguments["follows_up"])
    end

    def align_intent_plan(arguments)
      require_exact_arguments!(arguments, %w[project proposal], [])
      proposal = arguments.fetch("proposal")
      fail ProtocolError.new(-32602, "invalid Intent Plan proposal") unless proposal.is_a?(Hash)

      model_for(arguments).align_intent_plan(proposal)
    end

    def idc_envelope_for(arguments)
      require_exact_arguments!(arguments, %w[project], [])
      @registry.idc_envelope!(arguments.fetch("project"), selection: IDC_ENVELOPE_SELECTION)
    end

    def idc_triage_for(arguments)
      require_exact_arguments!(arguments, %w[project triage_request], [])
      fail ProtocolError.new(-32602, "invalid IDC triage request") unless arguments.fetch("triage_request").is_a?(Hash)

      model_for(arguments)
      Dora::IdcTriage.evaluate!(request: arguments.fetch("triage_request"))
    end

    def handoff_project!(arguments)
      project = required_argument!(arguments, "project")
      @registry.handoff_authorized!(project)
      project
    end

    def lifecycle_readback(arguments)
      return unavailable_lifecycle_readback unless arguments.keys.sort == %w[handoff_id project] && arguments.values.all? { |value| value.is_a?(String) && !value.empty? }

      handoff_store!.lifecycle_readback!(id: arguments.fetch("handoff_id"), project: handoff_project!(arguments))
    rescue ArgumentError
      unavailable_lifecycle_readback
    end

    def unavailable_lifecycle_readback
      {
        "status" => "UNAVAILABLE",
        "outcome_category" => "UNAVAILABLE",
        "verification" => {"state" => "UNAVAILABLE", "evidence_references" => []},
        "summary" => "Handoff lifecycle readback is unavailable."
      }
    end

    def handoff_store!
      fail ProtocolError.new(-32601, "structured handoff capability is disabled") unless @handoff_store

      @handoff_store
    end

    def require_exact_arguments!(arguments, required, optional)
      fail ProtocolError.new(-32602, "invalid structured handoff arguments") unless required.all? { |key| arguments.key?(key) } && (arguments.keys - required - optional).empty?
    end

    def error_response(id, code, message)
      {"jsonrpc" => "2.0", "id" => id, "error" => {"code" => code, "message" => message}}
    end
  end
end
