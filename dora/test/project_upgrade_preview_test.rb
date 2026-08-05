#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/bootstrap_source"

ROOT = File.expand_path("..", __dir__)

def snapshot(root)
  Dir[File.join(root, "**/*")].select { |path| File.file?(path) }.sort.to_h { |path| [path.delete_prefix("#{root}/"), Digest::SHA256.file(path).hexdigest] }
end

def write_source(root, marker)
  FileUtils.mkdir_p(File.join(root, "bin"))
  FileUtils.mkdir_p(File.join(root, "lib/dora"))
  File.write(File.join(root, "bin/dora"), "#!/usr/bin/env ruby\n")
  File.write(File.join(root, "lib/dora/marker.rb"), "MARKER = '#{marker}'\n")
end

Dir.mktmpdir("dora-project-upgrade-preview") do |sandbox|
  target_source = File.join(sandbox, "reviewed-source")
  write_source(target_source, "new")
  descriptor = File.join(sandbox, "source.yaml")
  File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => "reviewed-source", "ref" => "b" * 40, "checksum" => Dora::BootstrapSource.checksum_for(target_source)}}))
  ["alpha", "beta"].each do |id|
    consumer = File.join(sandbox, id)
    package = File.join(consumer, "dora")
    write_source(package, "old-#{id}")
    FileUtils.mkdir_p(File.join(consumer, ".dora"))
    File.write(File.join(consumer, ".dora/bootstrap-source.yaml"), YAML.dump({"kind" => "dora_bootstrap_record", "version" => 1, "source" => {"path" => "old-source", "ref" => "a" * 40, "checksum" => "c" * 64}, "package_path" => "dora"}))
    before = snapshot(consumer)
    output, status = Open3.capture2e(File.join(ROOT, "bin/dora"), "upgrade-preview", consumer, "--source", descriptor)
    abort "upgrade preview failed: #{output}" unless status.success?
    preview = YAML.safe_load(output)
    abort "upgrade preview omitted target pin" unless preview.dig("target", "ref") == "b" * 40 && preview.dig("migrations", "changed").include?("lib/dora/marker.rb")
    abort "upgrade preview was not read-only" unless preview.fetch("read_only") && snapshot(consumer) == before
  end
end

puts "Dora project upgrade preview test passed (two consumer previews report migrations without modifying pinned projects)."
