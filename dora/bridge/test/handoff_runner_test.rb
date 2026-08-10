#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "stringio"
require "tmpdir"
require "yaml"
require_relative "../../lib/dora/handoff"
require_relative "../../lib/dora/handoff_runner"

ROOT = File.expand_path("../..", __dir__)

def registry(root)
  path = File.join(root, "projects.yaml")
  projects = %w[dora doomsday-storage].map { |id| {"id" => id, "adapter_path" => "unused/.dora/project.yaml", "capabilities" => {"handoff_write" => true}} }
  File.write(path, YAML.dump({"kind" => "dora_bridge_projects", "version" => 1, "projects" => projects}))
  path
end

def launcher(root, exit_code: 0, output: nil, complete: false, state_root: nil, stdin_probe: nil, environment_probe: nil, launch_record: nil, feedback_phases: [], feedback_pause: 0, delivery: nil)
  path = File.join(root, "fixed-launcher-#{exit_code}-#{complete ? "complete" : "terminal"}-#{SecureRandom.hex(4)}")
  body = [
    "abort unless ARGV.length == 2 && ARGV.first == '--dora-preclaimed-handoff' && ARGV[1] =~ /\\Ahandoff-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\z/",
    "stdin = STDIN.read",
    ("File.write(#{stdin_probe.inspect}, stdin.empty? ? 'closed' : 'open')" if stdin_probe),
    ("File.open(#{launch_record.inspect}, 'a') { |file| file.puts ARGV.fetch(1) }" if launch_record),
    ("puts #{output.inspect}" if output),
    ("puts ENV.fetch(#{environment_probe.inspect})" if environment_probe)
  ].compact
  if delivery
    body.concat([
      "require #{File.join(ROOT, "lib/dora/handoff").inspect}",
      "store = Dora::Handoff.new(state_root: #{state_root.inspect})",
      "handoff = store.get!(id: ARGV.fetch(1))",
      "store.link_delivery!(id: ARGV.fetch(1), project: handoff.fetch('project'), master_plan: #{delivery.fetch("master_plan").inspect}, work_plan: #{delivery["work_plan"].inspect})"
    ])
  end
  unless feedback_phases.empty?
    body.concat([
      "require #{File.join(ROOT, "lib/dora/handoff").inspect}",
      "store = Dora::Handoff.new(state_root: #{state_root.inspect})",
      "handoff = store.get!(id: ARGV.fetch(1))",
      "project = handoff.fetch('project')"
    ])
    feedback_phases.each_with_index do |phase, index|
      verification_state = phase == "IMPLEMENTING" ? "IMPLEMENTED_UNVERIFIED" : "NOT_STARTED"
      feedback = {"phase" => phase, "milestone" => "Synthetic #{index}", "progress" => "Synthetic progress", "finding" => "Synthetic #{index}", "verification_state" => verification_state, "deviations" => [], "residual_risks" => []}
      body << "store.feedback!(id: ARGV.fetch(1), project: project, feedback: #{feedback.inspect})"
      body << "sleep #{feedback_pause}" if feedback_pause.positive?
    end
  end
  if complete
    body.concat([
      "require #{File.join(ROOT, "lib/dora/handoff").inspect}",
      "store = Dora::Handoff.new(state_root: #{state_root.inspect})",
      "handoff = store.get!(id: ARGV.fetch(1))",
      "project = handoff.fetch('project')"
    ])
    body << "store.link_delivery!(id: ARGV.fetch(1), project: project, master_plan: 'docs/work/synthetic-master.yaml')" unless delivery
    body << "store.complete!(id: ARGV.fetch(1), project: project, verification_references: ['docs/work/synthetic.yaml#verify'])"
  end
  body << "exit #{exit_code}"
  File.write(path, "#!/usr/bin/env ruby\n#{body.join("\n")}\n")
  FileUtils.chmod("u+x", path)
  path
end

Dir.mktmpdir("dora-handoff-runner") do |root|
  registry_path = registry(root)
  state_root = File.join(root, "private")
  clock = Time.utc(2026, 8, 10, 13, 0, 0)
  store = Dora::Handoff.new(state_root: state_root, now: -> { clock })
  later = store.create!(project: "doomsday-storage", title: "Later", objective: "later", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "later")
  clock += 1
  ready = store.create!(project: "dora", title: "Ready", objective: "ready", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "ready")
  output = StringIO.new
  runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: %w[dora doomsday-storage], launcher: launcher(root), now: -> { clock }, output: output)
  result = runner.run_once
  final = store.get!(id: later.fetch("id"), project: "doomsday-storage")
  abort "runner did not choose, claim, and safely block deterministic oldest handoff" unless result.fetch("status") == "BLOCKED" && final.fetch("status") == "BLOCKED" && final.fetch("feedback_history").length == 2 && final.fetch("feedback_history").first.fetch("phase") == "START"
  abort "zero-exit missing-completion failure was not structured" unless final.dig("blocked", "runner_failure") == {"code" => "completion_missing", "exit_code" => 0, "recovery_hint" => Dora::HandoffRunner::FAILURE_HINTS.fetch("completion_missing")}
  abort "runner terminal did not show safe lifecycle transitions" unless output.string.include?("Eligible handoff detected and claimed.") && output.string.include?("Handoff phase: Start.") && output.string.include?("Handoff phase: Blocked.") && output.string.include?("Handoff blocked after runner failure; owner review is required.")
  abort "runner terminal retained noisy polling output" if output.string.match?(/Polling for READY handoffs|No eligible READY handoff/)
  abort "runner launched a second handoff" unless store.next_ready!(project: "dora").fetch("status") == "READY"

  secret = "DO_NOT_STORE_RUNNER_SECRET"
  private_path = "/private/owner/local/path"
  original_secret = ENV["RUNNER_TEST_SECRET"]
  ENV["RUNNER_TEST_SECRET"] = secret
  begin
    failure_output = StringIO.new
    failure_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, exit_code: Dora::HandoffRunner::LAUNCHER_PREFLIGHT_EXIT, output: private_path, environment_probe: "RUNNER_TEST_SECRET"), now: -> { clock }, output: failure_output)
    failure_result = failure_runner.run_once
    failed = store.get!(id: ready.fetch("id"), project: "dora")
    expected_failure = {"code" => "launcher_preflight_failed", "exit_code" => Dora::HandoffRunner::LAUNCHER_PREFLIGHT_EXIT, "recovery_hint" => Dora::HandoffRunner::FAILURE_HINTS.fetch("launcher_preflight_failed")}
    abort "launcher preflight failure did not retain a sanitized diagnostic" unless failure_result.fetch("status") == "BLOCKED" && failed.dig("blocked", "runner_failure") == expected_failure
    abort "runner terminal leaked child output, a private path, or an environment value" if [failure_output.string, failed.to_s, failure_runner.status.to_s].any? { |value| value.include?(secret) || value.include?(private_path) }
    abort "runner terminal did not show a fixed blocked outcome" unless failure_output.string.include?("Handoff phase: Blocked.") && failure_output.string.include?("Handoff blocked after runner failure; owner review is required.")
  ensure
    original_secret.nil? ? ENV.delete("RUNNER_TEST_SECRET") : ENV["RUNNER_TEST_SECRET"] = original_secret
  end

  successful = store.create!(project: "dora", title: "Non-interactive success", objective: "complete", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "noninteractive-success")
  stdin_probe = File.join(root, "stdin-probe")
  success_output = StringIO.new
  success_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root, stdin_probe: stdin_probe), now: -> { clock }, output: success_output)
  success_result = success_runner.run_once
  completed = store.get!(id: successful.fetch("id"), project: "dora")
  abort "successful non-interactive invocation did not close stdin" unless File.read(stdin_probe) == "closed"
  abort "runner passed more than the fixed preclaimed UUID interface" unless completed.fetch("id").match?(Dora::Handoff::HANDOFF_ID)
  abort "successful non-interactive invocation did not preserve completion/readback" unless success_result.fetch("status") == "COMPLETED" && completed.fetch("status") == "COMPLETED" && success_output.string.include?("Handoff phase: Complete.") && success_output.string.include?("Handoff completed.")

  codex_failure = store.create!(project: "dora", title: "Codex execution failure", objective: "fail after launcher preflight", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "codex-execution-failure")
  execution_output = StringIO.new
  execution_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, exit_code: Dora::HandoffRunner::CODEX_EXECUTION_FAILURE_EXIT), now: -> { clock }, output: execution_output)
  execution_result = execution_runner.run_once
  execution_failed = store.get!(id: codex_failure.fetch("id"), project: "dora")
  expected_execution_failure = {"code" => "codex_execution_failed", "exit_code" => Dora::HandoffRunner::CODEX_EXECUTION_FAILURE_EXIT, "recovery_hint" => Dora::HandoffRunner::FAILURE_HINTS.fetch("codex_execution_failed")}
  abort "runner did not distinguish Codex execution failure from launcher preflight failure" unless execution_result.fetch("status") == "BLOCKED" && execution_failed.dig("blocked", "runner_failure") == expected_execution_failure && execution_failed.dig("blocked", "runner_failure", "code") != "launcher_preflight_failed"

  idle_output = StringIO.new
  idle_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root), now: -> { clock }, sleeper: ->(_seconds) {}, output: idle_output)
  idle_runner.watch(interval_seconds: 1, max_cycles: 2)
  abort "idle runner did not print exactly one concise readiness message" unless idle_output.string.lines.grep("Dora handoff runner ready; waiting for eligible handoffs.\n").length == 1
  abort "idle runner repeated an idle poll message" if idle_output.string.match?(/No eligible READY handoff|Polling for READY handoffs/)

  progressing = store.create!(project: "dora", title: "Progress transitions", objective: "show safe progress", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "progress-transitions")
  progress_output = StringIO.new
  progress_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root, feedback_phases: %w[DISCOVERY IMPLEMENTING], feedback_pause: 0.05), now: -> { clock }, sleeper: ->(_seconds) { sleep(0.02) }, output: progress_output)
  progress_result = progress_runner.run_once
  abort "runner did not retain normal completion after progress polling" unless progress_result.fetch("status") == "COMPLETED" && store.get!(id: progressing.fetch("id"), project: "dora").fetch("status") == "COMPLETED"
  %w[Start Analysis Implementation Complete].each do |label|
    abort "runner did not emit exactly one #{label} phase transition" unless progress_output.string.scan("Handoff phase: #{label}.").length == 1
  end

  canonical = store.create!(project: "dora", title: "Canonical progress", objective: "show Master Plan progress", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "canonical-master-progress")
  canonical_output = StringIO.new
  canonical_reader_calls = 0
  canonical_projection = {"master_plan" => {"id" => "canonical-master", "title" => "Canonical Master Plan", "reference" => "docs/work/canonical-master.yaml"}, "items" => [{"id" => "verified-step", "status" => "verified", "task" => {"id" => "verified-task", "title" => "Verified task", "reference" => "docs/work/verified.yaml"}}, {"id" => "current-step", "status" => "in_progress", "task" => {"id" => "current-task", "title" => "Current task", "reference" => "docs/work/current.yaml"}}, {"id" => "pending-step", "status" => "pending", "task" => {"id" => "pending-task", "title" => "Pending task", "reference" => "docs/work/pending.yaml"}}, {"id" => "blocked-step", "status" => "blocked", "task" => {"id" => "blocked-task", "title" => "Blocked task", "reference" => "docs/work/blocked.yaml"}}], "material_change_token" => "a" * 64}
  canonical_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root, feedback_phases: ["IMPLEMENTING"], feedback_pause: 0.05, delivery: {"master_plan" => "docs/work/canonical-master.yaml", "work_plan" => "docs/work/current.yaml"}), now: -> { clock }, sleeper: ->(_seconds) { sleep(0.02) }, output: canonical_output, master_plan_progress_reader: ->(_project, _handoff_id, _delivery) { canonical_reader_calls += 1; canonical_projection })
  canonical_result = canonical_runner.run_once
  abort "canonical Master Plan progress changed runner completion" unless canonical_result.fetch("status") == "COMPLETED" && store.get!(id: canonical.fetch("id"), project: "dora").fetch("status") == "COMPLETED"
  abort "runner did not render the canonical Master Plan checklist" unless canonical_reader_calls.positive? && canonical_output.string.include?("Master Plan: Canonical Master Plan (canonical-master)") && canonical_output.string.include?("[x] Verified task (verified-task) — verified") && canonical_output.string.include?("[>] Current task (current-task) — current") && canonical_output.string.include?("[ ] Pending task (pending-task) — pending") && canonical_output.string.include?("[!] Blocked task (blocked-task) — blocked")
  abort "runner printed a canonical progress reference" if canonical_output.string.include?("docs/work/")

  invalid_master = store.create!(project: "dora", title: "Invalid Master Plan progress", objective: "hide invalid Master Plan progress", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "invalid-master-progress")
  invalid_master_secret = "DO_NOT_PRINT_INVALID_MASTER_PROGRESS"
  invalid_master_output = StringIO.new
  invalid_master_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root, delivery: {"master_plan" => "docs/work/invalid-master.yaml"}), now: -> { clock }, output: invalid_master_output, master_plan_progress_reader: ->(_project, _handoff_id, _delivery) { {"master_plan" => {"id" => invalid_master_secret}, "items" => [], "material_change_token" => invalid_master_secret} })
  invalid_master_result = invalid_master_runner.run_once
  abort "invalid Master Plan progress changed runner completion" unless invalid_master_result.fetch("status") == "COMPLETED" && store.get!(id: invalid_master.fetch("id"), project: "dora").fetch("status") == "COMPLETED"
  abort "invalid Master Plan progress produced terminal output" if invalid_master_output.string.include?("Master Plan:") || invalid_master_output.string.include?(invalid_master_secret)

  invalid = store.create!(project: "dora", title: "Invalid progress", objective: "hide invalid progress", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "invalid-progress")
  invalid_secret = "DO_NOT_PRINT_INVALID_PROGRESS"
  invalid_output = StringIO.new
  invalid_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root), now: -> { clock }, output: invalid_output, progress_reader: ->(_project, _handoff_id) { {"phase" => invalid_secret, "label" => invalid_secret, "material_change_token" => invalid_secret} })
  invalid_result = invalid_runner.run_once
  abort "invalid progress changed runner completion behavior" unless invalid_result.fetch("status") == "COMPLETED" && store.get!(id: invalid.fetch("id"), project: "dora").fetch("status") == "COMPLETED"
  abort "invalid progress was not safely deduplicated" unless invalid_output.string.scan("Handoff progress is unavailable.").length == 1
  abort "invalid progress leaked arbitrary text" if invalid_output.string.include?(invalid_secret)

  unavailable = store.create!(project: "dora", title: "Unavailable progress", objective: "hide unavailable progress", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "unavailable-progress")
  unavailable_secret = "DO_NOT_PRINT_UNAVAILABLE_PROGRESS"
  unavailable_output = StringIO.new
  unavailable_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root), now: -> { clock }, output: unavailable_output, progress_reader: ->(_project, _handoff_id) { raise ArgumentError, unavailable_secret })
  unavailable_result = unavailable_runner.run_once
  abort "unavailable progress changed runner completion behavior" unless unavailable_result.fetch("status") == "COMPLETED" && store.get!(id: unavailable.fetch("id"), project: "dora").fetch("status") == "COMPLETED"
  abort "unavailable progress was not safely deduplicated" unless unavailable_output.string.scan("Handoff progress is unavailable.").length == 1
  abort "unavailable progress leaked arbitrary text" if unavailable_output.string.include?(unavailable_secret)

  begin
    Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora;touch-pwned"], launcher: launcher(root))
    abort "command-like project input was accepted"
  rescue Dora::HandoffRunner::Error
    nil
  end
  begin
    Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["missing"], launcher: launcher(root))
    abort "cross-project input was accepted"
  rescue Dora::HandoffRunner::Error
    nil
  end

  File.open(File.join(state_root, ".handoff-runner.lock"), File::RDWR | File::CREAT, 0o600) do |lock|
    lock.flock(File::LOCK_EX)
    begin
      runner.run_once
      abort "duplicate runner was accepted"
    rescue Dora::HandoffRunner::Error
      nil
    end
  end

  File.write(File.join(state_root, "handoff-runner-state.json"), JSON.generate({"status" => "RUNNING", "pid" => 999_999_999, "handoff" => {"id" => later.fetch("id"), "project" => "doomsday-storage"}}))
  File.chmod(0o600, File.join(state_root, "handoff-runner-state.json"))
  recovered = runner.run_once
  abort "stale runner state did not fail closed" unless recovered.fetch("status") == "RECOVERY_REQUIRED"

  runner.request_stop!
  abort "narrow local stop operation failed" unless File.file?(File.join(state_root, "handoff-runner-stop")) && (File.stat(File.join(state_root, "handoff-runner-stop")).mode & 0o077).zero?
end

Dir.mktmpdir("dora-handoff-runner-restart") do |root|
  registry_path = registry(root)
  state_root = File.join(root, "private")
  clock = Time.utc(2026, 8, 10, 14, 0, 0)
  store = Dora::Handoff.new(state_root: state_root, now: -> { clock })
  previously_claimed = store.create!(project: "dora", title: "Interrupted claimed work", objective: "must not replay", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "restart-claimed")
  clock += 1
  previously_claimed = store.claim!(id: previously_claimed.fetch("id"), project: "dora", claimed_by: "codex")
  claimed_snapshot = previously_claimed.slice("status", "lifecycle", "feedback_history", "delivery", "completion")
  clock += 1
  ready = store.create!(project: "dora", title: "Ready after restart", objective: "runs normally", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "restart-ready")
  launch_record = File.join(root, "launched-handoffs")
  restart_output = StringIO.new

  restarted_runner = Dora::HandoffRunner.new(registry_path: registry_path, state_root: state_root, projects: ["dora"], launcher: launcher(root, complete: true, state_root: state_root, launch_record: launch_record), now: -> { clock }, output: restart_output)
  restart_result = restarted_runner.run_once

  restarted_claimed = store.get!(id: previously_claimed.fetch("id"), project: "dora")
  completed_ready = store.get!(id: ready.fetch("id"), project: "dora")
  abort "fresh runner replayed a previously claimed handoff" unless restarted_claimed.slice("status", "lifecycle", "feedback_history", "delivery", "completion") == claimed_snapshot
  abort "fresh runner added a second claim or terminal lifecycle event" unless restarted_claimed.fetch("lifecycle").map { |event| event.fetch("action") } == %w[created claim]
  abort "fresh runner launched a previously claimed handoff" unless File.readlines(launch_record, chomp: true) == [ready.fetch("id")]
  abort "fresh runner did not run the READY handoff normally" unless restart_result.fetch("status") == "COMPLETED" && completed_ready.fetch("status") == "COMPLETED" && completed_ready.fetch("lifecycle").map { |event| event.fetch("action") } == %w[created claim feedback link_delivery complete]
  abort "fresh runner emitted terminal output for the previously claimed handoff" if restart_output.string.include?(previously_claimed.fetch("id"))
end

puts "Dora handoff runner test passed (fixed UUID launch, closed stdin/discarded child streams, sanitized failure classes, singleton, and V3 safety boundaries)."
