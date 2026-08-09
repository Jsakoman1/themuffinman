# frozen_string_literal: true

require "json"

require_relative "project_registry"

module DoraBridge
  # JSON-RPC framing for the small, read-only MCP surface. This contains no Dora
  # project semantics: it authorizes a configured public ID, then delegates to
  # Dora::ProjectReadModel.
  class Server
    PROTOCOL_VERSION = "2025-06-18"
    TOOL_DEFINITIONS = [
      ["list_projects", "List the bridge's explicitly allowed Dora projects.", {}],
      ["get_project_summary", "Get a compact, sanitized current Dora project summary.", {"project" => {"type" => "string"}}],
      ["get_project_health", "Get sanitized Dora project health and inconsistencies.", {"project" => {"type" => "string"}}],
      ["get_current_delivery", "Get active and latest verified Dora delivery state.", {"project" => {"type" => "string"}}],
      ["get_next_task", "Get Dora's next eligible task, if it resolves unambiguously.", {"project" => {"type" => "string"}}],
      ["get_open_decisions", "Get declared unresolved product decisions.", {"project" => {"type" => "string"}}],
      ["get_plan", "Get a sanitized declared Dora work plan.", {"project" => {"type" => "string"}, "plan" => {"type" => "string"}}],
      ["get_task_evidence", "Get a sanitized verification summary for a declared task.", {"project" => {"type" => "string"}, "plan" => {"type" => "string"}, "task" => {"type" => "string"}}]
    ].freeze

    def initialize(registry)
      @registry = registry
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
      {"protocolVersion" => PROTOCOL_VERSION, "capabilities" => {"tools" => {"listChanged" => false}}, "serverInfo" => {"name" => "dora-bridge", "version" => "1.0.0"}, "instructions" => "Dora Bridge is read-only. Use only declared project IDs and project-relative artifact references."}
    end

    def tools
      TOOL_DEFINITIONS.map do |name, description, properties|
        {"name" => name, "description" => description, "inputSchema" => {"type" => "object", "properties" => properties, "required" => properties.keys, "additionalProperties" => false}, "annotations" => {"readOnlyHint" => true, "destructiveHint" => false, "openWorldHint" => false}}
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

    def error_response(id, code, message)
      {"jsonrpc" => "2.0", "id" => id, "error" => {"code" => code, "message" => message}}
    end
  end
end
