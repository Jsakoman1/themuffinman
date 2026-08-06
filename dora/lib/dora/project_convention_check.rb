# frozen_string_literal: true

require "yaml"

module Dora
  class ProjectConventionCheck
    SCHEMA_PATH = File.expand_path("../../project-convention-profile.schema.yaml", __dir__)

    def self.inspect!(profile:, manifest:, schema_path: SCHEMA_PATH)
      conventions = validate_profile!(profile, schema_path: schema_path)
      outputs = Array(manifest["outputs"])
      fail!("convention check manifest outputs are invalid") unless outputs.any? && outputs.all? { |output| output.is_a?(Hash) && statement?(output["id"]) && safe_path?(output["path"]) }
      findings = outputs.map { |output| finding_for(output, conventions) }.compact
      {"kind" => "dora_project_convention_check", "version" => 1, "project_id" => conventions.fetch("project_id"), "compatible" => findings.empty?, "findings" => findings, "checked_outputs" => outputs.map { |output| output.fetch("path") }.sort, "completion_boundary" => "Convention checking reports declared path and package compatibility only; it does not write, normalize, compile, or prove consumer source."}.freeze
    rescue Psych::Exception => error
      fail!("project convention YAML is invalid: #{error.message}")
    end

    def self.validate_profile!(profile, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("project convention profile schema is invalid") unless schema["kind"] == "dora_project_convention_profile_schema" && schema["version"].to_i == 1
      fail!("project convention profile is invalid") unless profile.is_a?(Hash) && profile["kind"] == "dora_project_convention_profile" && profile["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| profile.key?(field) }
      fail!("project convention profile is missing #{missing.join(", ")}") unless missing.empty?
      %w[project_id java_package].each { |field| fail!("project convention profile #{field} is invalid") unless statement?(profile[field]) }
      %w[backend_root frontend_root migration_directory api_contract_directory frontend_feature_directory documentation_root].each { |field| fail!("project convention profile #{field} is invalid") unless safe_path?(profile[field]) }
      fail!("project convention profile confirmation must be true") unless profile["confirmation"] == true
      commands = profile["test_commands"]
      fail!("project convention profile test commands are invalid") unless commands.is_a?(Array) && !commands.empty? && commands.all? { |command| command.is_a?(Hash) && statement?(command["id"]) && statement?(command["command"]) }
      profile.slice(*schema.fetch("required_fields")).freeze
    end

    def self.finding_for(output, profile)
      path = output.fetch("path")
      expected = case output.fetch("id")
                 when "flyway-migration" then profile.fetch("migration_directory")
                 when "api-contract" then profile.fetch("api_contract_directory")
                 when "vue-client", "vue-view", "vue-test" then profile.fetch("frontend_feature_directory")
                 when "java-model", "jdbc-repository", "request-dto", "service", "controller" then "#{profile.fetch("backend_root")}/src/main/java/#{profile.fetch("java_package").tr(".", "/")}" 
                 when "junit" then "#{profile.fetch("backend_root")}/src/test/java/#{profile.fetch("java_package").tr(".", "/")}" 
                 when "capability-doc", "evidence-obligations" then profile.fetch("documentation_root")
                 when "generated-manifest" then ".dora/generated-features"
                 end
      return nil unless expected && !path.start_with?(expected + "/") && path != expected
      {"output_id" => output.fetch("id"), "path" => path, "category" => "convention_mismatch", "expected_root" => expected}
    end
    private_class_method :finding_for

    def self.safe_path?(value); value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_path?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
