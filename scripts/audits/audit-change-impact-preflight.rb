#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require_relative "../local_tooling_common"

module ChangeImpactPreflight
  extend self

  DOCS_BY_DOMAIN = {
    "workmarket" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "social" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "chat" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "identity" => %w[docs/business-logic.md docs/domain-technical.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "location" => %w[docs/business-logic.md docs/domain-technical.md docs/location-services.md docs/agent-operating-model.md docs/agent-operating-model.yaml],
    "agent" => %w[docs/agent-operating-model.md docs/agent-operating-model.yaml docs/domain-technical.md]
  }.freeze

  GENERATED_BY_CATEGORY = {
    "backend_controller" => %w[
      docs/generated/agent-endpoint-inventory.json
      docs/generated/source-of-truth-audit.json
      docs/generated/backend-audit-inventory.json
    ],
    "backend_service" => %w[
      docs/generated/source-of-truth-audit.json
      docs/generated/backend-audit-inventory.json
    ],
    "backend_mapper" => %w[
      docs/generated/source-of-truth-audit.json
      docs/generated/backend-audit-inventory.json
    ],
    "backend_dto" => %w[
      apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts
      docs/generated/automation-read-model-inventory.json
      docs/generated/source-of-truth-audit.json
      docs/generated/backend-audit-inventory.json
    ],
    "backend_model" => %w[
      docs/generated/backend-audit-inventory.json
    ],
    "frontend_api" => %w[
      apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts
    ],
    "frontend_contract" => %w[
      apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts
    ],
    "docs" => %w[
      docs/generated/agent-endpoint-inventory.json
      docs/generated/automation-read-model-inventory.json
      docs/generated/source-of-truth-audit.json
      docs/generated/backend-audit-inventory.json
    ]
  }.freeze

  COMMANDS_BY_CATEGORY = {
    "backend_controller" => %w[./mvnw test make generate-agent-artifacts],
    "backend_service" => %w[./mvnw test make generate-agent-artifacts],
    "backend_mapper" => %w[./mvnw test make generate-agent-artifacts],
    "backend_dto" => %w[npm run build npm run type-check make generate-agent-artifacts],
    "frontend_api" => %w[npm run type-check npm run build],
    "frontend_view" => %w[npm run type-check npm run build],
    "frontend_composable" => %w[npm run type-check npm run build],
    "frontend_contract" => %w[npm run type-check npm run build],
    "docs" => %w[./mvnw test make generate-agent-artifacts]
  }.freeze

  def run(argv)
    changed_files = argv.empty? ? LocalToolingCommon.git_changed_files : argv
    changed_files = changed_files.uniq.select { |path| File.exist?(File.join(LocalToolingCommon::REPO_ROOT, path)) }

    report = build_report(changed_files)
    LocalToolingCommon.write_json("docs/generated/local-tooling/change-impact-preflight.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/change-impact-preflight-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def build_report(changed_files)
    impacts = changed_files.map { |path| impact_for(path) }
    {
      generated_at: Time.now.utc.iso8601,
      changed_file_count: changed_files.size,
      changed_files: impacts,
      unique_docs: impacts.flat_map { |entry| entry[:likely_docs] }.uniq.sort,
      unique_tests: impacts.flat_map { |entry| entry[:likely_tests] }.uniq.sort,
      unique_generated_artifacts: impacts.flat_map { |entry| entry[:generated_artifacts] }.uniq.sort,
      suggested_commands: impacts.flat_map { |entry| entry[:suggested_commands] }.uniq.sort
    }
  end

  def impact_for(relative_path)
    category = LocalToolingCommon.path_category(relative_path)
    domain = LocalToolingCommon.domain_for_path(relative_path)
    {
      path: relative_path,
      category: category,
      domain: domain,
      likely_docs: likely_docs(relative_path, domain, category),
      likely_tests: likely_tests(relative_path),
      generated_artifacts: GENERATED_BY_CATEGORY.fetch(category, []),
      sibling_read_surfaces: sibling_read_surfaces(relative_path),
      suggested_commands: COMMANDS_BY_CATEGORY.fetch(category, [])
    }
  end

  def likely_docs(relative_path, domain, category)
    docs = DOCS_BY_DOMAIN.fetch(domain, []).dup
    docs << "docs/codex-local-tooling-todo.md" if category == "script" || relative_path.include?("/scripts/")
    docs << "docs/change-completion-checklist.md" if category.start_with?("backend_")
    docs.uniq
  end

  def likely_tests(relative_path)
    return [] unless relative_path.start_with?("apps/themuffinman/src/main/java/")

    test_relative = relative_path.sub("/src/main/java/", "/src/test/java/").sub(/\.java$/, "Test.java")
    candidates = [test_relative]
    file_name = File.basename(relative_path, ".java")
    domain = LocalToolingCommon.domain_for_path(relative_path)
    candidates.concat(
      Dir.glob(File.join(LocalToolingCommon::REPO_ROOT, "apps/themuffinman/src/test/java/com/themuffinman/app/#{domain}/service/*{ScenarioTest,ContractTest,Test}.java")).map do |path|
        LocalToolingCommon.relative_path(path)
      end
    )
    candidates.select { |path| File.exist?(File.join(LocalToolingCommon::REPO_ROOT, path)) }.uniq.sort
  end

  def sibling_read_surfaces(relative_path)
    return [] unless relative_path.include?("/service/") || relative_path.include?("/controller/") || relative_path.include?("/mapper/")

    base_dir = File.dirname(relative_path)
    file_name = File.basename(relative_path, ".java")
    Dir.glob(File.join(LocalToolingCommon::REPO_ROOT, base_dir, "*.java")).map do |path|
      LocalToolingCommon.relative_path(path)
    end.reject { |path| path.end_with?("#{file_name}.java") }.select do |path|
      content = LocalToolingCommon.read(File.join(LocalToolingCommon::REPO_ROOT, path))
      content.match?(/\b(get|search|find|list|open|lookup|resolve|build)[A-Z]\w*\s*\(/)
    end.sort.first(8)
  end

  def markdown_summary(report)
    lines = []
    lines << "# Change Impact Preflight"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Changed files: `#{report[:changed_file_count]}`"
    lines << "- Unique docs to review: `#{report[:unique_docs].size}`"
    lines << "- Unique tests to consider: `#{report[:unique_tests].size}`"
    lines << "- Generated artifacts to check: `#{report[:unique_generated_artifacts].size}`"
    lines << ""
    report[:changed_files].each do |entry|
      lines << "## `#{entry[:path]}`"
      lines << ""
      lines << "- Category: `#{entry[:category]}`"
      lines << "- Domain: `#{entry[:domain]}`"
      lines << "- Likely docs: #{format_inline_list(entry[:likely_docs])}"
      lines << "- Likely tests: #{format_inline_list(entry[:likely_tests])}"
      lines << "- Generated artifacts: #{format_inline_list(entry[:generated_artifacts])}"
      lines << "- Sibling read surfaces: #{format_inline_list(entry[:sibling_read_surfaces])}"
      lines << "- Suggested commands: #{format_inline_list(entry[:suggested_commands])}"
      lines << ""
    end
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Change impact preflight"
    lines << "  changed files: #{report[:changed_file_count]}"
    lines << "  docs to review: #{report[:unique_docs].size}"
    lines << "  tests to consider: #{report[:unique_tests].size}"
    lines << "  generated artifacts: #{report[:unique_generated_artifacts].size}"
    report[:changed_files].first(10).each do |entry|
      lines << "  - #{entry[:path]} -> docs=#{entry[:likely_docs].size}, tests=#{entry[:likely_tests].size}, siblings=#{entry[:sibling_read_surfaces].size}"
    end
    lines.join("\n")
  end

  def format_inline_list(items)
    return "_none_" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end
end

ChangeImpactPreflight.run(ARGV)
