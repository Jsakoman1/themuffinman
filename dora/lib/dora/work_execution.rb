# frozen_string_literal: true

require "digest"
require "fileutils"
require "open3"
require "shellwords"
require "time"
require "timeout"
require "yaml"

require_relative "adapter"
require_relative "project_memory"
require_relative "task_change_set"

module Dora
  class WorkExecution
    class Error < StandardError; end
    SECRET_ASSIGNMENT = /\b(password|passwd|token|secret|authorization|api[_-]?key)\s*(=|:)\s*([^\s]+)/i.freeze

    def self.run(adapter_path:, schema_path:, arguments:)
      new(adapter_path: adapter_path, schema_path: schema_path, arguments: arguments).run
    end

    def initialize(adapter_path:, schema_path:, arguments:)
      @context = Adapter.load_context!(adapter_path, schema_path)
      @arguments = arguments.dup
    end

    def run
      plan_path = option("plan") || fail!("usage: work execution requires plan=<path-to-work-plan>")
      action = option("action") || "verify"
      task_id = option("task")
      absolute_plan = resolve_under!(@context.root, plan_path, "work plan")
      fail!("plan not found: #{plan_path}") unless File.file?(absolute_plan)
      plan = load_plan(absolute_plan)
      baseline = plan["baseline"].to_s
      fail!("baseline must be a valid Git commit") unless valid_baseline?(baseline)
      revision = git_revision

      if action == "start"
        fail!("only work plans support action=start") unless plan["kind"] == "work" && serial_task_execution?(plan)
        start_serial_task!(plan_path, absolute_plan, plan, task_id)
        return "Work task started: #{plan_path}##{task_id}"
      end
      fail!("unsupported action: #{action}") unless action == "verify"

      verified_plan = if plan["kind"] == "master"
                        verify_master!(plan_path, plan, revision)
                      elsif plan["kind"] == "work"
                        verify_work!(plan_path, plan, baseline, revision, task_id)
                      else
                        fail!("kind must be work or master")
                      end
      File.write(absolute_plan, YAML.dump(verified_plan).sub(/\A---\n/, ""))
      "Work verified: #{plan_path}"
    rescue Error
      File.write(absolute_plan, YAML.dump(plan).sub(/\A---\n/, "")) if defined?(plan) && plan.is_a?(Hash) && plan["kind"] == "work" && plan["evidence"]
      raise
    end

    private

    def option(name)
      pair = @arguments.find { |argument| argument.start_with?("#{name}=") }
      pair&.split("=", 2)&.last
    end

    def git_revision
      revision, status = Open3.capture2("git", "-C", @context.root, "rev-parse", "HEAD")
      fail!("cannot read Git revision") unless status.success?

      revision.strip
    end

    def valid_baseline?(baseline)
      baseline.match?(/\A[0-9a-f]{7,40}\z/) && Open3.capture2e("git", "-C", @context.root, "cat-file", "-e", "#{baseline}^{commit}")[1].success?
    end

    def changed_files(baseline)
      diff, diff_status = Open3.capture2e("git", "-C", @context.root, "diff", "--name-only", baseline)
      fail!(diff) unless diff_status.success?
      porcelain, porcelain_status = Open3.capture2e("git", "-C", @context.root, "status", "--porcelain=v1", "--untracked-files=all")
      fail!(porcelain) unless porcelain_status.success?

      files = diff.lines.map(&:strip).reject(&:empty?).map { |path| project_relative_path(path) }.compact
      porcelain.lines.each do |line|
        next unless line.start_with?("?? ")

        path = line[3..].strip
        path = Shellwords.shellsplit(path).first if path.start_with?("\"")
        relative = project_relative_path(path)
        files << relative if relative
      end
      files.uniq
    end

    def project_relative_path(path)
      git_root, status = Open3.capture2("git", "-C", @context.root, "rev-parse", "--show-toplevel")
      fail!("cannot read Git worktree root") unless status.success?

      project_root = File.realpath(@context.root)
      git_root = File.realpath(git_root.strip)
      absolute = File.expand_path(path, git_root)
      absolute = File.expand_path(path, project_root) unless absolute.start_with?("#{project_root}/")
      return nil unless absolute.start_with?("#{project_root}/")

      absolute.delete_prefix("#{project_root}/")
    end

    def load_plan(path)
      plan = YAML.load_file(path)
      fail!("plan must be a YAML object: #{path}") unless plan.is_a?(Hash)
      fail!("unsupported work-plan version: #{path}") unless plan["version"].to_i == 1
      plan
    end

    def strict_plan?(plan)
      plan["strict_verification"] == true
    end

    def serial_task_execution?(plan)
      plan["serial_task_execution"] == true
    end

    def task_fingerprints(paths)
      paths.to_h do |path|
        absolute = File.expand_path(path, @context.root)
        [path, File.file?(absolute) ? Digest::SHA256.file(absolute).hexdigest : "__missing__"]
      end
    end

    def execution_inventory!(plan)
      relative_path = plan["execution_inventory"].to_s
      fail!("serial plan requires execution_inventory") if relative_path.empty?
      absolute_path = resolve_under!(@context.root, relative_path, "execution inventory")
      fail!("execution inventory not found: #{relative_path}") unless File.file?(absolute_path)
      inventory = YAML.load_file(absolute_path)
      fail!("execution inventory must be a YAML object: #{relative_path}") unless inventory.is_a?(Hash)
      fail!("execution inventory items must be a list: #{relative_path}") unless inventory["items"].is_a?(Array)
      [relative_path, absolute_path, inventory]
    end

    def inventory_item!(inventory, plan_path, task)
      item_id = task["inventory_item"].to_s
      fail!("serial task #{task["id"]} requires inventory_item") if item_id.empty?
      item = inventory.fetch("items").find { |candidate| candidate.is_a?(Hash) && candidate["id"] == item_id }
      fail!("execution inventory item not found: #{item_id}") unless item
      fail!("execution inventory mapping mismatch for #{item_id}") unless item["plan"] == plan_path && item["task"] == task["id"]
      item
    end

    def start_serial_task!(plan_path, absolute_plan, plan, task_id)
      fail!("task=<id> is required to start a serial plan") if task_id.to_s.empty?
      tasks = Array(plan["tasks"])
      task = tasks.find { |candidate| candidate.is_a?(Hash) && candidate["id"] == task_id }
      fail!("task not found: #{task_id}") unless task
      fail!("task #{task_id} is already done") if task["status"] == "done"
      fail!("another task is already in progress") if tasks.any? { |candidate| candidate["status"] == "in_progress" && candidate["id"] != task_id }
      previous = tasks.take_while { |candidate| candidate != task }
      fail!("task #{task_id} cannot start before prior tasks are done") unless previous.all? { |candidate| candidate["status"] == "done" }

      inventory_path, absolute_inventory, inventory = execution_inventory!(plan)
      item = inventory_item!(inventory, plan_path, task)
      fail!("execution inventory item #{item["id"]} is already verified") if item["status"] == "verified"
      fail!("another execution inventory item is already in progress") if inventory.fetch("items").any? { |candidate| candidate["status"] == "in_progress" && candidate["id"] != item["id"] }
      prior_items = inventory.fetch("items").take_while { |candidate| candidate != item }
      fail!("execution inventory item #{item["id"]} cannot start before earlier items are verified") unless prior_items.all? { |candidate| candidate["status"] == "verified" }

      paths = Array(task["verification_required_paths"] || task["required_paths"]).map(&:to_s).reject(&:empty?)
      fail!("serial task #{task_id} requires required_paths") if paths.empty?
      started_at = Time.now.utc.iso8601
      task["status"] = "in_progress"
      task["started_at"] = started_at
      task["start_fingerprints"] = task_fingerprints(paths)
      task["start_workspace_paths"] = TaskChangeSet.workspace_paths!(project_root: @context.root, baseline: plan.fetch("baseline"))
      item["status"] = "in_progress"
      item["started_at"] = started_at
      plan["status"] = "active"
      File.write(absolute_plan, YAML.dump(plan).sub(/\A---\n/, ""))
      File.write(absolute_inventory, YAML.dump(inventory).sub(/\A---\n/, ""))
    end

    def strict_task_evidence!(task, files)
      required_paths = Array(task["verification_required_paths"] || task["required_paths"]).map(&:to_s).reject(&:empty?)
      fail!("strict task #{task["id"]} requires required_paths") if required_paths.empty?
      missing_paths = required_paths.reject { |item| files.include?(item) }
      fail!("strict task #{task["id"]} is missing required changed paths: #{missing_paths.join(", ")}") unless missing_paths.empty?
      {"requiredPaths" => required_paths, "visualEvidencePaths" => Array(task["visual_evidence_paths"]), "runtimeEvidencePaths" => Array(task["runtime_evidence_paths"])}
    end

    def verify_work!(path, plan, baseline, revision, selected_task_id)
      fail!("superseded plan cannot be verified: #{path}") if plan["superseded_by"]
      tasks = plan["tasks"]
      fail!("work plan must contain tasks: #{path}") unless tasks.is_a?(Array) && !tasks.empty?
      files = changed_files(baseline)
      plan["status"] = "active"
      plan["evidence"] = Array(plan["evidence"])
      tasks = tasks.select { |task| task.is_a?(Hash) && task["id"] == selected_task_id } if serial_task_execution?(plan)
      fail!("task=<id> is required to verify a serial plan") if serial_task_execution?(plan) && selected_task_id.to_s.empty?
      fail!("task not found: #{selected_task_id}") if tasks.empty?

      tasks.each do |task|
        id = task["id"].to_s
        command = task["validation"].to_s
        fail!("task id and validation are required") if id.empty? || command.empty?
        fail!("task #{id} validation recursively invokes work verification; final validation must use leaf audits only") if command.match?(/\bwork-verify\b/) || (command.match?(%r{scripts/verify-work\.rb}) && !command.match?(/ruby\s+-c\s+scripts\/verify-work\.rb/))
        paths = Array(task["paths"]).map(&:to_s)
        fail!("task #{id} has no changed path linked to the baseline diff") unless task["type"].to_s == "docs-only" || paths.any? { |item| files.include?(item) }
        strict_evidence = strict_plan?(plan) ? strict_task_evidence!(task, files) : {}
        if serial_task_execution?(plan)
          fail!("task #{id} must be started before verification") unless task["status"] == "in_progress" && task["start_fingerprints"].is_a?(Hash)
          unchanged = strict_evidence.fetch("requiredPaths").select { |item| task["start_fingerprints"][item] == task_fingerprints(strict_evidence.fetch("requiredPaths"))[item] }
          fail!("task #{id} has no implementation change after start: #{unchanged.join(", ")}") unless unchanged.empty?
        end
        started = Time.now
        timeout_seconds = command_timeout_seconds(task)
        stdout, stderr, status, timed_out = capture_leaf_command(command, timeout_seconds)
        evidence = {"task" => id, "command" => redact(command), "ranAt" => started.utc.iso8601, "revision" => revision, "exitCode" => status.exitstatus, "result" => status.success? && !timed_out ? "passed" : "failed", "timedOut" => timed_out, "timeoutSeconds" => timeout_seconds, "changedFiles" => files.select { |item| paths.include?(item) }, "output" => bounded_redacted_output(stdout, stderr), "executionEnvironment" => {"shell" => "/bin/zsh", "rubyVersion" => RUBY_VERSION, "environmentExported" => false}, "requiredPathDigests" => task_fingerprints(Array(task["verification_required_paths"] || task["required_paths"]))}
        evidence.merge!(strict_evidence)
        evidence["startedAt"] = task["started_at"] if serial_task_execution?(plan)
        evidence["changedAfterStart"] = strict_evidence.fetch("requiredPaths") if serial_task_execution?(plan)
        plan["evidence"].reject! { |item| item.is_a?(Hash) && item["task"] == id }
        plan["evidence"] << evidence
        task["status"] = status.success? && !timed_out ? "done" : "blocked"
        if serial_task_execution?(plan) && status.success? && !timed_out
          _inventory_path, absolute_inventory, inventory = execution_inventory!(plan)
          item = inventory_item!(inventory, path, task)
          item["status"] = "verified"
          item["verified_at"] = Time.now.utc.iso8601
          item["evidence"] = {"plan" => path, "task" => id, "revision" => revision}
          File.write(absolute_inventory, YAML.dump(inventory).sub(/\A---\n/, ""))
        end
        fail!("task #{id} validation failed") unless status.success? && !timed_out
      end
      plan["status"] = Array(plan["tasks"]).all? { |task| task["status"] == "done" } ? "verified" : "active"
      plan
    end

    def verify_master!(path, plan, revision)
      fail!("superseded master cannot be verified: #{path}") if plan["superseded_by"]
      inventory = nil
      absolute_inventory = nil
      if strict_plan?(plan)
        _inventory_path, absolute_inventory, inventory = execution_inventory!(plan)
        fail!("execution inventory belongs to a different master") unless inventory["master_plan"] == path
        unverified = inventory.fetch("items").select { |item| item["status"] != "verified" }
        fail!("strict master has unverified execution inventory items: #{unverified.map { |item| item["id"] }.join(", ")}") unless unverified.empty?
      end
      children = Array(plan["children"]).map(&:to_s).reject(&:empty?)
      fail!("master plan must list children: #{path}") if children.empty?
      children.each do |child|
        child_plan = load_plan(resolve_under!(@context.root, child, "child plan"))
        fail!("child is not verified: #{child}") unless child_plan["kind"] == "work" && child_plan["status"] == "verified"
      end
      reconcile_project_memory_for_closeout!(inventory, absolute_inventory) if inventory
      plan["status"] = "verified"
      plan["evidence"] = [{"verifiedAt" => Time.now.utc.iso8601, "revision" => revision, "children" => children}]
      plan
    end

    def reconcile_project_memory_for_closeout!(inventory, absolute_inventory)
      memory = ProjectMemory.load!(File.join(@context.docs_root, "project-memory.yaml"))
      ProjectMemory.validate_work_navigation!(memory: memory, inventories: [inventory])
      inventory["state"] = "verified"
      File.write(absolute_inventory, YAML.dump(inventory).sub(/\A---\n/, ""))
    rescue ArgumentError => error
      fail!("project memory closeout gate failed: #{error.message}")
    end

    def bounded_redacted_output(stdout, stderr)
      redact([stdout, stderr].join("\n")).lines.last(20).join[0, 4000]
    end

    def command_timeout_seconds(task)
      policy = task["evidence_policy"] || {}
      fail!("task evidence_policy must be a mapping") unless policy.is_a?(Hash)
      seconds = policy.fetch("timeout_seconds", 300)
      fail!("task evidence_policy timeout_seconds must be an integer from 1 to 300") unless seconds.is_a?(Integer) && seconds.between?(1, 300)

      seconds
    end

    def capture_leaf_command(command, timeout_seconds)
      stdin, stdout, stderr, waiter = Open3.popen3("/bin/zsh", "-lc", command, chdir: @context.root, pgroup: true)
      stdin.close
      output_reader = Thread.new { stdout.read }
      error_reader = Thread.new { stderr.read }
      timed_out = false
      begin
        status = Timeout.timeout(timeout_seconds) { waiter.value }
      rescue Timeout::Error
        timed_out = true
        status = terminate_leaf_process(waiter)
      end
      [output_reader.value.to_s, error_reader.value.to_s, status, timed_out]
    ensure
      stdout&.close unless stdout&.closed?
      stderr&.close unless stderr&.closed?
    end

    def terminate_leaf_process(waiter)
      Process.kill("TERM", -waiter.pid)
    rescue Errno::ESRCH
      nil
    ensure
      begin
        return Timeout.timeout(1) { waiter.value }
      rescue Timeout::Error
        Process.kill("KILL", -waiter.pid) rescue nil
        return waiter.value
      end
    end

    def redact(value)
      value.to_s.gsub(SECRET_ASSIGNMENT) { "#{Regexp.last_match(1)}#{Regexp.last_match(2)}[REDACTED]" }
    end

    def resolve_under!(root, path, label)
      fail!("#{label} must be a non-empty relative path") unless path.is_a?(String) && !path.empty? && !path.start_with?("/")
      resolved = File.expand_path(path, root)
      fail!("#{label} resolves outside project.root") unless resolved.start_with?("#{root}/")
      resolved
    end

    def fail!(message)
      raise Error, message
    end
  end
end
