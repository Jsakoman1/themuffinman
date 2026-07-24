#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "time"
require "yaml"
require "digest"

ROOT = File.expand_path("..", __dir__)

def option(name)
  pair = ARGV.find { |arg| arg.start_with?("#{name}=") }
  pair&.split("=", 2)&.last
end

def fail!(message)
  warn "Work verification failed: #{message}"
  exit 1
end

def git_revision(root)
  revision, status = Open3.capture2("git", "-C", root, "rev-parse", "HEAD")
  fail!("cannot read Git revision") unless status.success?

  revision.strip
end

def valid_baseline?(root, baseline)
  baseline.match?(/\A[0-9a-f]{7,40}\z/) &&
    Open3.capture2e("git", "-C", root, "cat-file", "-e", "#{baseline}^{commit}")[1].success?
end

def changed_files(root, baseline)
  diff, diff_status = Open3.capture2e("git", "-C", root, "diff", "--name-only", baseline)
  fail!(diff) unless diff_status.success?
  porcelain, porcelain_status = Open3.capture2e("git", "-C", root, "status", "--porcelain=v1", "--untracked-files=all")
  fail!(porcelain) unless porcelain_status.success?

  files = diff.lines.map(&:strip).reject(&:empty?)
  porcelain.lines.each { |line| files << line[3..].strip if line.start_with?("?? ") }
  files.uniq
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

def task_fingerprints(root, paths)
  paths.to_h do |path|
    absolute = File.expand_path(path, root)
    [path, File.file?(absolute) ? Digest::SHA256.file(absolute).hexdigest : "__missing__"]
  end
end

def execution_inventory!(root, plan)
  relative_path = plan["execution_inventory"].to_s
  fail!("serial plan requires execution_inventory") if relative_path.empty?
  absolute_path = File.expand_path(relative_path, root)
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

def start_serial_task!(root, plan_path, absolute_plan, plan, task_id)
  fail!("task=<id> is required to start a serial plan") if task_id.to_s.empty?
  tasks = Array(plan["tasks"])
  task = tasks.find { |candidate| candidate.is_a?(Hash) && candidate["id"] == task_id }
  fail!("task not found: #{task_id}") unless task
  fail!("task #{task_id} is already done") if task["status"] == "done"
  fail!("another task is already in progress") if tasks.any? { |candidate| candidate["status"] == "in_progress" && candidate["id"] != task_id }
  previous = tasks.take_while { |candidate| candidate != task }
  fail!("task #{task_id} cannot start before prior tasks are done") unless previous.all? { |candidate| candidate["status"] == "done" }

  inventory_path, absolute_inventory, inventory = execution_inventory!(root, plan)
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
  task["start_fingerprints"] = task_fingerprints(root, paths)
  item["status"] = "in_progress"
  item["started_at"] = started_at
  plan["status"] = "active"
  File.write(absolute_plan, YAML.dump(plan).sub(/\A---\n/, ""))
  File.write(absolute_inventory, YAML.dump(inventory).sub(/\A---\n/, ""))
  puts "Work task started: #{plan_path}##{task_id} (#{inventory_path})"
end

def strict_task_evidence!(root, task, files)
  required_paths = Array(task["verification_required_paths"] || task["required_paths"]).map(&:to_s).reject(&:empty?)
  fail!("strict task #{task["id"]} requires required_paths") if required_paths.empty?
  missing_paths = required_paths.reject { |item| files.include?(item) }
  fail!("strict task #{task["id"]} is missing required changed paths: #{missing_paths.join(", ")}") unless missing_paths.empty?

  visual_paths = Array(task["visual_evidence_paths"]).map(&:to_s).reject(&:empty?)
  if task["requires_visual_evidence"] == true
    fail!("strict visual task #{task["id"]} requires visual_evidence_paths") if visual_paths.empty?
    missing_visual = visual_paths.reject { |item| files.include?(item) && File.file?(File.expand_path(item, root)) }
    fail!("strict visual task #{task["id"]} is missing changed visual evidence: #{missing_visual.join(", ")}") unless missing_visual.empty?
  end

  runtime_paths = Array(task["runtime_evidence_paths"]).map(&:to_s).reject(&:empty?)
  if !task["manual_runtime_command"].to_s.empty?
    fail!("strict runtime task #{task["id"]} requires runtime_evidence_paths") if runtime_paths.empty?
    missing_runtime = runtime_paths.reject { |item| files.include?(item) && File.file?(File.expand_path(item, root)) }
    fail!("strict runtime task #{task["id"]} is missing changed runtime evidence: #{missing_runtime.join(", ")}") unless missing_runtime.empty?
  end

  {"requiredPaths" => required_paths, "visualEvidencePaths" => visual_paths, "runtimeEvidencePaths" => runtime_paths}
end

def validate_work_evidence!(plan, path)
  fail!("superseded plan cannot be treated as verified: #{path}") if plan["superseded_by"]
  fail!("child is not verified: #{path}") unless plan["kind"] == "work" && plan["status"] == "verified"
  tasks = Array(plan["tasks"])
  evidence = Array(plan["evidence"])
  fail!("verified child has no tasks: #{path}") if tasks.empty?
  tasks.each do |task|
    entry = evidence.reverse.find { |item| item.is_a?(Hash) && item["task"] == task["id"] }
    fail!("child task lacks passing evidence: #{path}##{task["id"]}") unless task["status"] == "done" && entry&.dig("result") == "passed"
    if strict_plan?(plan)
      required_paths = Array(task["verification_required_paths"] || task["required_paths"]).map(&:to_s).reject(&:empty?)
      fail!("strict child task lacks required path evidence: #{path}##{task["id"]}") unless required_paths == Array(entry["requiredPaths"])
      if task["requires_visual_evidence"] == true
        fail!("strict child task lacks visual evidence: #{path}##{task["id"]}") if Array(entry["visualEvidencePaths"]).empty?
      end
      if !task["manual_runtime_command"].to_s.empty?
        fail!("strict child task lacks runtime evidence: #{path}##{task["id"]}") if Array(entry["runtimeEvidencePaths"]).empty?
      end
    end
  end
end

def verify_master!(root, path, plan, revision)
  fail!("superseded master cannot be verified: #{path}") if plan["superseded_by"]
  if strict_plan?(plan)
    _inventory_path, _absolute_inventory, inventory = execution_inventory!(root, plan)
    fail!("execution inventory belongs to a different master") unless inventory["master_plan"] == path
    unverified = inventory.fetch("items").select { |item| item["status"] != "verified" }
    fail!("strict master has unverified execution inventory items: #{unverified.map { |item| item["id"] }.join(", ")}") unless unverified.empty?
  end
  children = Array(plan["children"]).map(&:to_s).reject(&:empty?)
  fail!("master plan must list children: #{path}") if children.empty?
  children.each do |child|
    child_path = File.expand_path(child, root)
    fail!("child plan not found: #{child}") unless File.file?(child_path)
    child_plan = load_plan(child_path)
    fail!("strict master child is not strict: #{child}") if strict_plan?(plan) && !strict_plan?(child_plan)
    fail!("strict master child does not use serial task execution: #{child}") if strict_plan?(plan) && !serial_task_execution?(child_plan)
    validate_work_evidence!(child_plan, child)
  end

  plan["status"] = "verified"
  plan["evidence"] = [{"verifiedAt" => Time.now.utc.iso8601, "revision" => revision, "children" => children}]
  plan
end

def verify_work!(root, path, plan, baseline, revision, selected_task_id)
  fail!("superseded plan cannot be verified: #{path}") if plan["superseded_by"]
  tasks = plan["tasks"]
  fail!("work plan must contain tasks: #{path}") unless tasks.is_a?(Array) && !tasks.empty?
  files = changed_files(root, baseline)
  plan["status"] = "active"
  plan["evidence"] = Array(plan["evidence"])

  if serial_task_execution?(plan)
    fail!("task=<id> is required to verify a serial plan") if selected_task_id.to_s.empty?
    tasks = tasks.select { |task| task.is_a?(Hash) && task["id"] == selected_task_id }
    fail!("task not found: #{selected_task_id}") if tasks.empty?
  end

  tasks.each do |task|
    fail!("each task must be an object") unless task.is_a?(Hash)
    id = task["id"].to_s
    command = task["validation"].to_s
    fail!("task id and validation are required") if id.empty? || command.empty?
    if command.match?(/\bwork-verify\b/) || (command.match?(/scripts\/verify-work\.rb/) && !command.match?(/ruby\s+-c\s+scripts\/verify-work\.rb/))
      fail!("task #{id} validation recursively invokes work verification; final validation must use leaf audits only")
    end
    paths = Array(task["paths"]).map(&:to_s)
    unless task["type"].to_s == "docs-only" || paths.any? { |item| files.include?(item) }
      fail!("task #{id} has no changed path linked to the baseline diff")
    end

    strict_evidence = strict_plan?(plan) ? strict_task_evidence!(root, task, files) : {}
    if serial_task_execution?(plan)
      fail!("task #{id} must be started before verification") unless task["status"] == "in_progress" && task["start_fingerprints"].is_a?(Hash)
      current_fingerprints = task_fingerprints(root, strict_evidence.fetch("requiredPaths"))
      unchanged = strict_evidence.fetch("requiredPaths").select { |item| task["start_fingerprints"][item] == current_fingerprints[item] }
      fail!("task #{id} has no implementation change after start: #{unchanged.join(", ")}") unless unchanged.empty?
    end
    started = Time.now
    stdout, stderr, status = Open3.capture3("/bin/zsh", "-lc", command, chdir: root)
    evidence = {
      "task" => id,
      "command" => command,
      "ranAt" => started.utc.iso8601,
      "revision" => revision,
      "exitCode" => status.exitstatus,
      "result" => status.success? ? "passed" : "failed",
      "changedFiles" => files.select { |item| paths.include?(item) },
      "output" => [stdout, stderr].join("\n").lines.last(20).join[0, 4000]
    }
    if serial_task_execution?(plan)
      evidence["startedAt"] = task["started_at"]
      evidence["changedAfterStart"] = strict_evidence.fetch("requiredPaths")
    end
    plan["evidence"].reject! { |item| item.is_a?(Hash) && item["task"] == id }
    plan["evidence"] << evidence
    evidence.merge!(strict_evidence)
    task["status"] = status.success? ? "done" : "blocked"
    if serial_task_execution?(plan) && status.success?
      _inventory_path, absolute_inventory, inventory = execution_inventory!(root, plan)
      item = inventory_item!(inventory, path, task)
      item["status"] = "verified"
      item["verified_at"] = Time.now.utc.iso8601
      item["evidence"] = {"plan" => path, "task" => id, "revision" => revision}
      File.write(absolute_inventory, YAML.dump(inventory).sub(/\A---\n/, ""))
    end
    fail!("task #{id} validation failed") unless status.success?
  end

  plan["status"] = Array(plan["tasks"]).all? { |task| task["status"] == "done" } ? "verified" : "active"
  plan
end

plan_path = option("plan") || fail!("usage: ruby scripts/verify-work.rb plan=docs/work/change.yaml")
action = option("action") || "verify"
task_id = option("task")
absolute_plan = File.expand_path(plan_path, ROOT)
fail!("plan not found: #{plan_path}") unless File.file?(absolute_plan)
plan = load_plan(absolute_plan)
fail!("nested work verification is not allowed") if ENV["WORK_VERIFY_ACTIVE"] == "1"
baseline = plan["baseline"].to_s
fail!("baseline must be a valid Git commit") unless valid_baseline?(ROOT, baseline)
revision = git_revision(ROOT)

if action == "start"
  fail!("only work plans support action=start") unless plan["kind"] == "work" && serial_task_execution?(plan)
  start_serial_task!(ROOT, plan_path, absolute_plan, plan, task_id)
  exit 0
end
fail!("unsupported action: #{action}") unless action == "verify"

plan = if plan["kind"] == "master"
  verify_master!(ROOT, plan_path, plan, revision)
elsif plan["kind"] == "work"
  ENV["WORK_VERIFY_ACTIVE"] = "1"
  verify_work!(ROOT, plan_path, plan, baseline, revision, task_id)
else
  fail!("kind must be work or master")
end

FileUtils.mkdir_p(File.dirname(absolute_plan))
File.write(absolute_plan, YAML.dump(plan).sub(/\A---\n/, ""))
puts "Work verified: #{plan_path}"
