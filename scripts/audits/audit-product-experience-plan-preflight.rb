#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("../..", __dir__)
MASTER_RELATIVE = "docs/work/product-experience-account-discovery-master.yaml"
master_path = File.join(ROOT, MASTER_RELATIVE)
failures = []
abort "Product experience master plan missing: #{master_path}" unless File.file?(master_path)

master = YAML.load_file(master_path)
failures << "master kind must be master" unless master["kind"] == "master"
failures << "master status must be planned or verified" unless %w[planned verified].include?(master["status"])
children = Array(master["children"])
failures << "master has no children" if children.empty?
child_ids = []

children.each do |relative_path|
  path = File.join(ROOT, relative_path)
  unless File.file?(path)
    failures << "child missing: #{relative_path}"
    next
  end
  child = YAML.load_file(path)
  child_ids << child["id"]
  failures << "child kind must be work: #{relative_path}" unless child["kind"] == "work"
  failures << "child master_plan mismatch: #{relative_path}" unless child["master_plan"] == MASTER_RELATIVE
  baseline = child["baseline"].to_s
  valid_baseline = baseline.match?(/\A[0-9a-f]{7,40}\z/) && system("git", "-C", ROOT, "cat-file", "-e", "#{baseline}^{commit}", out: File::NULL, err: File::NULL)
  failures << "child baseline invalid: #{relative_path}" unless valid_baseline
  failures << "child objective missing: #{relative_path}" if child["objective"].to_s.empty?
  failures << "child acceptance criteria missing: #{relative_path}" if Array(child["acceptance"]).empty?
  tasks = Array(child["tasks"])
  failures << "child tasks missing: #{relative_path}" if tasks.empty?
  task_ids = []
  tasks.each do |task|
    task_id = task["id"].to_s
    validation = task["validation"].to_s
    paths = Array(task["paths"]).map(&:to_s)
    task_ids << task_id
    failures << "task id missing: #{relative_path}" if task_id.empty?
    failures << "duplicate task id: #{relative_path}##{task_id}" if task_ids.count(task_id) > 1
    failures << "task validation missing: #{relative_path}##{task_id}" if validation.empty?
    failures << "task paths missing: #{relative_path}##{task_id}" if paths.empty?
    failures << "recursive validation: #{relative_path}##{task_id}" if validation.match?(/\bwork-verify\b|scripts\/verify-work\.rb/)
    if validation.include?("web-runtime-smoke") && task["manual_runtime_command"].to_s.empty?
      failures << "browser command needs manual_runtime_command: #{relative_path}##{task_id}"
    end
  end
end

failures << "duplicate child ids in master" unless child_ids.uniq.length == child_ids.length
Array(master["dependencies"]).each do |dependency|
  dependency.to_s.split(" -> ").each do |path|
    failures << "dependency references unlisted child: #{path}" unless children.include?(path)
  end
end

analysis_paths = children.map { |path| File.join(ROOT, "docs/work", "product-experience-#{File.basename(path, ".yaml").sub("product-experience-", "")}-analysis.md") }
analysis_paths << File.join(ROOT, "docs/work/product-experience-runtime-analysis.md")
analysis_paths.each { |path| failures << "analysis artifact missing: #{path}" unless File.file?(path) }

abort "Product experience plan preflight failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Product experience plan preflight passed (#{children.length} child plans, #{child_ids.length} unique child ids, #{analysis_paths.length} analysis artifacts)."
