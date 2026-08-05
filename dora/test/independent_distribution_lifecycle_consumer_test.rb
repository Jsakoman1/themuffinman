#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

ROOT = File.expand_path("..", __dir__)
FIXTURE = File.join(ROOT, "test/fixtures/distribution-lifecycle-sources.yaml")

def write_source(root, marker)
  FileUtils.mkdir_p(File.join(root, "bin"))
  FileUtils.mkdir_p(File.join(root, "lib/dora"))
  File.write(File.join(root, "bin/dora"), "#!/usr/bin/env ruby\n")
  File.write(File.join(root, "lib/dora/marker.rb"), "MARKER = '#{marker}'\n")
end

def snapshot(root)
  Dir[File.join(root, "**/*")].select { |path| File.file?(path) }.sort.to_h { |path| [path.delete_prefix("#{root}/"), Digest::SHA256.file(path).hexdigest] }
end

Dir.mktmpdir("dora-distribution-lifecycle-consumer") do |sandbox|
  fixture = YAML.load_file(FIXTURE)
  reviewed = File.join(sandbox, "reviewed-dora")
  write_source(reviewed, "reviewed")
  descriptor = File.join(sandbox, "reviewed-source.yaml")
  File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "reviewed-dora", "ref" => fixture.fetch("reviewed_ref"), "checksum" => Dora::BootstrapSource.checksum_for(reviewed)}}))
  verified = Dora::BootstrapSource.load!(descriptor)
  abort "independent lifecycle source was not verified" unless verified.fetch("integrity") == "verified"

  fixture.fetch("consumers").each do |consumer_spec|
    consumer = File.join(sandbox, consumer_spec.fetch("id"))
    write_source(File.join(consumer, "dora"), consumer_spec.fetch("marker"))
    FileUtils.mkdir_p(File.join(consumer, ".dora"))
    File.write(File.join(consumer, ".dora/bootstrap-source.yaml"), YAML.dump({"kind" => "dora_bootstrap_record", "version" => 1, "source" => {"path" => "previous-reviewed-source", "ref" => fixture.fetch("consumer_ref"), "checksum" => "c" * 64}, "package_path" => "dora"}))
    before = snapshot(consumer)
    output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "upgrade-preview", consumer, "--source", descriptor)
    abort "independent lifecycle preview failed: #{output}" unless status.success?
    preview = YAML.safe_load(output)
    abort "independent lifecycle preview lost target source" unless preview.dig("target", "ref") == fixture.fetch("reviewed_ref") && preview.dig("migrations", "changed").include?("lib/dora/marker.rb")
    abort "independent lifecycle preview mutated a consumer" unless preview.fetch("read_only") && snapshot(consumer) == before
    content = Dir[File.join(consumer, "**/*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join("\n")
    abort "independent lifecycle consumer refers to MuffinMan" if content.downcase.include?("muffinman")
  end
end

puts "Dora independent distribution lifecycle consumer test passed (two verified-source consumers preview upgrades without mutation or MuffinMan data)."
