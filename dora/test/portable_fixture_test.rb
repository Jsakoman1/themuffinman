#!/usr/bin/env ruby
# frozen_string_literal: true

require "open3"

ROOT = File.expand_path("..", __dir__)
ADAPTER = File.join(ROOT, "fixtures/standalone-project/.dora/project.yaml")
PLAN = "docs/work/example.yaml"

stdout, stderr, status = Open3.capture3(File.join(ROOT, "bin/dora"), "validate-adapter", ADAPTER, chdir: ROOT)
abort "standalone fixture adapter failed:\n#{stdout}\n#{stderr}" unless status.success?

stdout, stderr, status = Open3.capture3(File.join(ROOT, "bin/dora"), "validate-work-plan", ADAPTER, PLAN, chdir: ROOT)
abort "standalone fixture work plan failed:\n#{stdout}\n#{stderr}" unless status.success?

forbidden = %w[apps themuffinman].join("/")
fixture_files = Dir[File.join(ROOT, "fixtures/standalone-project/**/*")].select { |path| File.file?(path) }
leaks = fixture_files.select { |path| File.read(path).include?(forbidden) }
abort "standalone fixture names a consumer product path: #{leaks.join(", ")}" unless leaks.empty?

puts "Dora portable fixture test passed (standalone adapter and work plan)."
