# frozen_string_literal: true

require "yaml"

module Dora
  class FeatureSkeleton
    SCHEMA_PATH = File.expand_path("../../feature-skeleton.schema.yaml", __dir__)
    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("feature skeleton schema is invalid") unless schema["kind"] == "dora_feature_skeleton_schema" && schema["version"].to_i == 1
      fail!("feature skeleton must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_feature_skeleton" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| present?(document[field]) }
      fail!("feature skeleton is missing #{missing.join(", ")}") unless missing.empty?
      %w[capability entity permission workflow ui_blueprint].each { |field| fail!("feature skeleton #{field} is invalid") unless identifier?(document[field]) }
      fail!("feature skeleton confirmation must be true") unless document["confirmation"] == true
      fields = Array(document["fields"]); fail!("feature skeleton fields must be explicit") unless !fields.empty? && fields.all? { |field| field.is_a?(Hash) && identifier?(field["id"]) && statement?(field["type"]) && field["confirmed"] == true }
      operations = Array(document["api_operations"]); fail!("feature skeleton API operations must be explicit") unless !operations.empty? && operations.all? { |operation| operation.is_a?(Hash) && identifier?(operation["id"]) && statement?(operation["purpose"]) && operation["confirmed"] == true }
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("feature skeleton YAML is invalid: #{error.message}")
    end

    def self.preview!(document)
      model = validate!(document)
      capability = model.fetch("capability")
      entity = model.fetch("entity")
      entity_file = entity.tr("-", "_")

      {
        "kind" => "dora_feature_skeleton_preview",
        "version" => 1,
        "capability" => capability,
        "confirmed_model" => model,
        "proposed_files" => [
          proposal("migration", "backend/src/main/resources/db/migration/V__create_#{entity_file}.sql", "Reviewable Flyway migration skeleton for the declared entity fields."),
          proposal("backend", "backend/src/main/java/example/#{capability}/#{entity_file}_dto.java", "Reviewable DTO skeleton for the declared API operations."),
          proposal("backend", "backend/src/main/java/example/#{capability}/#{entity_file}_service.java", "Reviewable service skeleton with the declared permission and workflow references."),
          proposal("backend", "backend/src/main/java/example/#{capability}/#{entity_file}_controller.java", "Reviewable controller skeleton for the declared API operations."),
          proposal("api", "docs/api/#{capability}.yaml", "Reviewable API contract skeleton."),
          proposal("frontend", "frontend/src/features/#{capability}/api-client.ts", "Reviewable Vue API-client skeleton."),
          proposal("frontend", "frontend/src/features/#{capability}/feature-view.vue", "Reviewable Vue feature-view skeleton using the declared UI blueprint."),
          proposal("test", "backend/src/test/java/example/#{capability}/#{entity_file}_service_test.java", "Reviewable backend test-scenario skeleton."),
          proposal("test", "frontend/src/features/#{capability}/feature-view.spec.ts", "Reviewable frontend test-scenario skeleton."),
          proposal("runtime", "docs/runtime-evidence/#{capability}.yaml", "Declared runtime scenario awaiting implementation and evidence."),
          proposal("documentation", "docs/capabilities/#{capability}.yaml", "Declared capability, permission, workflow, and evidence references.")
        ],
        "review_requirements" => [
          "Review every proposed path and generated placeholder before applying it.",
          "Confirm database types, API shapes, and UI behavior during implementation; this preview does not infer them.",
          "Record validation and runtime evidence only after the feature is implemented."
        ],
        "completion_boundary" => "This is a read-only proposal from confirmed feature inputs. It creates no files, implements no behavior, and proves no acceptance."
      }.freeze
    end

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.present?(value); !value.nil? && (!value.respond_to?(:empty?) || !value.empty?); end
    private_class_method :present?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
    def self.proposal(type, path, purpose); {"type" => type, "path" => path, "purpose" => purpose}; end
    private_class_method :proposal
  end
end
