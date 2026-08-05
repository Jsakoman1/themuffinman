#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
require File.join(ROOT, "dora/lib/dora/work_execution")

begin
  puts Dora::WorkExecution.run(
    adapter_path: File.join(ROOT, ".dora/project.yaml"),
    schema_path: File.join(ROOT, "dora/project-adapter.schema.yaml"),
    arguments: ARGV
  )
rescue Dora::WorkExecution::Error, ArgumentError => error
  warn "Work verification failed: #{error.message}"
  exit 1
end
