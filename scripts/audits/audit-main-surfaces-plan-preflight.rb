#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

root = File.expand_path("../..", __dir__)
master_path = File.join(root, "docs/work/main-surfaces-modernization-master.yaml")
failures = []
abort "Main surfaces master plan missing: #{master_path}" unless File.file?(master_path)

master = YAML.load_file(master_path)
failures << "master kind must be master" unless master["kind"] == "master"
failures << "master status must be planned or verified" unless %w[planned verified].include?(master["status"])
children = Array(master["children"])
failures << "master has no children" if children.empty?
ids = []

children.each do |relative_path|
  path = File.join(root, relative_path)
  unless File.file?(path)
    failures << "child missing: #{relative_path}"
    next
  end
  child = YAML.load_file(path)
  ids << child["id"]
  failures << "child kind must be work: #{relative_path}" unless child["kind"] == "work"
  failures << "child master_plan mismatch: #{relative_path}" unless child["master_plan"] == "docs/work/main-surfaces-modernization-master.yaml"
  baseline = child["baseline"].to_s
  valid_baseline = baseline.match?(/\A[0-9a-f]{7,40}\z/) && system("git", "-C", root, "cat-file", "-e", "#{baseline}^{commit}", out: File::NULL, err: File::NULL)
  failures << "child baseline invalid: #{relative_path}" unless valid_baseline
  failures << "child objective missing: #{relative_path}" if child["objective"].to_s.empty?
  failures << "child acceptance criteria missing: #{relative_path}" if Array(child["acceptance"]).empty?
  tasks = Array(child["tasks"])
  failures << "child tasks missing: #{relative_path}" if tasks.empty?
  task_ids = []
  tasks.each do |task|
    task_id = task["id"].to_s
    task_ids << task_id
    validation = task["validation"].to_s
    paths = Array(task["paths"]).map(&:to_s)
    failures << "task id missing: #{relative_path}" if task_id.empty?
    failures << "duplicate task id: #{relative_path}##{task_id}" if task_ids.count(task_id) > 1
    failures << "task validation missing: #{relative_path}##{task_id}" if validation.empty?
    failures << "task paths missing: #{relative_path}##{task_id}" if paths.empty?
    failures << "recursive validation: #{relative_path}##{task_id}" if validation.match?(/\bwork-verify\b|scripts\/verify-work\.rb/)
    paths.each do |file_path|
      failures << "directory path is not verifier-safe: #{relative_path}##{task_id}: #{file_path}" if File.directory?(File.join(root, file_path))
    end
  end
end

failures << "duplicate child ids in master" unless ids.uniq.length == ids.length
Array(master["dependencies"]).each do |dependency|
  dependency.to_s.split(" -> ").each do |path|
    failures << "dependency references unlisted child: #{path}" unless children.include?(path)
  end
end

abort "Main surfaces plan preflight failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Main surfaces plan preflight passed (#{children.length} child plans, #{ids.length} unique child ids)."
