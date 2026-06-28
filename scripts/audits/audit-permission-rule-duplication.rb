#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require "time"
require_relative "../local_tooling_common"

module PermissionRuleDuplicationAudit
  extend self

  ACTIVE_SURFACES_PATH = File.join(LocalToolingCommon::REPO_ROOT, "docs/generated/local-tooling/frontend-route-surface-inventory.json")

  CANONICAL_BACKEND_FILES = [
    "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java",
    "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java",
    "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java",
    "apps/themuffinman/src/main/java/com/themuffinman/app/social/service/SocialRelationActionHelper.java",
    "apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java"
  ].freeze

  ACTION_KEY_PATTERNS = {
    "EDIT" => /\bcanEdit\b|QuestAllowedAction\.EDIT|ApplicationAllowedAction\.EDIT/,
    "APPLY" => /\bcanApply\b|QuestAllowedAction\.APPLY/,
    "VIEW_APPLICATIONS" => /\bcanViewApplications\b|QuestAllowedAction\.VIEW_APPLICATIONS/,
    "DELETE" => /\bdeleteVisible\b|QuestAllowedAction\.DELETE/,
    "START" => /\bcanStart\b|QuestAllowedAction\.START/,
    "COMPLETE" => /\bcanComplete\b|QuestAllowedAction\.COMPLETE/,
    "CONFIRM_TERM_CHANGE" => /\b(canRespondToTermChange|termChangeConfirmLabel)\b|QuestAllowedAction\.CONFIRM_TERM_CHANGE/,
    "REJECT_TERM_CHANGE" => /\b(canRespondToTermChange|termChangeRejectLabel)\b|QuestAllowedAction\.REJECT_TERM_CHANGE/,
    "WITHDRAW" => /\bcanWithdraw\b|ApplicationAllowedAction\.WITHDRAW/,
    "APPROVE" => /\bcanApprove\b|ApplicationAllowedAction\.APPROVE|ACCEPT_REQUEST|canAcceptCircleRequest/,
    "DECLINE" => /\bcanDecline\b|ApplicationAllowedAction\.DECLINE|DECLINE_REQUEST|canDeclineCircleRequest/,
    "BLOCK" => /\b(showBlockAction|blockActionEnabled)\b|\bBLOCK\b/,
    "UNBLOCK" => /\bUNBLOCK\b/,
    "SEND_INVITE" => /\bSEND_INVITE\b/,
    "OPEN_CIRCLES" => /\bOPEN_CIRCLES\b/,
    "PRIMARY_ACTION" => /\bprimaryAction\b/,
    "SECONDARY_ACTION" => /\bsecondaryAction\b/
  }.freeze

  LOCAL_PERMISSION_SIGNAL = /
    canApply|canEdit|canWithdraw|canApprove|canDecline|canViewApplications|canOpenMyApplication|
    showBlockAction|blockActionEnabled|primaryAction|secondaryAction|
    applicationSentVisible|myApplication|applicationsView|selectedCircleIds
  /x.freeze

  def run
    active_frontend_files = active_frontend_files()
    backend_entries = backend_entries()
    frontend_entries = frontend_entries(active_frontend_files)

    report = {
      generated_at: Time.now.utc.iso8601,
      active_route_backed_file_count: active_frontend_files.size,
      backend_permission_source_count: backend_entries[:sources].size,
      backend_presentation_flag_count: backend_entries[:presentation_flags].size,
      frontend_passthrough_gate_count: frontend_entries[:passthrough].size,
      frontend_local_gate_count: frontend_entries[:local].size,
      backend_permission_sources: backend_entries[:sources],
      backend_presentation_flags: backend_entries[:presentation_flags],
      frontend_passthrough_gates: frontend_entries[:passthrough],
      frontend_local_gates: frontend_entries[:local],
      cross_layer_overlap_shortlist: overlap_shortlist(backend_entries, frontend_entries)
    }

    LocalToolingCommon.write_json("docs/generated/local-tooling/permission-rule-duplication-audit.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/permission-rule-duplication-audit-summary.md", markdown_summary(report))
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

  def backend_entries
    files = LocalToolingCommon.repo_glob(
      "apps/themuffinman/src/main/java/com/themuffinman/app/**/*Service.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/**/*Assembler.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/**/*Mgr.java",
      "apps/themuffinman/src/main/java/com/themuffinman/app/**/*Controller.java"
    ).map { |path| LocalToolingCommon.relative_path(path) }

    sources = []
    presentation_flags = []

    files.each do |relative_path|
      lines = LocalToolingCommon.read(File.join(LocalToolingCommon::REPO_ROOT, relative_path)).lines
      lines.each_with_index do |line, index|
        stripped = line.strip

        if permission_source_line?(stripped)
          sources << build_entry(relative_path, index + 1, stripped, action_keys_for(stripped), "backend_source")
        end

        if backend_presentation_flag_line?(stripped)
          presentation_flags << build_entry(relative_path, index + 1, stripped, action_keys_for(stripped), "backend_presentation")
        end
      end
    end

    {
      sources: dedupe_entries(sources),
      presentation_flags: dedupe_entries(presentation_flags)
    }
  end

  def frontend_entries(active_frontend_files)
    files = LocalToolingCommon.repo_glob(
      "apps/themuffinman/frontend/src/modules/**/*.ts",
      "apps/themuffinman/frontend/src/modules/**/*.vue",
      "apps/themuffinman/frontend/src/components/app/**/*.vue"
    ).map { |path| LocalToolingCommon.relative_path(path) }.reject do |path|
      path.include?("/api/") || path.include?("/domain/") || path.include?("/contracts/")
    end

    passthrough = []
    local = []

    files.each do |relative_path|
      lines = LocalToolingCommon.read(File.join(LocalToolingCommon::REPO_ROOT, relative_path)).lines
      lines.each_with_index do |line, index|
        stripped = line.strip
        active_route_backed = active_frontend_files.include?(relative_path)

        if frontend_passthrough_line?(stripped)
          passthrough << build_entry(relative_path, index + 1, stripped, action_keys_for(stripped), "frontend_passthrough", active_route_backed)
        end

        if frontend_local_gate_line?(stripped)
          local << build_entry(relative_path, index + 1, stripped, action_keys_for(stripped), "frontend_local", active_route_backed)
        end
      end
    end

    {
      passthrough: dedupe_entries(passthrough),
      local: dedupe_entries(local)
    }
  end

  def permission_source_line?(stripped)
    return true if stripped.match?(/\bresolveAllowedActions\b|\brequestActions\b|\bsearchActions\b|\bprofilePrimaryAction\b/)
    return true if stripped.match?(/allowedActions\.add\(/)
    return true if stripped.match?(/action\("([A-Z_]+)"/)

    false
  end

  def backend_presentation_flag_line?(stripped)
    return false unless stripped.match?(/\.(can[A-Z]|show[A-Z]|primaryAction|secondaryAction|blockActionEnabled|showBlockAction|deleteVisible)/)
    return true if stripped.include?("allowedActions.contains(")
    return true if stripped.match?(/relationStatus\(\)|ownProfile|blockedByCurrentUser|getStatus\(\)/)

    false
  end

  def frontend_passthrough_line?(stripped)
    stripped.match?(/presentation\.(can[A-Z]|show[A-Z]|deleteVisible|postingSettingsVisible|canAcceptCircleRequest|canDeclineCircleRequest)|\bprimaryAction\b|\bsecondaryAction\b/)
  end

  def frontend_local_gate_line?(stripped)
    return false unless stripped.match?(LOCAL_PERMISSION_SIGNAL)
    return true if stripped.match?(/v-if=.*(quest\.status|audience ===|relationStatus|ownProfile|blockedByCurrentUser|applicationSentVisible|myApplication)/)
    return true if stripped.match?(/\bconst\s+(can|show|is)[A-Z][A-Za-z0-9_]*\s*=\s*computed/) &&
      stripped.match?(/applicationsView|myApplication|applicationSentVisible|isOwnProfile|selectedCircleIds/)
    return true if stripped.match?(/\bif\s*\(([^)]*(quest\.status|audience ===|isOwnProfile|relationStatus|blockedByCurrentUser|applicationSentVisible|myApplication)[^)]*)\)/)

    false
  end

  def action_keys_for(snippet)
    return %w[APPROVE DECLINE PRIMARY_ACTION SECONDARY_ACTION] if snippet.include?("requestActions(")
    return %w[SEND_INVITE BLOCK UNBLOCK PRIMARY_ACTION SECONDARY_ACTION] if snippet.include?("searchActions(")
    return %w[EDIT APPLY VIEW_APPLICATIONS DELETE START COMPLETE CONFIRM_TERM_CHANGE REJECT_TERM_CHANGE] if snippet.include?("resolveAllowedActions(")

    keys = ACTION_KEY_PATTERNS.each_with_object([]) do |(action_key, pattern), matches|
      matches << action_key if snippet.match?(pattern)
    end
    keys.empty? ? ["UNKNOWN"] : keys
  end

  def build_entry(file, line, snippet, action_keys, kind, active_route_backed = nil)
    entry = {
      file: file,
      line: line,
      snippet: snippet,
      action_keys: action_keys,
      kind: kind
    }
    entry[:active_route_backed] = active_route_backed unless active_route_backed.nil?
    entry
  end

  def dedupe_entries(entries)
    seen = Set.new
    entries.each_with_object([]) do |entry, unique|
      identity = [entry[:file], entry[:line], entry[:snippet], entry[:kind]]
      next if seen.include?(identity)

      seen << identity
      unique << entry
    end
  end

  def overlap_shortlist(backend_entries, frontend_entries)
    grouped = Hash.new { |hash, key| hash[key] = {backend_sources: [], backend_presentation: [], frontend_passthrough: [], frontend_local: []} }

    backend_entries[:sources].each do |entry|
      entry[:action_keys].each { |key| grouped[key][:backend_sources] << entry }
    end
    backend_entries[:presentation_flags].each do |entry|
      entry[:action_keys].each { |key| grouped[key][:backend_presentation] << entry }
    end
    frontend_entries[:passthrough].each do |entry|
      entry[:action_keys].each { |key| grouped[key][:frontend_passthrough] << entry }
    end
    frontend_entries[:local].each do |entry|
      entry[:action_keys].each { |key| grouped[key][:frontend_local] << entry }
    end

    grouped
      .map do |action_key, buckets|
        {
          action_key: action_key,
          backend_source_count: buckets[:backend_sources].size,
          backend_presentation_count: buckets[:backend_presentation].size,
          frontend_passthrough_count: buckets[:frontend_passthrough].size,
          frontend_local_count: buckets[:frontend_local].size,
          backend_source_files: buckets[:backend_sources].map { |entry| entry[:file] }.uniq.sort,
          frontend_local_files: buckets[:frontend_local].map { |entry| entry[:file] }.uniq.sort,
          frontend_passthrough_files: buckets[:frontend_passthrough].map { |entry| entry[:file] }.uniq.sort
        }
      end
      .select { |entry| entry[:action_key] != "UNKNOWN" && entry[:frontend_local_count] > 0 && (entry[:backend_source_count] > 0 || entry[:backend_presentation_count] > 0) }
      .sort_by { |entry| [-(entry[:frontend_local_count] + entry[:backend_source_count] + entry[:backend_presentation_count]), entry[:action_key]] }
      .first(20)
  end

  def markdown_summary(report)
    lines = []
    lines << "# Permission Rule Duplication Audit"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Backend permission sources: `#{report[:backend_permission_source_count]}`"
    lines << "- Backend presentation flags: `#{report[:backend_presentation_flag_count]}`"
    lines << "- Frontend passthrough gates: `#{report[:frontend_passthrough_gate_count]}`"
    lines << "- Frontend local gates: `#{report[:frontend_local_gate_count]}`"
    lines << ""
    lines << "## Overlap shortlist"
    lines << ""
    report[:cross_layer_overlap_shortlist].each do |entry|
      lines << "- `#{entry[:action_key]}` backend-sources=`#{entry[:backend_source_count]}` backend-presentation=`#{entry[:backend_presentation_count]}` frontend-local=`#{entry[:frontend_local_count]}` frontend-passthrough=`#{entry[:frontend_passthrough_count]}`"
      lines << "  backend source files: #{format_inline(entry[:backend_source_files])}"
      lines << "  frontend local files: #{format_inline(entry[:frontend_local_files])}"
      lines << "  frontend passthrough files: #{format_inline(entry[:frontend_passthrough_files])}"
    end
    lines << ""
    lines << "## Canonical backend anchors"
    lines << ""
    CANONICAL_BACKEND_FILES.each do |path|
      lines << "- `#{path}`"
    end
    lines.join("\n") + "\n"
  end

  def terminal_summary(report)
    lines = []
    lines << "Permission rule duplication audit"
    lines << "  backend permission sources: #{report[:backend_permission_source_count]}"
    lines << "  backend presentation flags: #{report[:backend_presentation_flag_count]}"
    lines << "  frontend passthrough gates: #{report[:frontend_passthrough_gate_count]}"
    lines << "  frontend local gates: #{report[:frontend_local_gate_count]}"
    report[:cross_layer_overlap_shortlist].first(5).each do |entry|
      lines << "  - #{entry[:action_key]} local=#{entry[:frontend_local_count]} backend=#{entry[:backend_source_count] + entry[:backend_presentation_count]}"
    end
    lines.join("\n")
  end

  def format_inline(items)
    return "none" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end
end

PermissionRuleDuplicationAudit.run
