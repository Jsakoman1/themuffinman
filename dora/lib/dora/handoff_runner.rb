# frozen_string_literal: true

require "fileutils"
require "json"
require "time"

require_relative "handoff"
require_relative "handoff_cli"

module Dora
  # Owner-local V3 execution loop. It deliberately accepts only project IDs and invokes
  # one fixed local launcher with an argument vector; remote bridge requests cannot reach it.
  class HandoffRunner
    DEFAULT_LAUNCHER = File.expand_path("~/.local/bin/codex-both")
    MAX_INTERVAL_SECONDS = 60
    DEFAULT_PROGRESS_POLL_INTERVAL = 5
    LAUNCHER_PREFLIGHT_EXIT = 70
    CODEX_EXECUTION_FAILURE_EXIT = 71
    SAFE_PROGRESS_LABELS = {
      "START" => "Start",
      "ANALYSIS" => "Analysis",
      "IMPLEMENTATION" => "Implementation",
      "VERIFICATION" => "Verification",
      "BLOCKED" => "Blocked",
      "COMPLETED" => "Completed"
    }.freeze
    FAILURE_HINTS = {
      "keychain_unavailable" => "Run dora-bridge-keychain-setup in the owner login session, then create a new follow-up handoff.",
      "runtime_preflight_failed" => "Review local Dora Bridge runtime prerequisites in the owner terminal, then create a new follow-up handoff.",
      "launcher_dependency_unavailable" => "Install or repair the owner-local Codex launcher prerequisites, then create a new follow-up handoff.",
      "launcher_start_failed" => "Repair the fixed owner-local launcher, then create a new follow-up handoff.",
      "launcher_preflight_failed" => "Repair the fixed owner-local launcher prerequisites, then create a new follow-up handoff.",
      "codex_execution_failed" => "Review the claimed task outcome and create a new follow-up handoff if work remains.",
      "launcher_exited_nonzero" => "Review the owner-local Codex launcher, then create a new follow-up handoff after correction.",
      "completion_missing" => "Review the handoff outcome and create a new follow-up handoff if work remains."
    }.freeze

    class Error < StandardError; end

    def initialize(registry_path:, state_root:, projects:, launcher: DEFAULT_LAUNCHER, now: -> { Time.now.utc }, sleeper: ->(seconds) { sleep(seconds) }, output: $stdout, progress_reader: nil, master_plan_progress_reader: nil)
      @registry_path = File.expand_path(registry_path)
      @state_root = File.expand_path(state_root)
      @projects = validate_projects!(projects)
      @launcher = launcher
      @now = now
      @sleeper = sleeper
      @output = output
      @registry = DoraBridge::ProjectRegistry.load!(@registry_path)
      @projects.each { |project| @registry.handoff_authorized!(project) }
      @store = Handoff.new(state_root: @state_root)
      @progress_reader = progress_reader || ->(project, handoff_id) { @store.lifecycle_readback!(project: project, id: handoff_id).fetch("progress") }
      @master_plan_progress_reader = master_plan_progress_reader || ->(project, _handoff_id, delivery) { @registry.read_model!(project).master_plan_progress(delivery.fetch("master_plan")) }
      @observed_progress = {}
      @observed_master_plan_progress = {}
    rescue ArgumentError => error
      raise Error, error.message
    end

    def run_once
      with_singleton do
        emit("Dora handoff runner ready; checking for eligible handoffs.")
        run_once_locked(progress_poll_interval: DEFAULT_PROGRESS_POLL_INTERVAL)
      end
    end

    def watch(interval_seconds:, max_cycles: nil)
      interval = validate_interval!(interval_seconds)
      with_singleton do
        emit("Dora handoff runner ready; waiting for eligible handoffs.")
        cycles = 0
        until stop_requested? || (max_cycles && cycles >= max_cycles)
          run_once_locked(progress_poll_interval: interval)
          cycles += 1
          @sleeper.call(interval) unless stop_requested? || (max_cycles && cycles >= max_cycles)
        end
        clear_stop!
        write_health!("STOPPED", detail: "Owner requested runner stop")
        emit("Dora handoff runner stopped by owner request.")
      end
    rescue Interrupt
      write_health!("STOPPED", detail: "Owner interrupted runner")
      emit("Dora handoff runner interrupted by owner.")
    end

    def status
      state = read_state
      return {"status" => "IDLE"} unless state

      if state.fetch("status") == "RUNNING" && !process_alive?(state["pid"])
        return state.merge("status" => "RECOVERY_REQUIRED", "detail" => "A claimed runner process is no longer alive; owner review is required before any retry.")
      end
      state
    end

    def request_stop!
      write_private!(stop_path, "stop\n")
      {"status" => "STOP_REQUESTED"}
    end

    private

    def run_once_locked(progress_poll_interval:)
      current = status
      if current.fetch("status") == "RECOVERY_REQUIRED"
        write_health!("RECOVERY_REQUIRED", detail: current.fetch("detail"), handoff: current["handoff"])
        return current
      end

      candidate = next_candidate
      unless candidate
        write_health!("IDLE", detail: "No eligible READY handoff")
        return {"status" => "IDLE"}
      end

      project = candidate.fetch("project")
      handoff_id = candidate.fetch("id")
      claimed = HandoffCLI.run!(command: "claim", registry_path: @registry_path, state_root: @state_root, project: project, id: handoff_id)
      feedback!(project, handoff_id, phase: "START", milestone: "Local runner claimed authorized handoff", progress: "Invoking the fixed owner-local Codex workflow", verification_state: "NOT_STARTED")
      emit("Eligible handoff detected and claimed.")
      observe_progress!(project, handoff_id)
      execute_claimed!(claimed, progress_poll_interval: progress_poll_interval)
    rescue ArgumentError => error
      write_health_safely!("ERROR", detail: "Runner stopped after a Dora state error")
      raise Error, error.message
    end

    def next_candidate
      @projects.map { |project| HandoffCLI.run!(command: "next", registry_path: @registry_path, state_root: @state_root, project: project) }.compact.min_by { |handoff| [handoff.fetch("created_at"), handoff.fetch("id")] }
    end

    def execute_claimed!(handoff, progress_poll_interval:)
      project = handoff.fetch("project")
      handoff_id = handoff.fetch("id")
      raise Error, "approved local codex-both launcher is unavailable" unless File.file?(@launcher) && File.executable?(@launcher)
      raise Error, "claimed handoff identifier is invalid" unless Handoff::HANDOFF_ID.match?(handoff_id)

      pid = Process.spawn(@launcher, "--dora-preclaimed-handoff", handoff_id, chdir: File.dirname(@launcher), in: File::NULL, out: File::NULL, err: File::NULL)
      write_health!("RUNNING", detail: "Fixed local Codex workflow is active", handoff: handoff.slice("project", "id"), pid: pid)
      process_status = wait_for_claimed_exit!(pid, project, handoff_id, progress_poll_interval)
      terminal_result!(project, handoff_id, process_status.success?, process_status.exitstatus)
    rescue SystemCallError
      terminal_runner_failure!(project, handoff_id, code: "launcher_start_failed", exit_code: nil)
    end

    def terminal_result!(project, handoff_id, launcher_succeeded, exit_code)
      readback = @store.lifecycle_readback!(id: handoff_id, project: project)
      if readback.fetch("status") == "CLAIMED"
        code = launcher_succeeded ? "completion_missing" : failure_code_for_exit(exit_code)
        return terminal_runner_failure!(project, handoff_id, code: code, exit_code: exit_code)
      end
      status = readback.fetch("status")
      write_health!("IDLE", detail: "Claimed handoff reached #{status}", handoff: {"project" => project, "id" => handoff_id, "status" => status})
      observe_progress!(project, handoff_id)
      emit_terminal_outcome!(status)
      {"status" => status, "handoff" => {"project" => project, "id" => handoff_id}}
    end

    def terminal_runner_failure!(project, handoff_id, code:, exit_code:)
      failure = {"code" => code, "exit_code" => exit_code, "recovery_hint" => FAILURE_HINTS.fetch(code)}
      feedback!(project, handoff_id, phase: "VERIFYING", milestone: "Local runner recorded a sanitized Codex launch failure", progress: "Runner stopped without retrying the claimed handoff", verification_state: "VERIFICATION_FAILED", residual_risks: ["Owner review is required before follow-up work"])
      current = HandoffCLI.run!(command: "block_runner_failure", registry_path: @registry_path, state_root: @state_root, project: project, id: handoff_id, runner_failure: failure)
      write_health!("IDLE", detail: "Claimed handoff reached BLOCKED", handoff: current.slice("project", "id", "status"))
      observe_progress!(project, handoff_id)
      emit("Handoff blocked after runner failure; owner review is required.")
      {"status" => current.fetch("status"), "handoff" => current.slice("project", "id")}
    end

    def wait_for_claimed_exit!(pid, project, handoff_id, interval)
      loop do
        observe_progress!(project, handoff_id)
        _child, process_status = Process.wait2(pid, Process::WNOHANG)
        return process_status if process_status

        @sleeper.call(interval)
      end
    end

    def observe_progress!(project, handoff_id)
      phase, label = safe_progress_display(project, handoff_id)
      state = [phase, label]
      unless @observed_progress[handoff_id] == state
        @observed_progress[handoff_id] = state
        emit(phase == "UNAVAILABLE" ? "Handoff progress is unavailable." : "Handoff phase: #{label}.")
      end
      observe_master_plan_progress!(project, handoff_id)
    end

    def observe_master_plan_progress!(project, handoff_id)
      progress = safe_master_plan_progress(project, handoff_id)
      return unless progress

      token = progress.fetch("material_change_token")
      return if @observed_master_plan_progress[handoff_id] == token

      @observed_master_plan_progress[handoff_id] = token
      master = progress.fetch("master_plan")
      lines = ["Master Plan: #{master.fetch("title")} (#{master.fetch("id")})"]
      progress.fetch("items").each do |item|
        task = item.fetch("task")
        marker, label = {"verified" => ["x", "verified"], "in_progress" => [">", "current"], "pending" => [" ", "pending"], "blocked" => ["!", "blocked"]}.fetch(item.fetch("status"))
        lines << "  [#{marker}] #{task.fetch("title")} (#{task.fetch("id")}) — #{label}"
      end
      emit(lines.join("\n"))
    end

    def safe_master_plan_progress(project, handoff_id)
      readback = @store.status_readback!(project: project, id: handoff_id)
      delivery = readback["delivery"]
      return nil unless delivery.is_a?(Hash) && delivery["master_plan"].is_a?(String)

      progress = @master_plan_progress_reader.call(project, handoff_id, delivery)
      validate_master_plan_progress!(progress)
    rescue StandardError
      nil
    end

    def validate_master_plan_progress!(progress)
      return nil unless progress.is_a?(Hash) && progress.keys.sort == %w[items master_plan material_change_token]
      master = progress["master_plan"]
      items = progress["items"]
      return nil unless safe_terminal_entry?(master, require_reference: true) && items.is_a?(Array) && !items.empty? && safe_token?(progress["material_change_token"])

      return nil unless items.all? do |item|
        item.is_a?(Hash) && item.keys.sort == %w[id status task] && safe_identifier?(item["id"]) && %w[verified in_progress pending blocked].include?(item["status"]) && safe_terminal_entry?(item["task"], require_reference: true)
      end

      progress
    end

    def safe_terminal_entry?(entry, require_reference:)
      expected_keys = require_reference ? %w[id reference title] : %w[id title]
      entry.is_a?(Hash) && entry.keys.sort == expected_keys && safe_identifier?(entry["id"]) && safe_terminal_text?(entry["title"]) && (!require_reference || safe_relative_path?(entry["reference"]))
    end

    def safe_identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end

    def safe_terminal_text?(value)
      value.is_a?(String) && value.length.between?(1, 200) && value.match?(/\A[^\r\n\x00-\x1f\x7f]+\z/)
    end

    def safe_token?(value)
      value.is_a?(String) && value.match?(/\A[0-9a-f]{64}\z/)
    end

    def safe_relative_path?(value)
      value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?("..")
    end

    def safe_progress_display(project, handoff_id)
      progress = @progress_reader.call(project, handoff_id)
      phase = progress.fetch("phase")
      label = progress.fetch("label")
      expected_label = SAFE_PROGRESS_LABELS[phase]
      return ["UNAVAILABLE", "UNAVAILABLE"] unless expected_label && label == expected_label && progress["material_change_token"].is_a?(String)

      [phase, phase == "COMPLETED" ? "Complete" : expected_label]
    rescue StandardError
      ["UNAVAILABLE", "UNAVAILABLE"]
    end

    def emit_terminal_outcome!(status)
      message = {
        "COMPLETED" => "Handoff completed.",
        "BLOCKED" => "Handoff blocked; owner review is required.",
        "CANCELLED" => "Handoff was cancelled.",
        "SUPERSEDED" => "Handoff was superseded."
      }[status]
      emit(message || "Handoff reached a terminal outcome.")
    end

    def feedback!(project, handoff_id, phase:, milestone:, progress:, verification_state:, residual_risks: [])
      HandoffCLI.run!(command: "feedback", registry_path: @registry_path, state_root: @state_root, project: project, id: handoff_id, feedback: {"phase" => phase, "milestone" => milestone, "progress" => progress, "finding" => milestone, "verification_state" => verification_state, "deviations" => [], "residual_risks" => residual_risks})
    end

    def failure_code_for_exit(exit_code)
      return "launcher_preflight_failed" if exit_code == LAUNCHER_PREFLIGHT_EXIT
      return "codex_execution_failed" if exit_code == CODEX_EXECUTION_FAILURE_EXIT

      "launcher_exited_nonzero"
    end

    def emit(message)
      @output.puts(message)
      @output.flush if @output.respond_to?(:flush)
    rescue IOError, SystemCallError
      nil
    end

    def with_singleton
      File.open(lock_path, File::RDWR | File::CREAT, 0o600) do |file|
        File.chmod(0o600, lock_path)
        raise Error, "another local Dora handoff runner is active" unless file.flock(File::LOCK_EX | File::LOCK_NB)

        yield
      ensure
        file.flock(File::LOCK_UN) if file
      end
    end

    def validate_projects!(projects)
      raise Error, "runner requires one or more explicit projects" unless projects.is_a?(Array) && !projects.empty? && projects.all? { |project| project.is_a?(String) && project.match?(Handoff::PROJECT_ID) }
      raise Error, "runner project allowlist has duplicates" unless projects.uniq.length == projects.length

      projects.sort.freeze
    end

    def validate_interval!(value)
      seconds = Integer(value)
      raise Error, "runner interval must be between 1 and #{MAX_INTERVAL_SECONDS} seconds" unless seconds.between?(1, MAX_INTERVAL_SECONDS)

      seconds
    rescue ArgumentError, TypeError
      raise Error, "runner interval must be between 1 and #{MAX_INTERVAL_SECONDS} seconds"
    end

    def status_path
      File.join(@state_root, "handoff-runner-state.json")
    end

    def stop_path
      File.join(@state_root, "handoff-runner-stop")
    end

    def lock_path
      File.join(@state_root, ".handoff-runner.lock")
    end

    def read_state
      return nil unless File.exist?(status_path)
      assert_private_file!(status_path)
      state = JSON.parse(File.read(status_path))
      raise Error, "runner state is invalid" unless state.is_a?(Hash) && state["status"].is_a?(String)

      state
    rescue JSON::ParserError
      raise Error, "runner state is invalid"
    end

    def write_health!(status, detail:, handoff: nil, pid: nil)
      record = {"status" => status, "updated_at" => @now.call.utc.iso8601, "detail" => detail}
      record["handoff"] = handoff if handoff
      record["pid"] = pid if pid
      write_private!(status_path, JSON.generate(record) + "\n")
      record
    end

    def write_health_safely!(status, detail:)
      write_health!(status, detail: detail)
    rescue Error, SystemCallError
      nil
    end

    def write_private!(path, content)
      assert_private_file!(path) if File.exist?(path)
      File.open(path, File::WRONLY | File::CREAT | File::TRUNC, 0o600) { |file| file.write(content); file.flush; file.fsync }
      File.chmod(0o600, path)
    end

    def assert_private_file!(path)
      stat = File.lstat(path)
      raise Error, "runner private state is invalid" if stat.symlink? || !stat.file? || stat.uid != Process.uid || stat.mode & 0o077 != 0
    end

    def stop_requested?
      return false unless File.exist?(stop_path)
      assert_private_file!(stop_path)
      true
    end

    def clear_stop!
      File.delete(stop_path) if File.exist?(stop_path)
    end

    def process_alive?(pid)
      return false unless pid.is_a?(Integer) && pid.positive?
      Process.kill(0, pid)
      true
    rescue Errno::ESRCH
      false
    rescue Errno::EPERM
      true
    end
  end
end
