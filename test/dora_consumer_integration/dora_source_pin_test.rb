#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"
require "pathname"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
DESCRIPTOR_PATH = File.join(ROOT, "dora-source.yaml")
EXPECTED_REF = "5f5a915ab043060cbe17735e4f2a8c6628e47447"
EXPECTED_CHECKSUM = "fcad0c003111b80c470f811fb03c4bb4518beaae53a038eb22f7fa678c3270c6"

descriptor = YAML.load_file(DESCRIPTOR_PATH)
source = descriptor.fetch("source")

abort "MuffinMan Dora source descriptor has the wrong immutable ref" unless source.fetch("ref") == EXPECTED_REF
abort "MuffinMan Dora source descriptor has the wrong source checksum" unless source.fetch("checksum") == EXPECTED_CHECKSUM
abort "MuffinMan Dora source default path must remain relative" if Pathname.new(source.fetch("path")).absolute?
abort "MuffinMan Dora source override environment is missing" unless descriptor.dig("local_override", "environment") == "DORA_SOURCE_PATH"

default_root = File.expand_path(source.fetch("path"), ROOT)
require File.join(default_root, "lib/dora/bootstrap_source")

def local_source_path(descriptor, environment)
  override = environment.fetch("DORA_SOURCE_PATH", "").strip
  override.empty? ? descriptor.dig("source", "path") : override
end

def verify_source!(descriptor, candidate_path)
  candidate = descriptor.merge("source" => descriptor.fetch("source").merge("path" => candidate_path))
  verified = Dora::BootstrapSource.validate!(candidate, base_directory: ROOT)
  output, status = Open3.capture2e("git", "-C", verified.fetch("path"), "rev-parse", "HEAD")
  raise "Dora source is not a local Git repository: #{output}" unless status.success?
  raise "Dora source Git commit does not match the pinned ref" unless output.strip == descriptor.dig("source", "ref")
  raise "Dora source checksum does not match the pinned checksum" unless Dora::BootstrapSource.checksum_for(verified.fetch("path")) == descriptor.dig("source", "checksum")

  verified
end

default = verify_source!(descriptor, local_source_path(descriptor, {}))
abort "MuffinMan Dora source default did not resolve to the reviewed source" unless default.slice("ref", "checksum", "integrity") == {"ref" => EXPECTED_REF, "checksum" => EXPECTED_CHECKSUM, "integrity" => "verified"}

original_override = ENV["DORA_SOURCE_PATH"]
begin
  ENV["DORA_SOURCE_PATH"] = default.fetch("path")
  override = verify_source!(descriptor, local_source_path(descriptor, ENV))
  abort "MuffinMan Dora source override did not verify the same reviewed source" unless override == default
ensure
  ENV["DORA_SOURCE_PATH"] = original_override
end

begin
  verify_source!(descriptor, "https://example.invalid/dora.git")
  abort "MuffinMan Dora source accepted a remote override"
rescue ArgumentError => error
  abort "MuffinMan Dora source gave the wrong remote override error" unless error.message.include?("remote URL")
end

begin
  verify_source!(descriptor.merge("source" => source.merge("ref" => "0" * 40)), default.fetch("path"))
  abort "MuffinMan Dora source accepted an override with a mismatched Git ref"
rescue RuntimeError => error
  abort "MuffinMan Dora source gave the wrong Git ref error" unless error.message.include?("Git commit")
end

begin
  verify_source!(descriptor.merge("source" => source.merge("checksum" => "0" * 64)), default.fetch("path"))
  abort "MuffinMan Dora source accepted an override with a mismatched checksum"
rescue ArgumentError => error
  abort "MuffinMan Dora source gave the wrong checksum error" unless error.message.include?("checksum does not match")
end

plugin_manifest = File.read(File.join(ROOT, ".dora/plugins.yaml"))
java_shell = File.read(File.join(ROOT, "scripts/RepositoryJavaAstIndex.java"))
frontend_shell = File.read(File.join(ROOT, "apps/themuffinman/frontend/scripts/repository-ast-index.mjs"))
abort "MuffinMan plugin manifest was redirected through the Dora source pin" if plugin_manifest.include?("dora-source.yaml") || plugin_manifest.include?("DORA_SOURCE_PATH")
abort "MuffinMan Java wrapper was redirected through the Dora source pin" unless java_shell.include?("dora/tools/java-ast-index")
abort "MuffinMan frontend wrapper was redirected through the Dora source pin" unless frontend_shell.include?("dora', 'tools', 'typescript-vue-ast-index.mjs")

puts "MuffinMan Dora source pin test passed (local default and override verify immutable Git and checksum identity without redirecting the consumer workflow)."
