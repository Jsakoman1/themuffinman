# frozen_string_literal: true

require "digest"
require "json"
require "yaml"
require_relative "compiled_feature_contract"

module Dora
  class GeneratedFeatureManifest
    SCHEMA_PATH = File.expand_path("../../generated-feature-manifest.schema.yaml", __dir__)
    VERSION = "1.6"
    TEMPLATE_IDS = %w[flyway-migration spring-jdbc-feature api-contract vue-feature manifest evidence-obligations].freeze

    def self.build!(feature:, migration_version:, template_digests:)
      model = CompiledFeatureContract.validate_type_mappings!(feature)
      fail!("generated feature migration version is invalid") unless migration_version.is_a?(String) && migration_version.match?(/\A[1-9][0-9]*\z/)
      templates = validate_template_digests!(template_digests)
      entity = model.fetch("entity")
      stack = model.fetch("stack")
      capability = model.fetch("capability")
      package_path = stack.fetch("package").tr(".", "/")
      class_name = camelize(entity.fetch("id"))
      feature_path = safe_java_path(capability)
      outputs = [
        output("flyway-migration", "#{stack.fetch("migration_directory")}/V#{migration_version}__create_#{entity.fetch("table")}.sql", "flyway-migration", templates),
        output("java-model", "#{stack.fetch("backend_root")}/src/main/java/#{package_path}/#{feature_path}/#{class_name}.java", "spring-jdbc-feature", templates),
        output("jdbc-repository", "#{stack.fetch("backend_root")}/src/main/java/#{package_path}/#{feature_path}/#{class_name}Repository.java", "spring-jdbc-feature", templates),
        output("request-dto", "#{stack.fetch("backend_root")}/src/main/java/#{package_path}/#{feature_path}/Create#{class_name}Request.java", "spring-jdbc-feature", templates),
        output("service", "#{stack.fetch("backend_root")}/src/main/java/#{package_path}/#{feature_path}/#{class_name}Service.java", "spring-jdbc-feature", templates),
        output("controller", "#{stack.fetch("backend_root")}/src/main/java/#{package_path}/#{feature_path}/#{class_name}Controller.java", "spring-jdbc-feature", templates),
        output("junit", "#{stack.fetch("backend_root")}/src/test/java/#{package_path}/#{feature_path}/#{class_name}ServiceTest.java", "spring-jdbc-feature", templates),
        output("api-contract", "docs/api/#{capability}.yaml", "api-contract", templates),
        output("vue-client", "#{stack.fetch("frontend_root")}/src/features/#{feature_path}/api.js", "vue-feature", templates),
        output("vue-view", "#{stack.fetch("frontend_root")}/src/features/#{feature_path}/FeatureView.js", "vue-feature", templates),
        output("vue-test", "#{stack.fetch("frontend_root")}/src/features/#{feature_path}/feature.test.js", "vue-feature", templates),
        output("capability-doc", "docs/capabilities/#{capability}.yaml", "manifest", templates),
        output("evidence-obligations", "docs/capabilities/#{capability}-evidence.yaml", "evidence-obligations", templates),
        output("generated-manifest", ".dora/generated-features/#{capability}.yaml", "manifest", templates)
      ]
      manifest = {"kind" => "dora_generated_feature_manifest", "version" => 1, "generator_version" => VERSION, "confirmed_input_digest" => digest(model), "migration_version" => migration_version, "template_digests" => templates, "outputs" => outputs, "unresolved_obligations" => [{"id" => "compile", "reason" => "Generated output has not been compiled."}, {"id" => "runtime", "reason" => "Runtime proof requires separate explicit approval."}, {"id" => "acceptance", "reason" => "Business acceptance evidence has not been recorded."}]}
      validate!(manifest)
    end

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("generated feature manifest schema is invalid") unless schema["kind"] == "dora_generated_feature_manifest_schema" && schema["version"].to_i == 1
      fail!("generated feature manifest is invalid") unless document.is_a?(Hash) && document["kind"] == "dora_generated_feature_manifest" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("generated feature manifest is missing #{missing.join(", ")}") unless missing.empty?
      fail!("generated feature manifest generator version is invalid") unless statement?(document["generator_version"])
      fail!("generated feature manifest input digest is invalid") unless sha256?(document["confirmed_input_digest"])
      fail!("generated feature manifest migration version is invalid") unless document["migration_version"].is_a?(String) && document["migration_version"].match?(/\A[1-9][0-9]*\z/)
      validate_template_digests!(document["template_digests"])
      outputs = Array(document["outputs"])
      fail!("generated feature manifest outputs are invalid") unless !outputs.empty? && outputs.all? { |output| output.is_a?(Hash) && identifier?(output["id"]) && safe_path?(output["path"]) && TEMPLATE_IDS.include?(output["template"]) && sha256?(output["template_digest"]) } && outputs.map { |output| output["path"] }.uniq.length == outputs.length
      obligations = Array(document["unresolved_obligations"])
      fail!("generated feature manifest obligations are invalid") unless !obligations.empty? && obligations.all? { |entry| entry.is_a?(Hash) && identifier?(entry["id"]) && statement?(entry["reason"]) }
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("generated feature manifest YAML is invalid: #{error.message}")
    end

    def self.related_trace_path(feature)
      model = CompiledFeatureContract.validate!(feature)
      "docs/capabilities/#{model.fetch("capability")}-relation.yaml"
    end

    def self.validate_template_digests!(value)
      fail!("generated feature template digests are invalid") unless value.is_a?(Hash) && TEMPLATE_IDS.all? { |id| sha256?(value[id]) }
      value.slice(*TEMPLATE_IDS).freeze
    end
    private_class_method :validate_template_digests!
    def self.output(id, path, template, template_digests); {"id" => id, "path" => path, "template" => template, "template_digest" => template_digests.fetch(template)}; end
    private_class_method :output
    def self.digest(value); Digest::SHA256.hexdigest(JSON.generate(deep_sort(value))); end
    private_class_method :digest
    def self.deep_sort(value); value.is_a?(Hash) ? value.keys.sort.to_h { |key| [key, deep_sort(value[key])] } : value.is_a?(Array) ? value.map { |item| deep_sort(item) } : value; end
    private_class_method :deep_sort
    def self.camelize(value); value.split("-").map(&:capitalize).join; end
    private_class_method :camelize
    def self.safe_java_path(value); value.tr("-", "_"); end
    private_class_method :safe_java_path
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.safe_path?(value); value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_path?
    def self.sha256?(value); value.is_a?(String) && value.match?(/\A[0-9a-f]{64}\z/); end
    private_class_method :sha256?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
