#!/usr/bin/env ruby
# frozen_string_literal: true
require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/typescript_vue"
Dir.mktmpdir("dora-typescript-vue") do |sandbox|
  FileUtils.mkdir_p(File.join(sandbox, "ui"))
  File.write(File.join(sandbox, "ui/App.vue"), "<template/>")
  File.write(File.join(sandbox, "ui/client.ts"), "export {}")
  rows = Dora::Plugins::TypeScriptVue.discover(sandbox)
  abort "TypeScript/Vue plugin discovery failed" unless rows == [{"path" => "ui/App.vue", "kind" => "vue_component"}, {"path" => "ui/client.ts", "kind" => "typescript_source"}]
end
puts "Dora TypeScript/Vue source-map plugin test passed."
