#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "tmpdir"
require_relative "../lib/dora/plugins/typescript_vue_ast_index"

parser_modules = File.expand_path("../../apps/themuffinman/frontend/node_modules", __dir__)
abort "fixture parser dependency is missing" unless File.directory?(parser_modules)

Dir.mktmpdir("dora-typescript-vue-ast-index") do |root|
  FileUtils.mkdir_p(File.join(root, "frontend", "src"))
  File.write(File.join(root, "frontend", "package.json"), "{\"name\":\"portable-fixture\"}\n")
  File.symlink(parser_modules, File.join(root, "frontend", "node_modules"))
  File.write(File.join(root, "frontend", "src", "App.vue"), "<script lang=\"ts\">export const title = 'portable'</script>")
  File.write(File.join(root, "frontend", "src", "client.ts"), "import { title } from './App'; export function load() { return title; }")

  index = Dora::Plugins::TypeScriptVueAstIndex.index!(root: root, source_roots: [{"id" => "frontend", "path" => "frontend/src"}], package_root: "frontend")
  abort "portable TypeScript/Vue AST index did not retain project-relative paths" unless index.fetch("files").map { |file| file.fetch("path") }.sort == ["frontend/src/App.vue", "frontend/src/client.ts"]
  abort "portable TypeScript/Vue AST index did not retain TypeScript symbols" unless index.fetch("files").any? { |file| file.fetch("symbols").any? { |symbol| symbol == {"kind" => "FunctionDeclaration", "name" => "load"} } }
  abort "portable TypeScript/Vue AST index unexpectedly names MuffinMan" if JSON.generate(index).downcase.include?("muffinman")
end

legacy = File.read(File.expand_path("../../apps/themuffinman/frontend/scripts/repository-ast-index.mjs", __dir__))
abort "MuffinMan frontend AST source was not redirected to Dora" unless legacy.include?("dora', 'tools', 'typescript-vue-ast-index.mjs")

puts "Dora TypeScript/Vue AST index test passed (declared consumer parser and redirected MuffinMan compatibility shell)."
