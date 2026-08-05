#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "fileutils"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

Dir.mktmpdir("dora-bootstrap-source") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.mkdir_p(File.join(source, "bin"))
  FileUtils.mkdir_p(File.join(source, "lib", "dora"))
  File.write(File.join(source, "bin", "dora"), "#!/usr/bin/env ruby\n")

  descriptor_path = File.join(sandbox, "source.yaml")
  descriptor = {
    "kind" => "dora_bootstrap_source",
    "version" => 1,
    "source" => {"path" => "reviewed-dora", "ref" => "a" * 40}
  }
  File.write(descriptor_path, YAML.dump(descriptor))

  resolved = Dora::BootstrapSource.load!(descriptor_path)
  abort "bootstrap source did not resolve a local source" unless resolved == {"path" => File.realpath(source), "ref" => "a" * 40}

  descriptor["source"]["path"] = "https://example.test/dora.git"
  begin
    Dora::BootstrapSource.validate!(descriptor, base_directory: sandbox)
    abort "bootstrap source accepted a remote URL"
  rescue ArgumentError => error
    abort "bootstrap source gave the wrong remote error" unless error.message.include?("remote URL")
  end

  descriptor["source"] = {"path" => "reviewed-dora", "ref" => "main"}
  begin
    Dora::BootstrapSource.validate!(descriptor, base_directory: sandbox)
    abort "bootstrap source accepted a mutable ref"
  rescue ArgumentError => error
    abort "bootstrap source gave the wrong ref error" unless error.message.include?("immutable commit")
  end
end

puts "Dora bootstrap source test passed (local reviewed sources only)."
