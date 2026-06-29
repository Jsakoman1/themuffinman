#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require_relative "../local_tooling_common"

DECISIONS = [
  {
    id: "backend-centric-domain-logic",
    title: "Keep business rules in backend services",
    decision: "Permissions, validations, workflow rules, state transitions, and automation assumptions belong in backend services so web and future mobile clients share the same behavior.",
    applies_to: %w[backend frontend automation],
    source_paths: %w[AGENTS.md docs/domain-technical.md docs/change-completion-checklist.md]
  },
  {
    id: "thin-controllers",
    title: "Keep controllers transport-only",
    decision: "Controllers should stay thin and delegate behavior, DTO assembly, and permission decisions to service or mapper layers.",
    applies_to: %w[backend],
    source_paths: %w[AGENTS.md docs/domain-technical.md]
  },
  {
    id: "docs-sync-required",
    title: "Logic changes move with living docs",
    decision: "A logic change is not complete when only code and tests are updated; affected docs, agent artifacts, and validation tests move in the same change.",
    applies_to: %w[docs backend frontend automation],
    source_paths: %w[AGENTS.md docs/documentation-sync-policy.md docs/change-completion-checklist.md]
  },
  {
    id: "generated-artifacts-authoritative",
    title: "Generated source-of-truth artifacts must stay fresh",
    decision: "Generated endpoint, read-model, backend-audit, source-of-truth, and frontend contract artifacts are authoritative review inputs when their source surfaces change.",
    applies_to: %w[generated-artifacts automation frontend backend],
    source_paths: %w[docs/generated/artifact-policy.yaml docs/domain-technical.md docs/change-completion-checklist.md]
  },
  {
    id: "flyway-forward-only",
    title: "Schema changes use new Flyway migrations",
    decision: "Schema changes add a new Flyway migration instead of editing old migrations.",
    applies_to: %w[database backend],
    source_paths: %w[AGENTS.md docs/change-completion-checklist.md]
  },
  {
    id: "sandbox-production-separation",
    title: "Sandbox behavior stays separate",
    decision: "Sandbox and synthetic admin-generation behavior must stay explicitly separated from production-like user flows and semantics.",
    applies_to: %w[sandbox automation docs],
    source_paths: %w[AGENTS.md docs/documentation-sync-policy.md docs/agent-operating-model.md]
  },
  {
    id: "no-commit-without-explicit-request",
    title: "No commit or push by default",
    decision: "Do not commit or push changes unless the user explicitly requests it.",
    applies_to: %w[workflow git],
    source_paths: %w[AGENTS.md]
  },
  {
    id: "plan-backed-high-risk-work",
    title: "High-risk work needs plan-backed closeout",
    decision: "Multi-file, multi-layer, high-risk, master-plan-driven, or manifest-backed changes need explicit plan evidence, validation evidence, backlog hygiene, and closeout checks.",
    applies_to: %w[workflow docs automation],
    source_paths: %w[docs/documentation-sync-policy.md docs/change-completion-checklist.md docs/feature-completion-manifest.schema.json]
  },
  {
    id: "typed-operational-config",
    title: "Centralize operational config",
    decision: "Operational backend settings such as TTLs, cleanup jobs, limits, polling intervals, and timeouts belong in typed central configuration objects.",
    applies_to: %w[backend config],
    source_paths: %w[AGENTS.md]
  },
  {
    id: "frontend-shared-shell",
    title: "Standardize authenticated frontend surfaces",
    decision: "Authenticated pages should use shared app shell, shared components, shared tokens, and minimal UI-side product logic.",
    applies_to: %w[frontend],
    source_paths: %w[AGENTS.md apps/themuffinman/frontend/README.md]
  }
].freeze

report = {
  generated_at: Time.now.utc.iso8601,
  decision_count: DECISIONS.size,
  decisions: DECISIONS,
  read_first: %w[
    AGENTS.md
    docs/domain-technical.md
    docs/documentation-sync-policy.md
    docs/change-completion-checklist.md
    docs/generated/artifact-policy.yaml
  ]
}

LocalToolingCommon.write_json("docs/generated/local-tooling/architecture-decision-index.json", report)

lines = ["# Architecture Decision Index", ""]
lines << "- Generated at: `#{report[:generated_at]}`"
lines << "- Decisions: `#{report[:decision_count]}`"
lines << ""
DECISIONS.each do |decision|
  lines << "## `#{decision[:id]}`"
  lines << ""
  lines << "- #{decision[:title]}"
  lines << "- Applies to: `#{decision[:applies_to].join('`, `')}`"
  lines << ""
end
LocalToolingCommon.write_text("docs/generated/local-tooling/architecture-decision-index-summary.md", lines.join("\n"))

puts "Architecture decision index"
puts "  decisions: #{DECISIONS.size}"
