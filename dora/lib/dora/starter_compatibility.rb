# frozen_string_literal: true

require "yaml"
require "open3"

module Dora
  class StarterCompatibility
    SCHEMA_PATH = File.expand_path("../../starter-compatibility.schema.yaml", __dir__)

    def self.load!(path)
      validate!(YAML.load_file(path))
    rescue Psych::Exception => error
      fail!("starter compatibility YAML is invalid: #{error.message}")
    end

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("starter compatibility schema is invalid") unless schema["kind"] == "dora_starter_compatibility_schema" && schema["version"].to_i == 1
      fail!("starter compatibility must be a mapping") unless document.is_a?(Hash)
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("starter compatibility is missing #{missing.join(", ")}") unless missing.empty?
      fail!("starter compatibility kind is invalid") unless document["kind"] == "dora_starter_compatibility" && document["version"].to_i == schema["version"].to_i
      fail!("starter compatibility starter_id is invalid") unless identifier?(document["starter_id"])
      fail!("starter compatibility observation_boundary must be read_only") unless document["observation_boundary"] == "read_only"
      fail!("starter compatibility requirements must be a list") unless document["requirements"].is_a?(Array)

      requirements = document.fetch("requirements")
      ids = requirements.map { |requirement| validate_requirement!(requirement) }
      fail!("starter compatibility requirement ids must be unique") unless ids.uniq.length == ids.length
      missing_ids = schema.fetch("required_requirement_ids") - ids
      fail!("starter compatibility is missing required tools: #{missing_ids.join(", ")}") unless missing_ids.empty?
      unknown = ids - schema.fetch("required_requirement_ids") - schema.fetch("optional_requirement_ids")
      fail!("starter compatibility has unsupported tools: #{unknown.join(", ")}") unless unknown.empty?

      requirements.each do |requirement|
        fail!("starter compatibility command is not allowed for #{requirement.fetch("id")}") unless requirement.fetch("command") == expected_command(requirement.fetch("id"))
        next unless schema.fetch("optional_requirement_ids").include?(requirement.fetch("id"))

        fail!("optional tool #{requirement.fetch("id")} must be marked required: false") unless requirement.fetch("required") == false
      end
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("starter compatibility schema is invalid: #{error.message}")
    end

    def self.readiness!(path, command_runner: nil)
      compatibility = load!(path)
      command_runner ||= lambda do |command|
        Open3.capture3(*command.split).last.success?
      rescue Errno::ENOENT
        false
      end
      observed = compatibility.fetch("requirements").map do |requirement|
        available = command_runner.call(requirement.fetch("command"))
        {"id" => requirement.fetch("id"), "required" => requirement.fetch("required"), "command" => requirement.fetch("command"), "purpose" => requirement.fetch("purpose"), "status" => available ? "available" : requirement.fetch("required") ? "missing" : "optional_missing"}
      end
      missing = observed.select { |requirement| requirement.fetch("status") == "missing" }
      {"kind" => "dora_app_readiness", "version" => 1, "starter_id" => compatibility.fetch("starter_id"), "ready_to_start" => missing.empty?, "requirements" => observed, "blocking_gaps" => missing.map { |requirement| "Install or expose #{requirement.fetch("id")} required by #{compatibility.fetch("starter_id")}: #{requirement.fetch("command")}" }, "recommended_next_action" => missing.empty? ? "The declared technical prerequisites are observable. Review project-specific configuration before applying the starter." : "Resolve the first missing prerequisite: #{missing.first.fetch("id")}", "observation_boundary" => "Commands are version observations only; Dora does not install, configure, or mutate host tools.", "completion_boundary" => "App readiness is a local toolchain observation only; it does not apply a starter, create a project, or prove a generated application."}.freeze
    end

    def self.validate_requirement!(requirement)
      fail!("starter compatibility requirement must be a mapping") unless requirement.is_a?(Hash)
      %w[id command required purpose].each { |field| fail!("starter compatibility requirement is missing #{field}") unless present?(requirement[field]) || field == "required" && [true, false].include?(requirement[field]) }
      fail!("starter compatibility requirement id is invalid") unless identifier?(requirement.fetch("id"))
      fail!("starter compatibility requirement command is invalid") unless statement?(requirement.fetch("command"))
      fail!("starter compatibility requirement required must be boolean") unless [true, false].include?(requirement.fetch("required"))
      fail!("starter compatibility requirement purpose is invalid") unless statement?(requirement.fetch("purpose"))
      requirement.fetch("id")
    end
    private_class_method :validate_requirement!

    def self.expected_command(id)
      {"java" => "java -version", "maven" => "mvn -version", "node" => "node --version", "npm" => "npm --version", "docker" => "docker version", "compose" => "docker compose version", "playwright-browser" => "npx playwright --version"}.fetch(id)
    end
    private_class_method :expected_command

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.present?(value)
      !value.nil? && (!value.respond_to?(:empty?) || !value.empty?)
    end
    private_class_method :present?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
