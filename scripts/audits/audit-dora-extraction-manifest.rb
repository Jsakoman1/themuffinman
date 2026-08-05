#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
MANIFEST_PATH = File.join(ROOT, "docs/dora-extraction-manifest.yaml")
HANDOFF_PATH = File.join(ROOT, "docs/dora-release-handoff.yaml")
require_approved_release = ARGV.delete("--require-approved-release")
abort "usage: ruby scripts/audits/audit-dora-extraction-manifest.rb [--require-approved-release]" unless ARGV.empty?

def repository_paths
  tracked, tracked_status = Open3.capture2("git", "-C", ROOT, "ls-files")
  abort "cannot read tracked repository paths" unless tracked_status.success?
  untracked, untracked_status = Open3.capture2("git", "-C", ROOT, "ls-files", "--others", "--exclude-standard")
  abort "cannot read untracked repository paths" unless untracked_status.success?
  (tracked.lines + untracked.lines).map(&:strip).reject(&:empty?).uniq.sort
end

def matches_prefix?(path, prefix)
  prefix.end_with?("/") ? path.start_with?(prefix) : path == prefix
end

manifest = YAML.load_file(MANIFEST_PATH)
failures = []
failures << "extraction manifest kind is invalid" unless manifest["kind"] == "dora_extraction_manifest" && manifest["version"].to_i == 1
source = manifest.fetch("source_repository", {})
baseline = source["baseline"].to_s
_output, status = Open3.capture2e("git", "-C", ROOT, "cat-file", "-e", "#{baseline}^{commit}")
failures << "manifest baseline is not a Git commit" unless status.success?
failures << "Dora package prefix is invalid" unless source["dora_package_prefix"] == "dora/"
valid_distribution = (manifest["status"] == "prepared_local_only" && source["current_distribution"] == "embedded_local_package") ||
  (manifest["status"] == "released" && source["current_distribution"] == "git_subtree_pinned")
failures << "manifest has an invalid distribution state" unless valid_distribution

ownership = manifest.fetch("ownership", {})
dora_owned = Array(ownership["dora_owned"])
failures << "Dora ownership list is empty" if dora_owned.empty?
dora_owned.each { |path| failures << "Dora-owned path is missing: #{path}" unless File.exist?(File.join(ROOT, path)) }
retained = Array(ownership["muffinman_retained_prefixes"])
operational = Array(ownership["repository_operational_prefixes"])
unclassified = repository_paths.reject do |path|
  matches_prefix?(path, source["dora_package_prefix"]) || retained.any? { |prefix| matches_prefix?(path, prefix) } || operational.any? { |prefix| matches_prefix?(path, prefix) }
end
failures << "unclassified repository paths: #{unclassified.join(", ")}" unless unclassified.empty?

release_gate = manifest.fetch("release_gate", {})
failures << "release gate lacks required decisions" if Array(release_gate["required_decisions"]).length < 4
allowed_release_states = ["awaiting_explicit_external_approval", "approved_and_published"]
failures << "release gate state is invalid" unless allowed_release_states.include?(release_gate["current_state"])
prohibited = Array(manifest.dig("preservation", "prohibited_before_approval"))
failures << "pre-approval deletion is not prohibited" unless prohibited.include?("deleting source-repository paths")
failures << "pre-approval history rewrite is not prohibited" unless prohibited.include?("rewriting source-repository history")

if require_approved_release
  handoff = YAML.load_file(HANDOFF_PATH)
  failures << "release handoff kind is invalid" unless handoff["kind"] == "dora_release_handoff" && handoff["status"] == "published"
  release = handoff.fetch("release", {})
  consumption = handoff.fetch("consumption", {})
  %w[repository version immutable_commit].each { |field| failures << "release handoff is missing #{field}" if release[field].to_s.empty? }
  failures << "release handoff does not declare git_subtree" unless consumption["method"] == "git_subtree"
  failures << "release handoff pin differs from immutable commit" unless consumption["pinned_commit"] == release["immutable_commit"]
  adapter = YAML.load_file(File.join(ROOT, ".dora/project.yaml"))
  distribution = adapter.fetch("distribution", {})
  failures << "adapter source commit differs from release handoff" unless distribution["source_commit"] == release["immutable_commit"]
  failures << "adapter source ref differs from release handoff" unless distribution["source_ref"] == release["version"]
  remote_output, remote_status = Open3.capture2e("git", "ls-remote", distribution["source_repository"], "refs/tags/#{release["version"]}^{}")
  failures << "published Dora tag is not reachable" unless remote_status.success? && remote_output.include?(release["immutable_commit"])
end

abort "Dora extraction manifest audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Dora extraction manifest audit passed (#{repository_paths.length} classified repository paths; #{manifest["status"]})."
