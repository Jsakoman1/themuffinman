#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "tmpdir"
require_relative "../lib/dora/plugins/typescript_vue_ast_index"

Dir.mktmpdir("dora-typescript-vue-ast-index") do |root|
  FileUtils.mkdir_p(File.join(root, "frontend", "src"))
  File.write(File.join(root, "frontend", "package.json"), "{\"name\":\"portable-fixture\"}\n")
  parser = File.join(root, "fixture-parser.cjs")
  File.write(parser, <<~JAVASCRIPT)
    exports.parse = (source) => ({
      type: 'File',
      program: {
        type: 'Program',
        body: source.includes('function load')
          ? [{ type: 'FunctionDeclaration', id: { type: 'Identifier', name: 'load' } }]
          : [{ type: 'VariableDeclarator', id: { type: 'Identifier', name: 'title' } }],
      },
    });
  JAVASCRIPT
  File.write(File.join(root, "frontend", "src", "App.vue"), "<script lang=\"ts\">export const title = 'portable'</script>")
  File.write(File.join(root, "frontend", "src", "client.ts"), "import { title } from './App'; export function load() { return title; }")

  index = Dora::Plugins::TypeScriptVueAstIndex.index!(root: root, source_roots: [{"id" => "frontend", "path" => "frontend/src"}], package_root: "frontend", parser_module: parser)
  abort "portable TypeScript/Vue AST index did not retain project-relative paths" unless index.fetch("files").map { |file| file.fetch("path") }.sort == ["frontend/src/App.vue", "frontend/src/client.ts"]
  abort "portable TypeScript/Vue AST index did not retain TypeScript symbols" unless index.fetch("files").any? { |file| file.fetch("symbols").any? { |symbol| symbol == {"kind" => "FunctionDeclaration", "name" => "load"} } }
  abort "portable TypeScript/Vue AST index unexpectedly names MuffinMan" if JSON.generate(index).downcase.include?("muffinman")
end

puts "Dora TypeScript/Vue AST index test passed (portable declared parser fixture)."
