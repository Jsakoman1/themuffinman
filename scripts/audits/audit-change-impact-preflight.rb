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
    scope_guardrails = scope_guardrails(impacts)
    {
      generated_at: Time.now.utc.iso8601,
      changed_file_count: changed_files.size,
      changed_files: impacts,
      unique_docs: impacts.flat_map { |entry| entry[:likely_docs] }.uniq.sort,
      unique_tests: impacts.flat_map { |entry| entry[:likely_tests] }.uniq.sort,
      unique_generated_artifacts: impacts.flat_map { |entry| entry[:generated_artifacts] }.uniq.sort,
      suggested_commands: impacts.flat_map { |entry| entry[:suggested_commands] }.uniq.sort,
      scope_guardrails: scope_guardrails,
      scope_warning_count: scope_guardrails.count { |guardrail| guardrail[:status] == "warn" }
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

  def scope_guardrails(impacts)
    categories = impacts.map { |entry| entry[:category] }.uniq.sort
    domains = impacts.map { |entry| entry[:domain] }.reject { |domain| %w[unknown common config docs testing].include?(domain) }.uniq.sort
    generated_paths = impacts.select { |entry| generated_report_path?(entry[:path]) }.map { |entry| entry[:path] }.sort
    runtime_paths = impacts.reject { |entry| non_runtime_path?(entry[:path], entry[:category]) }.map { |entry| entry[:path] }.sort
    tooling_paths = impacts.select { |entry| tooling_or_infrastructure_path?(entry[:path], entry[:category]) }.map { |entry| entry[:path] }.sort
    expected_generated = impacts.reject { |entry| generated_report_path?(entry[:path]) }
      .flat_map { |entry| entry[:generated_artifacts] }
      .uniq
      .sort
    unexpected_generated = generated_paths.reject { |path| expected_generated.include?(path) }

    [
      guardrail(
        "mixed_product_domains",
        domains.size > 1,
        "Changes touch multiple product domains in one scope.",
        domains
      ),
      guardrail(
        "runtime_plus_tooling_or_infrastructure",
        !runtime_paths.empty? && !tooling_paths.empty?,
        "Runtime code and tooling/infrastructure files changed together.",
        (runtime_paths.first(8) + tooling_paths.first(8)).uniq
      ),
      guardrail(
        "large_generated_report_churn",
        generated_paths.size > 10,
        "More than ten generated report files changed; review whether they are task-required.",
        generated_paths.first(20)
      ),
      guardrail(
        "unexpected_generated_reports",
        !unexpected_generated.empty?,
        "Generated report changes include files not predicted by changed source files.",
        unexpected_generated.first(20)
      ),
      guardrail(
        "source_and_generated_only_review",
        !runtime_paths.empty? && generated_paths.any?,
        "Source changes and generated artifacts changed together; keep only generated outputs required by the source delta.",
        generated_paths.first(20)
      )
    ]
  end

  def guardrail(id, warned, message, evidence)
    {
      id: id,
      status: warned ? "warn" : "ok",
      message: message,
      evidence: evidence
    }
  end

  def generated_report_path?(relative_path)
    relative_path.start_with?("docs/generated/")
  end

  def non_runtime_path?(relative_path, category)
    generated_report_path?(relative_path) ||
      category == "docs" ||
      tooling_or_infrastructure_path?(relative_path, category) ||
      relative_path.start_with?(".agents/")
  end

  def tooling_or_infrastructure_path?(relative_path, category)
    category == "script" ||
      category == "frontend_script" ||
      relative_path == "Makefile" ||
      relative_path.start_with?("docs/tooling/") ||
      relative_path.start_with?(".github/") ||
      relative_path.end_with?("package.json")
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
    lines << "- Scope guardrail warnings: `#{report[:scope_warning_count]}`"
    lines << ""
    lines << "## Scope Guardrails"
    lines << ""
    report[:scope_guardrails].each do |guardrail|
      lines << "- `#{guardrail[:id]}`: `#{guardrail[:status]}` - #{guardrail[:message]}"
      lines << "  Evidence: #{format_inline_list(guardrail[:evidence])}"
    end
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
    lines << "  scope guardrail warnings: #{report[:scope_warning_count]}"
    report[:scope_guardrails].select { |guardrail| guardrail[:status] == "warn" }.first(5).each do |guardrail|
      lines << "  ! #{guardrail[:id]}: #{guardrail[:message]}"
    end
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
