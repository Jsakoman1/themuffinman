#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/retention_review"

Dir.mktmpdir("dora-retention-review") do |root|
  FileUtils.mkdir_p(File.join(root, "reports"))
  File.write(File.join(root, "reports/retain.txt"), "retain")
  File.write(File.join(root, "reports/delete.tmp"), "candidate")
  policy = File.join(root, "retention.yaml")
  File.write(policy, YAML.dump({"kind" => "dora_retention_policy", "version" => 1, "generated_roots" => ["reports"], "retained_paths" => ["reports/retain.txt"], "cleanup_candidate_globs" => ["reports/*.tmp"]}))
  review = Dora::RetentionReview.review!(policy, project_root: root).to_h { |item| [item.fetch("path"), item.fetch("classification")] }
  abort "retained file was misclassified" unless review["reports/retain.txt"] == "retained"
  abort "cleanup candidate was misclassified" unless review["reports/delete.tmp"] == "cleanup_candidate"
  abort "read-only review changed a file" unless File.read(File.join(root, "reports/delete.tmp")) == "candidate"
end

puts "Dora retention review test passed (read-only retained and cleanup classifications)."
