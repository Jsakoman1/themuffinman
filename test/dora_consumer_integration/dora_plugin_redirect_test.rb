#!/usr/bin/env ruby
# frozen_string_literal: true

# These redirects belong to the MuffinMan consumer because they intentionally
# name its compatibility shells and application-local frontend path.
ROOT = File.expand_path("../..", __dir__)

java_shell = File.read(File.join(ROOT, "scripts/RepositoryJavaAstIndex.java"))
abort "MuffinMan Java AST source was not redirected to Dora" unless java_shell.include?("dora/tools/java-ast-index")

frontend_shell = File.read(File.join(ROOT, "apps/themuffinman/frontend/scripts/repository-ast-index.mjs"))
abort "MuffinMan frontend AST source was not redirected to Dora" unless frontend_shell.include?("dora', 'tools', 'typescript-vue-ast-index.mjs")

puts "MuffinMan Dora parser redirect integration test passed."
