#!/usr/bin/env ruby
# frozen_string_literal: true

# The consumer declares roots and invokes only locked public Dora commands; it
# does not resolve package internals or retain obsolete compatibility shells.
ROOT = File.expand_path("../..", __dir__)

makefile = File.read(File.join(ROOT, "Makefile"))
abort "repository map does not use the locked public command" unless makefile.include?("bin/dora repository-map .dora/project.yaml --config .dora/repository-map.yaml")
abort "symbol discovery does not use the bounded consumer search" unless makefile.include?("scripts/context-search.rb --mode symbol")

private_paths = %w[scripts/RepositoryJavaAstIndex.java apps/themuffinman/frontend/scripts/repository-ast-index.mjs scripts/repository-map.rb]
abort "private Dora compatibility path remains" if private_paths.any? { |path| File.exist?(File.join(ROOT, path)) }

verify_wrapper = File.read(File.join(ROOT, "scripts/verify-work.rb"))
abort "work verifier compatibility entrypoint still loads package internals" if verify_wrapper.include?("dora/lib/")
abort "work verifier compatibility entrypoint does not delegate to locked Dora" unless verify_wrapper.include?('File.join(ROOT, "bin/dora")') && verify_wrapper.include?('"work-verify"')

puts "MuffinMan locked Dora source-tooling integration test passed (public repository map, consumer search, and no package-internal redirects)."
