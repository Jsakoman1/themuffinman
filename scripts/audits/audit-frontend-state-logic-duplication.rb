#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require_relative "../audit_support"

module FrontendStateLogicDuplicationAudit
  extend self

  ACTIVE_SURFACES_PATH = File.join(AuditSupport::REPO_ROOT, "docs/audit-output/frontend-route-surface-inventory.json")

  WORKFLOW_ACTION_PATTERNS = [
    /applyForQuest/,
    /approveApplication/,
    /declineApplication/,
    /withdrawMyApplication/,
    /createQuestReview/,
    /confirmQuestTermChange/,
    /rejectQuestTermChange/,
    /deleteQuest/,
    /startQuest/,
    /completeQuest/,
    /createCircle(Request)?/,
    /updateConnectionCircles(Bulk)?/,
    /block(User|CircleUser)/,
    /unblock(User|CircleUser)/
  ].freeze

  def run
    active_files = active_frontend_files
    entries = active_files.map { |path| analyze_file(path) }

    report = {
      generated_at: Time.now.utc.iso8601,
      active_file_count: entries.size,
      mutation_runner_overlap: mutation_runner_overlap(entries),
      workflow_action_overlap: workflow_action_overlap(entries),
      dialog_state_overlap: dialog_state_overlap(entries),
      feedback_error_overlap: feedback_error_overlap(entries)
    }

    AuditSupport.write_json("docs/audit-output/frontend-state-logic-duplication-audit.json", report)
    AuditSupport.write_text("docs/audit-output/frontend-state-logic-duplication-audit-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def active_frontend_files
    inventory = JSON.parse(File.read(ACTIVE_SURFACES_PATH))
    files = []
    inventory.fetch("routes", []).each do |route|
      files << route["surface_file"] if route["surface_file"]
      files.concat(route["primary_composables"] || [])
    end
    files.uniq.sort
  end

  def analyze_file(relative_path)
    content = AuditSupport.read(File.join(AuditSupport::REPO_ROOT, relative_path))
    {
      path: relative_path,
      category: AuditSupport.path_category(relative_path),
      mutation_runner_signals: mutation_runner_signals(content),
      workflow_actions: workflow_actions(content),
      dialog_actions: dialog_actions(content),
      feedback_signals: feedback_signals(content)
    }
  end

  def mutation_runner_signals(content)
    signals = []
    signals << "createFeedbackMutationRunner" if content.include?("createFeedbackMutationRunner")
    signals << "createDashboardMutationRunner" if content.include?("createDashboardMutationRunner")
    signals << "createQuestDetailMutationRunner" if content.include?("createQuestDetailMutationRunner")
    signals << "runWithFeedback" if content.include?("runWithFeedback")
    signals << "runQuestMutation" if content.include?("runQuestMutation")
    signals << "runCircleMutation" if content.include?("runCircleMutation")
    signals.uniq.sort
  end

  def workflow_actions(content)
    WORKFLOW_ACTION_PATTERNS.map do |pattern|
      match = content.match(pattern)
      match && match[0]
    end.compact.uniq.sort
  end

  def dialog_actions(content)
    content.scan(/\b(open[A-Z][A-Za-z0-9_]*Dialog|close[A-Z][A-Za-z0-9_]*Dialog)\b/).flatten.uniq.sort
  end

  def feedback_signals(content)
    signals = []
    signals << "getApiErrorMessage" if content.include?("getApiErrorMessage")
    signals << "showFeedback" if content.include?("showFeedback")
    signals << "showMessage" if content.include?("showMessage")
    signals << "requestDebugInfo" if content.include?("buildRequestDebugInfo")
    signals << "error_assignment" if content.match?(/error\.value\s*=/)
    signals.uniq.sort
  end

  def mutation_runner_overlap(entries)
    overlap_for(entries, :mutation_runner_signals, minimum_files: 2)
  end

  def workflow_action_overlap(entries)
    overlap_for(entries, :workflow_actions, minimum_files: 2)
  end

  def dialog_state_overlap(entries)
    overlap_for(entries, :dialog_actions, minimum_files: 2)
  end

  def feedback_error_overlap(entries)
    overlap_for(entries, :feedback_signals, minimum_files: 3)
  end

  def overlap_for(entries, key, minimum_files:)
    groups = Hash.new { |hash, signal| hash[signal] = [] }
    entries.each do |entry|
      entry.fetch(key, []).each do |signal|
        groups[signal] << entry[:path]
      end
    end

    groups
      .map { |signal, files| { signal: signal, files: files.uniq.sort } }
      .select { |entry| entry[:files].size >= minimum_files }
      .sort_by { |entry| [-entry[:files].size, entry[:signal]] }
  end

  def markdown_summary(report)
    lines = []
    lines << "# Frontend State Logic Duplication Audit"
    lines << ""
    lines << "- Decision: `#{[report[:mutation_runner_overlap], report[:workflow_action_overlap], report[:dialog_state_overlap], report[:feedback_error_overlap]].all?(&:empty?) ? "clear" : "review"}`"
    lines << "- Why: active files=#{report[:active_file_count]}"
    lines << "- Next action: review the overlap groups below"
    lines << "- Evidence: mutation=#{report[:mutation_runner_overlap].size}, workflow=#{report[:workflow_action_overlap].size}, dialog=#{report[:dialog_state_overlap].size}, feedback=#{report[:feedback_error_overlap].size}"
    lines << ""

    append_group(lines, "workflow_action_overlap", report["workflow_action_overlap"] || report[:workflow_action_overlap])
    append_group(lines, "feedback_error_overlap", report["feedback_error_overlap"] || report[:feedback_error_overlap])

    lines.join("\n")
  end

  def append_group(lines, title, entries)
    lines << "## `#{title}`"
    lines << ""
    entries.first(5).each do |entry|
      lines << "- `#{entry[:signal] || entry['signal']}` files=`#{(entry[:files] || entry['files']).size}`"
    end
    lines << ""
  end

  def terminal_summary(report)
    lines = []
    lines << "Frontend state logic duplication audit"
    lines << "  active files: #{report[:active_file_count]}"
    lines << "  mutation runner overlaps: #{report[:mutation_runner_overlap].size}"
    lines << "  workflow action overlaps: #{report[:workflow_action_overlap].size}"
    lines << "  dialog overlaps: #{report[:dialog_state_overlap].size}"
    lines << "  feedback overlaps: #{report[:feedback_error_overlap].size}"
    (report[:workflow_action_overlap].first(3) || []).each do |entry|
      lines << "  - #{entry[:signal]} files=#{entry[:files].size}"
    end
    lines.join("\n")
  end

  def format_inline(items)
    return "none" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end
end

FrontendStateLogicDuplicationAudit.run
