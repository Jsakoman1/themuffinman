#!/usr/bin/env ruby

require "yaml"

ROOT = File.expand_path("../..", __dir__)
REGISTRY_PATH = File.join(ROOT, "docs/module-dependency-registry.yaml")
registry = YAML.load_file(REGISTRY_PATH)

analysis = registry.fetch("dependency_boundary_analysis")
rules = analysis.fetch("proposed_forbidden_directions")
required_rule_ids = %w[domain_to_frontend feature_to_vision_internal frontend_feature_to_backend_internals]
rule_ids = rules.map { |rule| rule.fetch("id") }
missing_rules = required_rule_ids - rule_ids
abort "Dependency boundary audit failed: missing rules #{missing_rules.join(', ')}" unless missing_rules.empty?

exception_shape = analysis.fetch("accepted_exception_shape")
required_exception_fields = %w[id from to owner reason expiry_or_review evidence]
missing_exception_fields = required_exception_fields - exception_shape.fetch("required_fields")
abort "Dependency boundary audit failed: exception schema missing #{missing_exception_fields.join(', ')}" unless missing_exception_fields.empty?

backend_root = File.join(ROOT, "apps/themuffinman/src/main/java")
frontend_root = File.join(ROOT, "apps/themuffinman/frontend/src")
feature_packages = %w[workmarket business chat social things rides]
backend_files = Dir[File.join(backend_root, "**/*.java")]
frontend_files = Dir[File.join(frontend_root, "**/*")].select { |path| File.file?(path) }

violations = []

backend_files.each do |path|
  content = File.read(path)
  relative = path.delete_prefix("#{ROOT}/")
  violations << "domain_to_frontend: #{relative}" if content.match?(/import\s+.*(?:frontend|apps\.themuffinman\.frontend)/)

  package_match = content.match(/package\s+com\.themuffinman\.app\.([A-Za-z0-9_]+)/)
  source_package = package_match && package_match[1]
  if source_package && feature_packages.include?(source_package)
    content.scan(/import\s+com\.themuffinman\.app\.vision(?:\.|;)/).each do
      violations << "feature_to_vision_internal: #{relative}"
    end
  end
end

frontend_files.each do |path|
  content = File.read(path)
  relative = path.delete_prefix("#{ROOT}/")
  if content.match?(/(?:import|from)\s+['\"](?:com\.themuffinman\.app|\.\.\/.*backend)/)
    violations << "frontend_feature_to_backend_internals: #{relative}"
  end
end

unless violations.empty?
  warn "Dependency boundary audit failed"
  violations.uniq.each { |violation| warn "  #{violation}" }
  exit 1
end

puts "Dependency boundary audit passed"
puts "  backend files checked: #{backend_files.size}"
puts "  frontend source files checked: #{frontend_files.size}"
puts "  forbidden directions checked: #{required_rule_ids.join(', ')}"
puts "  explicit exception schema: valid"
