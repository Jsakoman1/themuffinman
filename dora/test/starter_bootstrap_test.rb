#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")

Dir.mktmpdir("dora-starter-bootstrap") do |sandbox|
  source = File.join(sandbox, "reviewed-dora")
  FileUtils.cp_r(ROOT, source)
  descriptor = File.join(sandbox, "source.yaml")
  File.write(descriptor, YAML.dump({"kind" => "dora_bootstrap_source", "version" => 1, "source" => {"path" => source, "ref" => "c" * 40}}))
  {"blank" => ["src", "README.md"], "spring-vue" => ["backend/src/main/java", "frontend/src", ".dora/stack.yaml"]}.each do |starter, expected|
    destination = File.join(sandbox, starter)
    output, status = Open3.capture2e(CLI, "bootstrap", destination, "--project", "#{starter}-project", "--source", descriptor, "--starter", starter, chdir: ROOT)
    abort "#{starter} bootstrap failed: #{output}" unless status.success?
    abort "#{starter} did not create declared files" unless expected.all? { |relative| File.exist?(File.join(destination, relative)) }
    generated_application_code = Dir[File.join(destination, "{backend,frontend}/**/*.{java,vue,ts}")]
    abort "#{starter} generated application code" unless generated_application_code.empty?
  end
end

puts "Dora starter bootstrap test passed (two technical starters only)."
