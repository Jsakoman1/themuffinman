#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require "time"
require_relative "../local_tooling_common"

module DuplicateLogicAudit
  extend self

  ACTIVE_SURFACES_PATH = File.join(LocalToolingCommon::REPO_ROOT, "docs/generated/local-tooling/frontend-route-surface-inventory.json")
  FRONTEND_ROOT = File.join(LocalToolingCommon::REPO_ROOT, "apps/themuffinman/frontend/src")

  CANONICAL_BACKEND_FILES = {
    "workmarket" => [
      "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketPresentationHelper.java"
    ],
    "social" => [
      "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleViewAssembler.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialPresentationHelper.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java"
    ],
    "identity" => [
      "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java"
    ]
  }.freeze

  RAW_STATE_CHECK = /
    \b(status|relationStatus|ownProfile|blockedByCurrentUser|audience|termMode)\b
    [^\n]{0,80}
    (===|!==|==|!=)
    [^\n]{0,40}
    (?:
      ["'][A-Z][A-Z_-]*["']|
      [A-Z][A-Z_]+|
      true|false
    )
  /x.freeze

  def run
    active_files = active_frontend_files
    candidates = frontend_candidate_files
    analyses = candidates.map { |path| analyze_file(path, active_files.include?(path)) }
    analyses.select! { |entry| interesting?(entry) }

    report = {
      generated_at: Time.now.utc.iso8601,
      scanned_file_count: candidates.size,
      active_route_backed_file_count: active_files.size,
      candidate_count: analyses.size,
      frontend_status_mapping_candidates: analyses.flat_map { |entry| findings_for(entry, :status_mapping_hits) },
      frontend_permission_gate_candidates: analyses.flat_map { |entry| findings_for(entry, :permission_gate_hits) },
      frontend_transition_eligibility_candidates: analyses.flat_map { |entry| findings_for(entry, :transition_eligibility_hits) },
      review_shortlist: analyses
        .sort_by { |entry| [-entry[:score], entry[:path]] }
        .first(20)
        .map { |entry| shortlist_entry(entry) }
    }

    LocalToolingCommon.write_json("docs/generated/local-tooling/duplicate-logic-audit.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/duplicate-logic-audit-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def active_frontend_files
    inventory = JSON.parse(File.read(ACTIVE_SURFACES_PATH))
    files = Set.new
    inventory.fetch("routes", []).each do |route|
      files << route["surface_file"] if route["surface_file"]
      Array(route["primary_composables"]).each { |path| files << path }
    end
    files
  end

  def frontend_candidate_files
    patterns = [
      "apps/themuffinman/frontend/src/modules/**/*.ts",
      "apps/themuffinman/frontend/src/modules/**/*.vue"
    ]

    LocalToolingCommon.repo_glob(*patterns).map { |path| LocalToolingCommon.relative_path(path) }.reject do |path|
      path.include?("/api/") || path.include?("/domain/") || path.include?("/contracts/")
    end
  end

  def analyze_file(relative_path, active_route_backed)
    absolute_path = File.join(LocalToolingCommon::REPO_ROOT, relative_path)
    content = LocalToolingCommon.read(absolute_path)
    lines = content.lines

    module_key = module_key_for(relative_path)
    {
      path: relative_path,
      active_route_backed: active_route_backed,
      module_key: module_key,
      canonical_backend_files: CANONICAL_BACKEND_FILES.fetch(module_key, []),
      status_mapping_hits: status_mapping_hits(lines),
      permission_gate_hits: permission_gate_hits(lines),
      transition_eligibility_hits: transition_eligibility_hits(lines),
      score: score_for(lines, active_route_backed)
    }
  end

  def module_key_for(relative_path)
    return "workmarket" if relative_path.include?("/modules/workmarket/")
    return "social" if relative_path.include?("/modules/social/")
    return "identity" if relative_path.include?("/modules/identity/")

    "unknown"
  end

  def status_mapping_hits(lines)
    hits = []
    lines.each_with_index do |line, index|
      stripped = line.strip
      next unless stripped.match?(/statusLabel|relationLabel|statusBadgeClass|badgeClass/)

      context = lines[index, 5].join(" ")
      next unless context.match?(/\?\?\s*(props\.)?(status|relationStatus)|\{\s*["']?[A-Z][A-Za-z_-]*["']?\s*:/) ||
                  context.match?(/\?\s*"[^"]+"\s*:\s*"[^"]+"/)

      hits << hit(index + 1, stripped)
    end
    hits.uniq
  end

  def permission_gate_hits(lines)
    hits = []
    lines.each_with_index do |line, index|
      stripped = line.strip

      if stripped.match?(/\bconst\s+(can|show|is)[A-Z][A-Za-z0-9_]*\s*=\s*computed/)
        context = lines[index, 6].join(" ")
        next unless context.match?(RAW_STATE_CHECK) ||
                    (context.match?(/(&&|\|\|)/) &&
                      context.match?(/\b(myApplication|applicationsView|applicationSentVisible|blocked|selectedCircleIds)\b/) &&
                      !context.include?(".presentation."))
        next if context.match?(/\?\?\s*false|\?\?\s*null/)

        hits << hit(index + 1, stripped)
        next
      end

      next unless stripped.match?(RAW_STATE_CHECK)
      next if stripped.include?(".presentation.") || stripped.start_with?("import ")

      hits << hit(index + 1, stripped)
    end
    hits.uniq
  end

  def transition_eligibility_hits(lines)
    hits = []
    lines.each_with_index do |line, index|
      stripped = line.strip

      if stripped.match?(/\b(?:const|export const)\s+(canSubmit[A-Z][A-Za-z0-9_]*|can[A-Z][A-Za-z0-9_]*Draft|show[A-Z][A-Za-z0-9_]*Section|[A-Za-z0-9_]+HasChanges)\b/)
        context = lines[[index - 1, 0].max, 8].join(" ")
        next unless context.match?(/\b(richTextHasContent|Number\(|trim\(|scheduledAt|endsAt|selectedCircleIds|applicationSentVisible|myApplication|status|audience|termMode)\b/)

        hits << hit(index + 1, stripped)
      end
    end
    hits.uniq
  end

  def interesting?(entry)
    entry[:status_mapping_hits].any? || entry[:permission_gate_hits].any? || entry[:transition_eligibility_hits].any?
  end

  def findings_for(entry, key)
    entry.fetch(key).map do |finding|
      {
        file: entry[:path],
        active_route_backed: entry[:active_route_backed],
        module_key: entry[:module_key],
        line: finding[:line],
        snippet: finding[:snippet],
        canonical_backend_files: entry[:canonical_backend_files]
      }
    end
  end

  def shortlist_entry(entry)
    {
      file: entry[:path],
      active_route_backed: entry[:active_route_backed],
      module_key: entry[:module_key],
      score: entry[:score],
      status_mapping_count: entry[:status_mapping_hits].size,
      permission_gate_count: entry[:permission_gate_hits].size,
      transition_eligibility_count: entry[:transition_eligibility_hits].size,
      example_findings: (entry[:status_mapping_hits] + entry[:permission_gate_hits] + entry[:transition_eligibility_hits]).first(3),
      canonical_backend_files: entry[:canonical_backend_files]
    }
  end

  def score_for(lines, active_route_backed)
    score = 0
    score += status_mapping_hits(lines).size * 3
    score += permission_gate_hits(lines).size * 2
    score += transition_eligibility_hits(lines).size
    score += 2 if active_route_backed
    score
  end

  def hit(line_number, snippet)
    {
      line: line_number,
      snippet: snippet.strip
    }
  end

  def markdown_summary(report)
    lines = []
    lines << "# Duplicate Logic Audit"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Frontend files scanned: `#{report[:scanned_file_count]}`"
    lines << "- Active route-backed files: `#{report[:active_route_backed_file_count]}`"
    lines << "- Review candidates: `#{report[:candidate_count]}`"
    lines << "- Status mapping hits: `#{report[:frontend_status_mapping_candidates].size}`"
    lines << "- Permission gate hits: `#{report[:frontend_permission_gate_candidates].size}`"
    lines << "- Transition eligibility hits: `#{report[:frontend_transition_eligibility_candidates].size}`"
    lines << ""
    lines << "## Review shortlist"
    lines << ""

    report[:review_shortlist].each do |entry|
      lines << "- `#{entry[:file]}` score=`#{entry[:score]}` active=`#{entry[:active_route_backed]}` status-maps=`#{entry[:status_mapping_count]}` permission-gates=`#{entry[:permission_gate_count]}` transition-helpers=`#{entry[:transition_eligibility_count]}`"
      Array(entry[:example_findings]).each do |finding|
        lines << "  line #{finding[:line]}: `#{finding[:snippet]}`"
      end
      Array(entry[:canonical_backend_files]).each do |path|
        lines << "  backend: `#{path}`"
      end
    end

    lines << ""
    lines << "## Canonical backend anchors"
    lines << ""
    CANONICAL_BACKEND_FILES.each do |module_key, paths|
      lines << "- `#{module_key}` -> #{paths.map { |path| "`#{path}`" }.join(", ")}"
    end

    lines.join("\n") + "\n"
  end

  def terminal_summary(report)
    lines = []
    lines << "Duplicate logic audit"
    lines << "  frontend files scanned: #{report[:scanned_file_count]}"
    lines << "  review candidates: #{report[:candidate_count]}"
    lines << "  status mapping hits: #{report[:frontend_status_mapping_candidates].size}"
    lines << "  permission gate hits: #{report[:frontend_permission_gate_candidates].size}"
    lines << "  transition eligibility hits: #{report[:frontend_transition_eligibility_candidates].size}"
    report[:review_shortlist].first(5).each do |entry|
      lines << "  - #{entry[:file]} score=#{entry[:score]}"
    end
    lines.join("\n")
  end
end

DuplicateLogicAudit.run
