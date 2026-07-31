#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "rexml/document"

ROOT = File.expand_path("../..", __dir__)
ROUTING_PATH = File.join(ROOT, "docs/intellij-mcp-tool-routing.yaml")
FAST_PATH = File.join(ROOT, "docs/intellij-mcp-fast-path.md")
SELF_TEST = File.join(ROOT, "scripts/tool-self-test.rb")
RUN_CONFIGURATIONS = {
  "TheMuffinMan Backend.run.xml" => { name: "TheMuffinMan Backend", command: "./mvnw spring-boot:run", directory: "$PROJECT_DIR$/apps/themuffinman" },
  "TheMuffinMan Backend Tests.run.xml" => { name: "TheMuffinMan Backend Tests", command: "./mvnw test", directory: "$PROJECT_DIR$/apps/themuffinman" },
  "TheMuffinMan Frontend Dev.run.xml" => { name: "TheMuffinMan Frontend Dev", command: "npm run dev", directory: "$PROJECT_DIR$/apps/themuffinman/frontend" },
  "TheMuffinMan Frontend Type Check.run.xml" => { name: "TheMuffinMan Frontend Type Check", command: "npm run type-check", directory: "$PROJECT_DIR$/apps/themuffinman/frontend" },
  "TheMuffinMan Frontend Build.run.xml" => { name: "TheMuffinMan Frontend Build", command: "npm run build", directory: "$PROJECT_DIR$/apps/themuffinman/frontend" }
}.freeze
REQUIRED_INTENTS = %w[bounded_context known_symbol call_relationship symbol_rename edited_file_inspection runtime_diagnosis validation_selection].freeze
REQUIRED_FIELDS = %w[id use_when preferred preconditions fallback local_evidence bounds].freeze

routing = YAML.load_file(ROUTING_PATH)
failures = []
failures << "routing kind must be intellij_mcp_tool_routing" unless routing["kind"] == "intellij_mcp_tool_routing"
failures << "routing canonical owner must be implementation control" unless routing["canonical_owner"] == "docs/implementation-control.md"
rules = Array(routing["rules"])
failures << "routing must require local fallback" unless rules.any? { |rule| rule.include?("Fall back immediately") }
failures << "routing must preserve verifier authority" unless rules.any? { |rule| rule.include?("completion evidence") }

intents = Array(routing["intents"])
ids = intents.map { |intent| intent["id"] }
failures << "routing intent ids differ from required contract" unless ids.sort == REQUIRED_INTENTS.sort
failures << "routing intent ids are duplicated" unless ids.uniq.length == ids.length
intents.each do |intent|
  missing = REQUIRED_FIELDS.reject { |field| intent.key?(field) && !intent[field].to_s.empty? }
  failures << "#{intent["id"]} missing #{missing.join(", ")}" unless missing.empty?
  failures << "#{intent["id"]} fallback cannot require IDEA" if intent["fallback"].to_s.match?(/IDEA|MCP/i)
  failures << "#{intent["id"]} local evidence cannot claim IDEA completion" if intent["local_evidence"].to_s.match?(/IDEA.*(verify|complete)/i)
end

fast_path = File.read(FAST_PATH)
failures << "fast path must link the routing contract" unless fast_path.include?("intellij-mcp-tool-routing.yaml")
failures << "fast path must preserve verifier authority" unless fast_path.include?("work-verify")
self_test = File.read(SELF_TEST)
failures << "tool self-test must expose tooling-only mode" unless self_test.include?("--tooling-only")
failures << "tool self-test must expose stage-contract mode" unless self_test.include?("--check-stage-contract")
failures << "tool self-test must classify product contract stages" unless self_test.include?("product_contract")

RUN_CONFIGURATIONS.each do |file, expected|
  path = File.join(ROOT, ".run", file)
  unless File.file?(path)
    failures << "missing shared run configuration #{file}"
    next
  end

  begin
    configuration = REXML::Document.new(File.read(path)).root&.elements&.to_a("configuration")&.first
    options = configuration ? configuration.elements.to_a("option").to_h { |option| [option.attributes["name"].to_s, option.attributes["value"].to_s] } : {}
    failures << "#{file} has wrong configuration name" unless configuration&.attributes&.fetch("name", nil).to_s == expected.fetch(:name)
    failures << "#{file} must use an integrated shell configuration" unless configuration&.attributes&.fetch("type", nil).to_s == "ShConfigurationType" && options["INTEGRATED"] == "true"
    failures << "#{file} has wrong command" unless options["SCRIPT_TEXT"] == expected.fetch(:command)
    failures << "#{file} has wrong working directory" unless options["WORKING_DIRECTORY"] == expected.fetch(:directory)
  rescue REXML::ParseException => error
    failures << "#{file} is invalid XML: #{error.message.lines.first.strip}"
  end
end

abort "IntelliJ MCP routing audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "IntelliJ MCP routing audit passed (#{intents.length} intents, #{RUN_CONFIGURATIONS.length} shared runs, local fallback and verifier authority preserved)."
