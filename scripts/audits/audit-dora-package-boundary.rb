#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("../..", __dir__)
DORA_ROOT = File.join(ROOT, "dora")
REQUIRED_PATHS = %w[
  dora/bin/dora
  dora/lib/dora/adapter.rb
  dora/project-adapter.schema.yaml
  dora/templates/work-plan.yaml
  dora/README.md
].freeze
FORBIDDEN_PRODUCT_REFERENCES = ["apps/themuffinman", "com.themuffinman", "muffinman_"].freeze

failures = REQUIRED_PATHS.reject { |path| File.file?(File.join(ROOT, path)) }.map { |path| "missing Dora package path: #{path}" }
package_files = Dir[File.join(DORA_ROOT, "bin/**/*"), File.join(DORA_ROOT, "lib/**/*"), File.join(DORA_ROOT, "templates/**/*"), File.join(DORA_ROOT, "*.yaml"), File.join(DORA_ROOT, "README.md")].select { |path| File.file?(path) }
failures << "Dora package has no portable files" if package_files.empty?
package_files.each do |path|
  content = File.read(path)
  forbidden = FORBIDDEN_PRODUCT_REFERENCES.find { |reference| content.include?(reference) }
  failures << "Dora portable file names product reference #{forbidden}: #{path.delete_prefix("#{ROOT}/")}" if forbidden
end

abort "Dora package boundary audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora package boundary audit passed (#{package_files.length} portable files, no embedded product path)."
