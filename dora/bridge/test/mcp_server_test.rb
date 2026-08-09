#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "stringio"
require "tmpdir"
require "yaml"

require_relative "../../lib/dora/project_initializer"
require_relative "../lib/dora_bridge/server"

ROOT = File.expand_path("../..", __dir__)

def write_yaml(root, relative, document)
  path = File.join(root, relative)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump(document))
end

def create_project(root)
  Dora::ProjectInitializer.initialize!(root, project_id: "doomsday-storage", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  master = {"kind" => "master", "version" => 1, "id" => "delivery", "title" => "Verified delivery", "status" => "verified", "children" => ["docs/work/delivery.yaml"]}
  plan = {"kind" => "work", "version" => 1, "id" => "delivery-work", "title" => "Delivery work", "status" => "verified", "baseline" => "pending", "tasks" => [{"id" => "verify-delivery", "title" => "Verify delivery", "status" => "done", "observable_outcome" => "A delivery is verified.", "dependencies" => [], "required_paths" => ["docs/delivery.md"], "validation" => "safe-command", "evidence_boundary" => ["fixture"]}], "evidence" => [{"task" => "verify-delivery", "result" => "passed", "ranAt" => "2026-08-09T15:00:00Z", "revision" => "abcdef1", "exitCode" => 0, "output" => "/private/secret/raw-command-output"}]}
  inventory = {"kind" => "execution_inventory", "version" => 1, "id" => "delivery", "master_plan" => "docs/work/delivery-master.yaml", "items" => [{"id" => "delivery-proof", "plan" => "docs/work/delivery.yaml", "task" => "verify-delivery", "status" => "verified", "verified_at" => "2026-08-09T15:00:00Z"}]}
  write_yaml(root, "docs/work/delivery-master.yaml", master)
  write_yaml(root, "docs/work/delivery.yaml", plan)
  write_yaml(root, "docs/work/delivery-inventory.yaml", inventory)
end

def request(server, id, method, params = nil)
  payload = {"jsonrpc" => "2.0", "id" => id, "method" => method}
  payload["params"] = params if params
  server.handle(payload)
end

Dir.mktmpdir("dora-bridge-mcp") do |root|
  project_root = File.join(root, "project")
  create_project(project_root)
  registry_path = File.join(root, "bridge-projects.yaml")
  write_yaml(root, "bridge-projects.yaml", {"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "doomsday-storage", "name" => "DoomsDayStorage", "adapter_path" => "project/.dora/project.yaml"}, {"id" => "unreachable-project", "adapter_path" => "missing/.dora/project.yaml"}]})
  server = DoraBridge::Server.new(DoraBridge::ProjectRegistry.load!(registry_path))

  initialize = request(server, 1, "initialize", {"protocolVersion" => "2025-06-18"})
  abort "initialize failed" unless initialize.dig("result", "capabilities", "tools")
  tool_names = request(server, 2, "tools/list").dig("result", "tools").map { |tool| tool.fetch("name") }
  abort "write or shell tool is exposed" unless tool_names.sort == DoraBridge::Server::TOOL_DEFINITIONS.map(&:first).sort && tool_names.none? { |name| name.match?(/work|verify|shell|file|commit|push/) }
  listed = request(server, 3, "tools/call", {"name" => "list_projects", "arguments" => {}})
  abort "allow-list leaked a filesystem root" if listed.to_s.include?(root)
  abort "allow-list was incomplete" unless listed.dig("result", "structuredContent", "projects").map { |entry| entry.fetch("id") } == %w[doomsday-storage unreachable-project]
  unknown = request(server, 4, "tools/call", {"name" => "get_project_summary", "arguments" => {"project" => "unknown-project"}})
  abort "unknown project was not rejected before an unreachable entry could matter" unless unknown.dig("error", "code") == -32001 && !unknown.to_s.include?("missing")
  summary = request(server, 5, "tools/call", {"name" => "get_project_summary", "arguments" => {"project" => "doomsday-storage"}})
  output = summary.dig("result", "structuredContent")
  abort "summary did not delegate to read model" unless output.fetch("kind") == "dora_project_read_model" && output.dig("delivery", "latest_verified", "id") == "delivery"
  abort "summary leaked a root or raw evidence" if output.to_s.include?(root) || output.to_s.include?("raw-command-output")
  evidence = request(server, 6, "tools/call", {"name" => "get_task_evidence", "arguments" => {"project" => "doomsday-storage", "plan" => "docs/work/delivery.yaml", "task" => "verify-delivery"}})
  abort "sanitized evidence was unavailable" unless evidence.dig("result", "structuredContent", "status") == "passed" && !evidence.to_s.include?("raw-command-output")
  escaped = request(server, 7, "tools/call", {"name" => "get_plan", "arguments" => {"project" => "doomsday-storage", "plan" => "../../.env"}})
  abort "path escape was not rejected" unless escaped.dig("error", "code") == -32001
  File.symlink("/etc/hosts", File.join(project_root, "docs/work/escaped-plan.yaml"))
  symlink_escape = request(server, 71, "tools/call", {"name" => "get_plan", "arguments" => {"project" => "doomsday-storage", "plan" => "docs/work/escaped-plan.yaml"}})
  abort "symlink path escape was not rejected" unless symlink_escape.dig("error", "code") == -32001 && !symlink_escape.to_s.include?("/etc/hosts")
  write_attempt = request(server, 8, "tools/call", {"name" => "work-start", "arguments" => {"project" => "doomsday-storage"}})
  abort "write attempt was accepted" unless write_attempt.dig("error", "code") == -32601
  wire = StringIO.new
  server.run(input: StringIO.new(JSON.generate({"jsonrpc" => "2.0", "id" => 9, "method" => "tools/list"}) + "\n"), output: wire)
  abort "stdio JSON-RPC framing failed" unless JSON.parse(wire.string).dig("result", "tools")
end

puts "Dora Bridge MCP test passed (allow-list, safe read projection, stdio framing, path containment, and no write tools)."
