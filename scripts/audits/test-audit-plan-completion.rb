#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"

SOURCE_AUDIT = File.expand_path("audit-plan-completion.rb", __dir__)

def run!(*command, chdir:)
  output, status = Open3.capture2e(*command, chdir: chdir)
  raise "command failed: #{command.join(' ')}\n#{output}" unless status.success?

  output
end

def write(path, content)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, content)
end

def plan(baseline:, code_path:, evidence_baseline: baseline, manifest_decision: "required", manifest_path: ".agents/feature-manifest.yaml")
  <<~MARKDOWN
    ---
    machine_kind: plan
    machine_status: complete
    machine_closeout_contract: 2
    machine_baseline_ref: #{baseline}
    machine_delivery_mode: implementation
    ---

    # Fixture Plan

    ## Status

    Complete.

    ## Workflow Frame

    - Manifest decision: #{manifest_decision}
    - Manifest path: #{manifest_path}

    ## Implementation Checkboxes

    - [x] Implement fixture behavior.

    ## Completion Evidence

    - Status: complete
    - Baseline ref: `#{evidence_baseline}`
    - Implemented code paths: `#{code_path}`
    - Changed files: `#{code_path}` and fixture evidence files.
    - Validation evidence: fixture audit command passed.
    - Doc delta summary: fixture documentation is not applicable.
    - Backlog update: no backlog item is needed.
    - Residual risk: fixture only.
  MARKDOWN
end

def manifest(code_path:)
  <<~YAML
    closeoutContractVersion: 2
    status: complete
    planFile: .agents/feature-plan.md
    checklist:
      codeImplemented: true
    artifacts:
      codePaths:
        - #{code_path}
  YAML
end

def master_plan
  <<~MARKDOWN
    ---
    machine_kind: master_plan
    machine_status: complete
    machine_closeout_contract: 2
    machine_baseline_ref: fixture
    machine_delivery_mode: coordination
    ---

    # Fixture Master Plan

    ## Status

    Complete.

    ## Master Plan Frame

    - Plan inventory: `.agents/child-plan.md`

    ## Initial Analysis

    - Historical reference only: `.agents/ignored-plan.md`

    ## Plan Breakdown

    - [x] Plan 1: `.agents/child-plan.md`

    ## Completion Evidence

    - Status: complete
    - Changed files: child plan and shared fixture code.
    - Validation evidence: fixture master audit passed.
    - Doc delta summary: fixture documentation is not applicable.
    - Backlog update: no backlog item is needed.
    - Residual risk: fixture only.
  MARKDOWN
end

def with_fixture_repo
  Dir.mktmpdir("plan-completion-audit") do |root|
    audit_path = File.join(root, "scripts/audits/audit-plan-completion.rb")
    FileUtils.mkdir_p(File.dirname(audit_path))
    FileUtils.cp(SOURCE_AUDIT, audit_path)
    run!("git", "init", "-q", chdir: root)
    run!("git", "config", "user.email", "fixture@example.test", chdir: root)
    run!("git", "config", "user.name", "Fixture", chdir: root)
    write(File.join(root, "scripts/example.rb"), "puts 'before'\n")
    run!("git", "add", ".", chdir: root)
    run!("git", "commit", "-qm", "baseline", chdir: root)
    baseline = run!("git", "rev-parse", "HEAD", chdir: root).strip
    write(File.join(root, "scripts/example.rb"), "puts 'after'\n")
    yield root, audit_path, baseline
  end
end

def audit(root, audit_path, plan_path: ".agents/feature-plan.md", manifest_path: nil)
  command = ["ruby", audit_path, "plan=#{plan_path}"]
  command << "manifest=#{manifest_path}" if manifest_path
  Open3.capture2e(*command, chdir: root)
end

with_fixture_repo do |root, audit_path, baseline|
  write(File.join(root, ".agents/feature-plan.md"), plan(baseline: baseline, code_path: "scripts/example.rb"))
  write(File.join(root, ".agents/feature-manifest.yaml"), manifest(code_path: "scripts/example.rb"))
  output, status = audit(root, audit_path, manifest_path: ".agents/feature-manifest.yaml")
  raise "positive fixture failed:\n#{output}" unless status.success?
end

with_fixture_repo do |root, audit_path, baseline|
  write(File.join(root, ".agents/feature-plan.md"), plan(baseline: baseline, evidence_baseline: "wrong", code_path: "scripts/example.rb"))
  write(File.join(root, ".agents/feature-manifest.yaml"), manifest(code_path: "scripts/example.rb"))
  output, status = audit(root, audit_path, manifest_path: ".agents/feature-manifest.yaml")
  raise "baseline mismatch fixture unexpectedly passed" if status.success?
  raise "baseline mismatch error missing:\n#{output}" unless output.include?("completion evidence baseline must match machine_baseline_ref")
end

with_fixture_repo do |root, audit_path, baseline|
  write(File.join(root, ".agents/feature-plan.md"), plan(baseline: baseline, code_path: "scripts/example.rb"))
  write(File.join(root, ".agents/feature-manifest.yaml"), manifest(code_path: "scripts/other.rb"))
  output, status = audit(root, audit_path, manifest_path: ".agents/feature-manifest.yaml")
  raise "path linkage fixture unexpectedly passed" if status.success?
  raise "path linkage error missing:\n#{output}" unless output.include?("plan implemented code paths, manifest codePaths, and baseline diff must share at least one path")
end

with_fixture_repo do |root, audit_path, baseline|
  write(File.join(root, ".agents/child-plan.md"), plan(
    baseline: baseline,
    code_path: "scripts/example.rb",
    manifest_decision: "optional",
    manifest_path: "not used"
  ))
  write(File.join(root, ".agents/master-plan.md"), master_plan)
  output, status = audit(root, audit_path, plan_path: ".agents/master-plan.md")
  raise "master inventory fixture failed:\n#{output}" unless status.success?
  raise "master audit treated non-inventory reference as child:\n#{output}" if output.include?("ignored-plan")
end

puts "Plan completion audit fixtures passed"
