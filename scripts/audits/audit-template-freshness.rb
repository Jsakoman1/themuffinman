#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("../..", __dir__)
TEMPLATE_PATHS = Dir[File.join(ROOT, ".agents/templates/**/*.md"), File.join(ROOT, ".agents/templates/**/*.yaml")].sort.freeze
RETIRED_REFERENCES = %w[
  docs/agent-operating-model.md
  docs/generated/source-of-truth-audit.json
].freeze
REQUIRED_CANONICAL_REFERENCES = %w[
  docs/business-logic.md
  docs/domain-technical.md
  docs/agent-operating-model.yaml
].freeze

failures = []
template_contents = TEMPLATE_PATHS.to_h { |path| [path, File.read(path)] }

template_contents.each do |path, content|
  relative_path = path.delete_prefix("#{ROOT}/")
  RETIRED_REFERENCES.each do |reference|
    failures << "#{relative_path} references retired source #{reference}" if content.include?(reference)
  end

  content.scan(%r{docs/[A-Za-z0-9_./-]+\.(?:md|yaml)}).uniq.each do |reference|
    next if reference.include?("<")

    failures << "#{relative_path} references missing source #{reference}" unless File.file?(File.join(ROOT, reference))
  end
end

REQUIRED_CANONICAL_REFERENCES.each do |reference|
  failures << "No agent template references canonical source #{reference}" unless template_contents.values.any? { |content| content.include?(reference) }
end

evidence_template = template_contents.fetch(File.join(ROOT, ".agents/templates/validation-evidence.template.yaml"))
failures << "Validation-evidence template must require make work-verify" unless evidence_template.include?("make work-verify plan=<work-plan>")

abort "Template freshness audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?

puts "Template freshness audit passed (#{TEMPLATE_PATHS.length} templates, canonical sources and verifier evidence)."
