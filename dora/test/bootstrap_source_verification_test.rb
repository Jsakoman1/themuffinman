#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

Dir.mktmpdir("dora-bootstrap-source-verification") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.mkdir_p(File.join(source, "bin"))
  FileUtils.mkdir_p(File.join(source, "lib", "dora"))
  File.write(File.join(source, "bin", "dora"), "#!/usr/bin/env ruby\n")
  File.write(File.join(source, "lib", "dora", "marker.rb"), "REVIEWED = true\n")
  descriptor = {"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "reviewed-dora", "ref" => "a" * 40, "checksum" => Dora::BootstrapSource.checksum_for(source)}}
  descriptor_path = File.join(sandbox, "source.yaml")
  File.write(descriptor_path, YAML.dump(descriptor))
  verified = Dora::BootstrapSource.load!(descriptor_path)
  abort "source descriptor did not verify its immutable ref and checksum" unless verified.slice("ref", "checksum", "integrity") == {"ref" => "a" * 40, "checksum" => descriptor.dig("source", "checksum"), "integrity" => "verified"}
  File.write(File.join(source, "lib", "dora", "marker.rb"), "REVIEWED = false\n")
  begin
    Dora::BootstrapSource.load!(descriptor_path)
    abort "source descriptor accepted tampered source content"
  rescue ArgumentError => error
    abort "source descriptor gave the wrong tampering error" unless error.message.include?("checksum does not match")
  end
end

puts "Dora bootstrap source verification test passed (reviewed immutable refs and checksums reject tampered local source content)."
