# frozen_string_literal: true

require "yaml"
require_relative "compiled_feature_contract"

module Dora
  class RelatedResourceFeature
    SCHEMA_PATH = File.expand_path("../../related-resource-feature.schema.yaml", __dir__)

    def self.validate!(document, schema_path: SCHEMA_PATH)
      schema = YAML.load_file(schema_path)
      fail!("related resource schema is invalid") unless schema["kind"] == "dora_related_resource_feature_schema" && schema["version"].to_i == 1
      fail!("related resource must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_related_resource_feature" && document["version"].to_i == 1
      missing = schema.fetch("required_fields").reject { |field| document.key?(field) }
      fail!("related resource is missing #{missing.join(", ")}") unless missing.empty?
      feature = CompiledFeatureContract.validate_type_mappings!(document.fetch("feature"))
      relation = document.fetch("relation")
      fail!("related resource relation is invalid") unless relation.is_a?(Hash) && schema.fetch("relation_required_fields").all? { |field| relation.key?(field) } && relation["confirmed"] == true
      foreign_key = CompiledFeatureContract.confirmed_foreign_key!(feature, field_id: relation.fetch("field"))
      fail!("related resource relation must match a confirmed foreign key") unless foreign_key.fetch("table") == relation.fetch("target_table") && foreign_key.fetch("column") == relation.fetch("target_column")
      query = document.fetch("query")
      fail!("related resource query is invalid") unless query.is_a?(Hash) && schema.fetch("query_required_fields").all? { |key| query.key?(key) } && query["index_confirmed"] == true && Array(query["fields"]).include?(relation.fetch("field"))
      fail!("related resource UI states are invalid") unless Array(document.fetch("ui_states")).any? && Array(document.fetch("ui_states")).all? { |state| state.is_a?(String) && !state.empty? }
      fail!("related resource convention profile is invalid") unless document.fetch("convention_profile").is_a?(String) && !document.fetch("convention_profile").empty?
      fail!("related resource confirmation must be true") unless document.fetch("confirmation") == true
      document.slice(*schema.fetch("required_fields")).freeze
    rescue Psych::Exception => error
      fail!("related resource YAML is invalid: #{error.message}")
    end

    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
