#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/plugin_runner"

def policy(trust)
  {"trust" => trust, "timeout_seconds" => 5, "isolation" => "none", "unsupported_guarantees" => Dora::PluginContract::REQUIRED_UNSUPPORTED_GUARANTEES}
end

Dir.mktmpdir("dora-plugin-read-boundary") do |root|
  FileUtils.mkdir_p(File.join(root, "src")); FileUtils.mkdir_p(File.join(root, "plugins"))
  File.write(File.join(root, "src/sample.txt"), "sample\n")
  File.write(File.join(root, "plugins/custom.rb"), "puts 'custom diagnostic'\n")
  manifest = {"kind" => "dora_plugin_manifest", "version" => 2, "plugins" => [{"id" => "custom", "entrypoint" => "plugins/custom.rb", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"mode" => "test"}, "output" => {"kind" => "report", "path" => "docs/audit-output/custom.json"}, "execution_policy" => policy("project_trusted")}, {"id" => "builtin", "builtin" => "http-contract-linker", "source_roots" => [{"id" => "source", "path" => "src"}], "inputs" => {"controller_glob" => "src/**/*.java", "client_glob" => "src/**/*.ts"}, "output" => {"kind" => "report", "path" => "docs/audit-output/builtin.json"}, "execution_policy" => policy("dora_builtin")}]} 
  path = File.join(root, "plugins.yaml"); File.write(path, YAML.dump(manifest))
  %w[custom builtin].each do |id|
    Dora::PluginRunner.run!(path, plugin_id: id, project_root: root)
    report = JSON.parse(File.read(File.join(root, "docs/audit-output/#{id}.json")))
    boundary = report.fetch("read_boundary")
    abort "#{id} lost declared root" unless boundary.fetch("declared_source_roots") == [{"id" => "source", "path" => "src"}]
    abort "#{id} claimed a sandbox" if boundary.to_s.downcase.include?("filesystem-sandboxed") && !boundary.to_s.include?("not filesystem-sandboxed")
    abort "#{id} omitted unsupported guarantees" unless boundary.fetch("unsupported_guarantees").include?("filesystem_isolation")
  end
end
puts "Dora plugin read boundary test passed (custom and built-in reports distinguish declared roots from absent sandbox guarantees)."
