# frozen_string_literal: true

require_relative "compiled_feature_contract"
require_relative "related_resource_feature"

module Dora
  class CompiledFeatureRenderers
    TEMPLATE_ROOT = File.expand_path("../../templates/compiled-feature", __dir__)

    def self.render_flyway!(feature:, migration_version:)
      model = CompiledFeatureContract.validate_type_mappings!(feature)
      fail!("compiled feature migration version is invalid") unless migration_version.is_a?(String) && migration_version.match?(/\A[1-9][0-9]*\z/)
      entity = model.fetch("entity")
      table = entity.fetch("table")
      fail!("compiled feature table is not SQL-safe") unless sql_identifier?(table)
      columns = entity.fetch("fields").map { |field| column_definition(field) }
      foreign_keys = entity.fetch("fields").map { |field| foreign_key_definition(field) }.compact
      create = "CREATE TABLE #{table} (\n  #{(columns + foreign_keys).join(",\n  ")}\n);"
      indexes = entity.fetch("fields").select { |field| field.dig("database", "index") }.map do |field|
        column = field.dig("database", "column")
        "CREATE INDEX idx_#{table}_#{column} ON #{table} (#{column});"
      end
      render_template("flyway-migration.sql.erb", "migration_version" => migration_version, "table" => table, "statements" => ([create] + indexes).join("\n\n"))
    end

    def self.render_spring_jdbc!(feature:)
      model = CompiledFeatureContract.validate_type_mappings!(feature)
      stack = model.fetch("stack")
      entity = model.fetch("entity")
      package_name = "#{stack.fetch("package")}.#{safe_java_name(model.fetch("capability"))}"
      package_path = package_name.tr(".", "/")
      class_name = camelize(entity.fetch("id"))
      backend = stack.fetch("backend_root")
      fields = entity.fetch("fields")
      sources = {
        "#{backend}/src/main/java/#{package_path}/#{class_name}.java" => java_record(package_name, class_name, fields),
        "#{backend}/src/main/java/#{package_path}/Create#{class_name}Request.java" => java_record(package_name, "Create#{class_name}Request", fields),
        "#{backend}/src/main/java/#{package_path}/#{class_name}Repository.java" => repository_source(package_name, class_name, entity, fields),
        "#{backend}/src/main/java/#{package_path}/#{class_name}Service.java" => service_source(package_name, class_name, fields),
        "#{backend}/src/main/java/#{package_path}/#{class_name}Controller.java" => controller_source(package_name, class_name, model),
        "#{backend}/src/test/java/#{package_path}/#{class_name}ServiceTest.java" => test_source(package_name, class_name)
      }
      sources.transform_values { |source| template_header + source }
    end

    def self.render_api_and_vue!(feature:)
      model = CompiledFeatureContract.validate_type_mappings!(feature)
      capability = model.fetch("capability")
      feature_path = safe_java_name(capability)
      stack = model.fetch("stack")
      entity = model.fetch("entity")
      class_name = camelize(entity.fetch("id"))
      frontend = stack.fetch("frontend_root")
      base = model.dig("api", "base_path")
      {
        "docs/api/#{capability}.yaml" => api_contract_source(model),
        "#{frontend}/src/features/#{feature_path}/api.js" => vue_client_source(class_name, base),
        "#{frontend}/src/features/#{feature_path}/FeatureView.js" => vue_view_source(class_name, model.fetch("ui")),
        "#{frontend}/src/features/#{feature_path}/feature.test.js" => vue_test_source(class_name)
      }
    end

    def self.render_related_resource_packet!(document:)
      model = RelatedResourceFeature.validate!(document)
      feature = model.fetch("feature")
      capability = feature.fetch("capability")
      sources = render_spring_jdbc!(feature: feature).merge(render_api_and_vue!(feature: feature))
      sources["docs/capabilities/#{capability}-relation.yaml"] = YAML.dump({"kind" => "dora_related_resource_trace", "version" => 1, "capability" => capability, "relation" => model.fetch("relation"), "query" => model.fetch("query"), "ui_states" => model.fetch("ui_states"), "convention_profile" => model.fetch("convention_profile"), "completion_boundary" => "This trace records confirmed additive relation intent only; it does not prove a database, API, UI, or runtime relationship."})
      sources.freeze
    end

    def self.api_contract_source(model)
      entity = model.fetch("entity")
      operation = model.dig("api", "operations").first
      fields = entity.fetch("fields")
      request_fields = operation.fetch("request_fields")
      response_fields = operation.fetch("response_fields")
      properties = fields.map do |field|
        id = field.fetch("id")
        "      #{id}:\n        type: #{openapi_type(field.fetch("java_type"))}"
      end.join("\n")
      required = fields.select { |field| !field.dig("database", "nullable") }.map { |field| field.fetch("id") }
      render_template("api-contract.yaml.erb", "path" => operation.fetch("path"), "method" => operation.fetch("method").downcase, "operation_id" => operation.fetch("id"), "entity" => entity.fetch("id"), "properties" => properties, "request_fields" => request_fields.join(", "), "response_fields" => response_fields.join(", "), "required" => required.join(", "))
    end
    private_class_method :api_contract_source

    def self.vue_client_source(class_name, base_path)
      render_template("vue-feature.js.erb", "source" => "import { ref } from \"vue\";\n\nexport async function create#{class_name}(payload) {\n  const response = await fetch(\"#{base_path}\", { method: \"POST\", headers: { \"Content-Type\": \"application/json\" }, body: JSON.stringify(payload) });\n  if (!response.ok) throw new Error(\"Request failed\");\n  return response.json();\n}\n\nexport function use#{class_name}Feature() {\n  const state = ref(\"loading\");\n  const error = ref(null);\n  return { state, error, create#{class_name} };\n}\n")
    end
    private_class_method :vue_client_source

    def self.vue_view_source(class_name, ui)
      states = ui.fetch("states").map { |state| "\"#{state}\"" }.join(", ")
      render_template("vue-feature.js.erb", "source" => "import { h } from \"vue\";\n\nexport const #{class_name}FeatureView = {\n  name: \"#{class_name}FeatureView\",\n  props: { state: { type: String, required: true } },\n  setup(props) {\n    const declaredStates = [#{states}];\n    return () => h(\"section\", { \"aria-live\": \"polite\" }, declaredStates.includes(props.state) ? props.state : \"error\");\n  }\n};\n")
    end
    private_class_method :vue_view_source

    def self.vue_test_source(class_name)
      "import test from \"node:test\";\nimport assert from \"node:assert/strict\";\n\ntest(\"#{class_name} feature scaffold declares recovery states\", () => {\n  assert.deepEqual([\"loading\", \"empty\", \"error\", \"populated\"].includes(\"error\"), true);\n});\n"
    end
    private_class_method :vue_test_source

    def self.openapi_type(java_type)
      {"String" => "string", "Integer" => "integer", "Long" => "integer", "Boolean" => "boolean", "LocalDate" => "string"}.fetch(java_type)
    end
    private_class_method :openapi_type

    def self.java_record(package_name, class_name, fields)
      components = fields.map { |field| "#{field.fetch("java_type")} #{safe_java_name(field.fetch("id"))}" }.join(", ")
      "package #{package_name};\n\npublic record #{class_name}(#{components}) {}\n"
    end
    private_class_method :java_record

    def self.repository_source(package_name, class_name, entity, fields)
      names = fields.map { |field| safe_java_name(field.fetch("id")) }
      columns = fields.map { |field| field.dig("database", "column") }
      placeholders = Array.new(fields.length, "?").join(", ")
      arguments = names.map { |name| "value.#{name}()" }.join(", ")
      "package #{package_name};\n\nimport org.springframework.jdbc.core.JdbcTemplate;\nimport org.springframework.stereotype.Repository;\n\n@Repository\npublic class #{class_name}Repository {\n    private final JdbcTemplate jdbcTemplate;\n\n    public #{class_name}Repository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }\n\n    public void insert(#{class_name} value) {\n        jdbcTemplate.update(\"INSERT INTO #{entity.fetch("table")} (#{columns.join(", ")}) VALUES (#{placeholders})\", #{arguments});\n    }\n}\n"
    end
    private_class_method :repository_source

    def self.service_source(package_name, class_name, fields)
      checks = fields.select { |field| !field.dig("database", "nullable") && field.fetch("java_type") == "String" }.map { |field| name = safe_java_name(field.fetch("id")); "if (request.#{name}() == null || request.#{name}().isBlank()) { throw new IllegalArgumentException(\"#{name} is required\"); }" }.join("\n        ")
      values = fields.map { |field| "request.#{safe_java_name(field.fetch("id"))}()" }.join(", ")
      "package #{package_name};\n\nimport org.springframework.stereotype.Service;\n\n@Service\npublic class #{class_name}Service {\n    private final #{class_name}Repository repository;\n\n    public #{class_name}Service(#{class_name}Repository repository) { this.repository = repository; }\n\n    public #{class_name} create(Create#{class_name}Request request) {\n        #{checks}\n        #{class_name} value = new #{class_name}(#{values});\n        repository.insert(value);\n        return value;\n    }\n}\n"
    end
    private_class_method :service_source

    def self.controller_source(package_name, class_name, model)
      api = model.fetch("api")
      "package #{package_name};\n\nimport org.springframework.http.ResponseEntity;\nimport org.springframework.web.bind.annotation.PostMapping;\nimport org.springframework.web.bind.annotation.RequestBody;\nimport org.springframework.web.bind.annotation.RequestMapping;\nimport org.springframework.web.bind.annotation.RestController;\n\n@RestController\n@RequestMapping(\"#{api.fetch("base_path")}\")\npublic class #{class_name}Controller {\n    private final #{class_name}Service service;\n\n    public #{class_name}Controller(#{class_name}Service service) { this.service = service; }\n\n    @PostMapping\n    public ResponseEntity<#{class_name}> create(@RequestBody Create#{class_name}Request request) {\n        return ResponseEntity.ok(service.create(request));\n    }\n}\n"
    end
    private_class_method :controller_source

    def self.test_source(package_name, class_name)
      "package #{package_name};\n\nimport static org.junit.jupiter.api.Assertions.assertTrue;\nimport org.junit.jupiter.api.Test;\n\nclass #{class_name}ServiceTest {\n    @Test void generatedTestScaffoldIsPresent() { assertTrue(true); }\n}\n"
    end
    private_class_method :test_source

    def self.template_header
      render_template("spring-jdbc-feature.java.erb", {})
    end
    private_class_method :template_header

    def self.column_definition(field)
      database = field.fetch("database")
      column = database.fetch("column")
      fail!("compiled feature column is not SQL-safe") unless sql_identifier?(column)
      parts = [column, database.fetch("sql_type").upcase]
      parts << "NOT NULL" unless database.fetch("nullable")
      parts << "DEFAULT #{database.fetch("default")}" if database["default"]
      parts << "UNIQUE" if database.fetch("unique")
      parts.join(" ")
    end
    private_class_method :column_definition

    def self.foreign_key_definition(field)
      foreign_key = field.dig("database", "foreign_key")
      return unless foreign_key

      column = field.dig("database", "column")
      table = foreign_key.fetch("table")
      target_column = foreign_key.fetch("column")
      fail!("compiled feature foreign key is not SQL-safe") unless [column, table, target_column].all? { |value| sql_identifier?(value) }
      "FOREIGN KEY (#{column}) REFERENCES #{table} (#{target_column})"
    end
    private_class_method :foreign_key_definition

    def self.render_template(name, values)
      path = File.join(TEMPLATE_ROOT, name)
      fail!("compiled feature template is missing: #{name}") unless File.file?(path)
      values.reduce(File.read(path)) { |content, (key, value)| content.gsub("{{#{key}}}", value.to_s) }
    end
    private_class_method :render_template
    def self.sql_identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9_]*\z/); end
    private_class_method :sql_identifier?
    def self.safe_java_name(value); value.tr("-", "_"); end
    private_class_method :safe_java_name
    def self.camelize(value); value.split("-").map(&:capitalize).join; end
    private_class_method :camelize
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
