#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/policy_cleanup"

Dir.mktmpdir("dora-policy-cleanup") do |root|
  FileUtils.mkdir_p(File.join(root, "output"))
  keep = File.join(root, "output/keep.txt")
  candidate = File.join(root, "output/remove.tmp")
  File.write(keep, "keep")
  File.write(candidate, "remove")
  policy = File.join(root, "retention.yaml")
  File.write(policy, YAML.dump({"kind" => "dora_retention_policy", "version" => 1, "generated_roots" => ["output"], "retained_paths" => ["output/keep.txt"], "cleanup_candidate_globs" => ["output/*.tmp"]}))

  targets = Dora::PolicyCleanup.dry_run!(policy, project_root: root)
  abort "dry-run targets differ" unless targets == ["output/remove.tmp"]
  abort "dry-run deleted a target" unless File.file?(candidate)
  begin
    Dora::PolicyCleanup.apply!(policy, project_root: root, approved_paths: [])
    abort "cleanup accepted an unconfirmed target set"
  rescue ArgumentError
    nil
  end
  Dora::PolicyCleanup.apply!(policy, project_root: root, approved_paths: targets)
  abort "confirmed cleanup did not delete the candidate" if File.exist?(candidate)
  abort "cleanup deleted retained data" unless File.file?(keep)
end

puts "Dora policy cleanup test passed (exact dry-run confirmation and retained-file protection)."
