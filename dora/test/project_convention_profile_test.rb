#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
schema = YAML.load_file(File.join(ROOT, "project-convention-profile.schema.yaml"))
template = YAML.load_file(File.join(ROOT, "templates/project-convention-profile.yaml"))

abort "project convention schema is invalid" unless schema["kind"] == "dora_project_convention_profile_schema" && schema["version"] == 1
missing = schema.fetch("required_fields").reject { |field| template.key?(field) }
abort "project convention template is missing #{missing.join(", ")}" unless missing.empty?
abort "project convention template must require confirmation" unless template.fetch("confirmation") == false
template.fetch("test_commands").each do |command|
  absent = schema.fetch("test_command_required_fields").reject { |field| command.key?(field) }
  abort "project convention test command is incomplete" unless absent.empty?
end
%w[backend_root frontend_root migration_directory api_contract_directory frontend_feature_directory documentation_root].each do |field|
  path = template.fetch(field)
  abort "project convention path is not relative" if path.start_with?("/") || path.split("/").include?("..")
end

puts "Dora project convention profile test passed (explicit relative roots, commands, and confirmation are declared without inspecting a consumer)."
