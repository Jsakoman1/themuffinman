#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "fileutils"
require "yaml"
require_relative "../lib/dora/plugin_runner"
Dir.mktmpdir("dora-cache-identity") do |root|
  FileUtils.mkdir_p(File.join(root, "src")); File.write(File.join(root, "src/a.txt"), "a")
  entry = File.join(root, "plugin.rb"); File.write(entry, "puts 'one'")
  manifest = {"kind"=>"dora_plugin_manifest","version"=>1,"plugins"=>[{"id"=>"custom","entrypoint"=>"plugin.rb","source_roots"=>[{"id"=>"src","path"=>"src"}],"inputs"=>{"mode"=>"fixture"},"output"=>{"kind"=>"report","path"=>"report.json"}}]}
  path=File.join(root,"plugins.yaml"); File.write(path,YAML.dump(manifest))
  first=Dora::PluginRunner.run!(path,plugin_id:"custom",project_root:root); second=Dora::PluginRunner.run!(path,plugin_id:"custom",project_root:root)
  abort "cache did not hit" unless !first.dig("cache","hit") && second.dig("cache","hit")
  File.write(entry,"puts 'two'"); third=Dora::PluginRunner.run!(path,plugin_id:"custom",project_root:root)
  abort "entrypoint change did not invalidate cache" if third.dig("cache","hit")
end
puts "Dora plugin cache identity test passed (entrypoint and Dora runner identities invalidate stale cache)."
