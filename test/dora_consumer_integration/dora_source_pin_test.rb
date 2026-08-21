#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require "digest"

ROOT = File.expand_path("../..", __dir__)
CACHE_ROOT = File.join(Dir.home, "Library/Application Support/AISystem/tools")
RELEASE_VERSION = "v1.16.15"
SOURCE_COMMIT = "634b2315353caf1f8832e9b546aae76f45e86140"
PACKAGE_CHECKSUM = "88f7c04eeacdcfcbb494bf61674fa71341884b0c19662c1871c1d1d5407134df"

def package_checksum(root)
  entries = Dir.glob(File.join(root, "**", "*"), File::FNM_DOTMATCH).select do |path|
    relative = path.delete_prefix("#{root}/")
    File.file?(path) && relative != ".dora-release.yaml" && (relative.split("/") & %w[.DS_Store .git .idea]).empty?
  end.sort
  Digest::SHA256.hexdigest(entries.map { |path| relative = path.delete_prefix("#{root}/"); "#{relative}\0#{Digest::SHA256.file(path).hexdigest}" }.join("\n"))
end

adapter = YAML.load_file(File.join(ROOT, ".dora/project.yaml"))
lock = YAML.load_file(File.join(ROOT, "dora.lock.yaml"))
distribution = adapter.fetch("distribution")
release = File.join(CACHE_ROOT, "dora", lock.fetch("release_version"), lock.fetch("package_checksum"))
manifest = YAML.load_file(File.join(release, ".dora-release.yaml"))

expected_lock = {"kind" => "dora_locked_cli_lock", "version" => 1, "release_version" => RELEASE_VERSION, "source_commit" => SOURCE_COMMIT, "package_checksum" => PACKAGE_CHECKSUM, "adapter_contract_version" => 1, "cache_layout" => "user_application_support_v1"}
expected_distribution = {"method" => "locked_local_cli", "lock_path" => "dora.lock.yaml", "release_version" => RELEASE_VERSION, "source_commit" => SOURCE_COMMIT, "package_checksum" => PACKAGE_CHECKSUM, "cache_layout" => "user_application_support_v1"}

abort "MuffinMan Dora lock identity is invalid" unless lock == expected_lock
abort "MuffinMan Dora distribution is not aligned with the lock" unless expected_distribution.all? { |key, value| distribution[key] == value }
abort "MuffinMan locked Dora runtime is missing" unless File.file?(File.join(release, "bin/dora")) && File.directory?(File.join(release, "lib/dora"))
abort "MuffinMan locked Dora manifest is invalid" unless manifest.is_a?(Hash) && expected_lock.slice("release_version", "source_commit", "package_checksum", "adapter_contract_version").all? { |key, value| manifest[key] == value }
abort "MuffinMan locked Dora package checksum is invalid" unless package_checksum(release) == PACKAGE_CHECKSUM
abort "MuffinMan retains an obsolete vendored Dora runtime" if File.exist?(File.join(ROOT, "dora"))
abort "MuffinMan retains obsolete local Dora source descriptor" if File.exist?(File.join(ROOT, "dora-source.yaml"))
abort "MuffinMan retains obsolete bootstrap-source metadata" if File.exist?(File.join(ROOT, ".dora/bootstrap-source.yaml"))

puts "MuffinMan locked Dora CLI test passed (exact lock/cache identity and no stale vendored metadata)."
