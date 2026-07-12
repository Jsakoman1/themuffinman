#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "time"
require "yaml"
require_relative "../local_tooling_common"

module GenerateControlStart
  extend self

  OUTPUT_ROOT = "docs/generated/local-tooling"

  def run
    plans = plan_entries
    open_plans = plans.reject { |plan| plan.fetch("status") == "complete" }
    master_plans = open_plans.select { |plan| plan.fetch("kind") == "master-plan" }
    audit_summary = read_json("#{OUTPUT_ROOT}/audit-summary-index.json")
    temp_work_products = temp_work_product_entries

    plan_index = {
      "generatedAt" => now,
      "totalCount" => plans.size,
      "openCount" => open_plans.size,
      "openMasterPlans" => master_plans,
      "openPlans" => open_plans.reject { |plan| plan.fetch("kind") == "master-plan" }
    }
    control_start = {
      "generatedAt" => now,
      "planIndex" => {
        "path" => "#{OUTPUT_ROOT}/plan-index.json",
        "summaryPath" => "#{OUTPUT_ROOT}/plan-index-summary.md",
        "totalCount" => plan_index.fetch("totalCount"),
        "openCount" => plan_index.fetch("openCount"),
        "openMasterPlans" => master_plans.first(5),
        "openPlans" => plan_index.fetch("openPlans").first(10)
      },
      "auditSummaryIndex" => {
        "path" => "#{OUTPUT_ROOT}/audit-summary-index.json",
        "summaryPath" => "#{OUTPUT_ROOT}/audit-summary-index.md",
        "registryEntries" => audit_summary["registry_entries"],
        "trackedOutputs" => audit_summary["tracked_outputs"],
        "missingOutputs" => audit_summary["missing_outputs"]
      },
      "tempWorkProducts" => temp_work_products,
      "operatorCoreSurfaces" => [
        "#{OUTPUT_ROOT}/control-start-summary.md",
        "#{OUTPUT_ROOT}/audit-summary-index.md",
        "#{OUTPUT_ROOT}/codex-context/latest.review.md",
        "#{OUTPUT_ROOT}/targeted-tests-summary.md"
      ],
      "archiveSurfaces" => [
        "#{OUTPUT_ROOT}/.history/",
        "#{OUTPUT_ROOT}/.cache/"
      ],
      "nextAction" => "Use `make codex-context topic=<topic> intent='<intent>'` for topic-specific context after this snapshot is refreshed."
    }

    LocalToolingCommon.write_json("#{OUTPUT_ROOT}/plan-index.json", plan_index)
    LocalToolingCommon.write_text("#{OUTPUT_ROOT}/plan-index-summary.md", plan_index_markdown(plan_index))
    LocalToolingCommon.write_json("#{OUTPUT_ROOT}/control-start.json", control_start)
    LocalToolingCommon.write_text("#{OUTPUT_ROOT}/control-start-summary.md", control_start_markdown(control_start))

    puts "Control Start\n  json: #{OUTPUT_ROOT}/control-start.json\n  summary: #{OUTPUT_ROOT}/control-start-summary.md"
  end

  private

  def plan_entries
    LocalToolingCommon.repo_glob(".agents/*-plan.md").map do |absolute_path|
      relative_path = LocalToolingCommon.relative_path(absolute_path)
      content = File.read(absolute_path)
      frontmatter = LocalToolingCommon.markdown_frontmatter(content)
      status = normalize_status(frontmatter["machine_status"] || frontmatter[:machine_status])
      {
        "path" => relative_path,
        "kind" => File.basename(relative_path).include?("master-plan") ? "master-plan" : "plan",
        "status" => status,
        "title" => (frontmatter["machine_title"] || frontmatter[:machine_title] || markdown_title(content)).to_s.strip,
        "goal" => (frontmatter["machine_goal"] || frontmatter[:machine_goal]).to_s.strip,
        "openTasks" => content.scan(/^\s*-\s*\[\s\]/).size
      }
    end.compact.sort_by { |plan| [status_order(plan.fetch("status")), plan.fetch("kind"), plan.fetch("path")] }
  end

  def temp_work_product_entries
    LocalToolingCommon.repo_glob(".agents/tmp/*.{yaml,yml,json}").map do |absolute_path|
      relative_path = LocalToolingCommon.relative_path(absolute_path)
      payload = parse_structured_file(absolute_path)
      next unless payload.is_a?(Hash)

      owner_plan = payload["ownerPlan"] || payload[:ownerPlan]
      next if owner_plan.to_s.strip.empty?

      {
        "path" => relative_path,
        "ownerPlan" => owner_plan,
        "purpose" => payload["purpose"] || payload[:purpose],
        "status" => payload["status"] || payload[:status]
      }
    end.compact.sort_by { |item| [item.fetch("ownerPlan").to_s, item.fetch("path")] }
  end

  def parse_structured_file(path)
    case File.extname(path)
    when ".json"
      JSON.parse(File.read(path))
    else
      YAML.safe_load(File.read(path), permitted_classes: [Date, Time], aliases: true)
    end
  rescue StandardError
    nil
  end

  def read_json(path)
    JSON.parse(File.read(File.join(LocalToolingCommon::REPO_ROOT, path)))
  rescue StandardError
    {}
  end

  def normalize_status(value)
    normalized = value.to_s.strip.downcase
    return "complete" if %w[complete completed done closed].include?(normalized)
    return "active" if %w[active in-progress in_progress].include?(normalized)
    return "draft" if %w[draft pending].include?(normalized)

    "unknown"
  end

  def status_order(status)
    {"active" => 0, "draft" => 1, "complete" => 2}.fetch(status, 3)
  end

  def markdown_title(content)
    content[/^#\s+(.+)$/, 1].to_s
  end

  def plan_index_markdown(payload)
    lines = ["# Plan Index", ""]
    lines << "- Total plans: `#{payload.fetch("totalCount")}`"
    lines << "- Open plans: `#{payload.fetch("openCount")}`"
    lines << ""
    lines << "## Open Master Plans"
    lines << ""
    append_plan_rows(lines, payload.fetch("openMasterPlans"))
    lines << ""
    lines << "## Open Plans"
    lines << ""
    append_plan_rows(lines, payload.fetch("openPlans"))
    lines << ""
    lines << "_Routing aid only. Use the underlying plan file for current status._"
    lines.join("\n") + "\n"
  end

  def control_start_markdown(payload)
    plan_index = payload.fetch("planIndex")
    temp_work_products = payload.fetch("tempWorkProducts")
    lines = ["# Control Start", ""]
    lines << "- Plan index: `#{plan_index.fetch("summaryPath")}`"
    lines << "- Audit summary index: `#{payload.dig("auditSummaryIndex", "summaryPath")}`"
    lines << "- Open plans: `#{plan_index.fetch("openCount")}`"
    lines << "- Temp work products: `#{temp_work_products.size}`"
    lines << ""
    lines << "## Open Master Plans"
    lines << ""
    append_plan_rows(lines, plan_index.fetch("openMasterPlans"))
    lines << ""
    lines << "## Temp Work Products"
    lines << ""
    if temp_work_products.empty?
      lines << "- none"
    else
      temp_work_products.each do |item|
        lines << "- `#{item.fetch("path")}` | `#{item.fetch("ownerPlan")}`"
      end
    end
    lines << ""
    lines << "_Routing aid only. Generated history and caches remain archive-only._"
    lines.join("\n") + "\n"
  end

  def append_plan_rows(lines, plans)
    if plans.empty?
      lines << "- none"
      return
    end

    plans.each do |plan|
      label = plan.fetch("goal").empty? ? plan.fetch("title") : plan.fetch("goal")
      lines << "- `#{plan.fetch("path")}` | `#{plan.fetch("status")}` | #{label}"
    end
  end

  def now
    Time.now.utc.iso8601
  end
end

GenerateControlStart.run if $PROGRAM_NAME == __FILE__
