#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "time"
require "yaml"

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

def validate_work_evidence!(plan, path)
  fail!("child is not verified: #{path}") unless plan["kind"] == "work" && plan["status"] == "verified"
  tasks = Array(plan["tasks"])
  evidence = Array(plan["evidence"])
  fail!("verified child has no tasks: #{path}") if tasks.empty?
  tasks.each do |task|
    entry = evidence.reverse.find { |item| item.is_a?(Hash) && item["task"] == task["id"] }
    fail!("child task lacks passing evidence: #{path}##{task["id"]}") unless task["status"] == "done" && entry&.dig("result") == "passed"
  end
end

def verify_master!(root, path, plan, revision)
  children = Array(plan["children"]).map(&:to_s).reject(&:empty?)
  fail!("master plan must list children: #{path}") if children.empty?
  children.each do |child|
    child_path = File.expand_path(child, root)
    fail!("child plan not found: #{child}") unless File.file?(child_path)
    validate_work_evidence!(load_plan(child_path), child)
  end

  plan["status"] = "verified"
  plan["evidence"] = [{"verifiedAt" => Time.now.utc.iso8601, "revision" => revision, "children" => children}]
  plan
end

def verify_work!(root, path, plan, baseline, revision)
  tasks = plan["tasks"]
  fail!("work plan must contain tasks: #{path}") unless tasks.is_a?(Array) && !tasks.empty?
  files = changed_files(root, baseline)
  plan["status"] = "active"
  plan["evidence"] = Array(plan["evidence"])

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
    plan["evidence"].reject! { |item| item.is_a?(Hash) && item["task"] == id }
    plan["evidence"] << evidence
    task["status"] = status.success? ? "done" : "blocked"
    fail!("task #{id} validation failed") unless status.success?
  end

  plan["status"] = "verified"
  plan
end

plan_path = option("plan") || fail!("usage: ruby scripts/verify-work.rb plan=docs/work/change.yaml")
absolute_plan = File.expand_path(plan_path, ROOT)
fail!("plan not found: #{plan_path}") unless File.file?(absolute_plan)
plan = load_plan(absolute_plan)
fail!("nested work verification is not allowed") if ENV["WORK_VERIFY_ACTIVE"] == "1"
baseline = plan["baseline"].to_s
fail!("baseline must be a valid Git commit") unless valid_baseline?(ROOT, baseline)
revision = git_revision(ROOT)

plan = if plan["kind"] == "master"
  verify_master!(ROOT, plan_path, plan, revision)
elsif plan["kind"] == "work"
  ENV["WORK_VERIFY_ACTIVE"] = "1"
  verify_work!(ROOT, plan_path, plan, baseline, revision)
else
  fail!("kind must be work or master")
end

FileUtils.mkdir_p(File.dirname(absolute_plan))
File.write(absolute_plan, YAML.dump(plan).sub(/\A---\n/, ""))
puts "Work verified: #{plan_path}"
