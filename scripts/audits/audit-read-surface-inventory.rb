#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"
require_relative "../../dora/lib/dora/plugins/vue_surface_hygiene"

ROOT = File.expand_path("../..", __dir__)
result = Dora::Plugins::VueSurfaceHygiene.scan!(root: ROOT, source_glob: "apps/themuffinman/src/main/java/com/themuffinman/app/*/service/*.java")
FileUtils.mkdir_p(File.join(ROOT, "docs/audit-output"))
File.write(File.join(ROOT, "docs/audit-output/read-surface-inventory.json"), JSON.pretty_generate(result.merge("advisory_only" => true)) + "\n")
puts "Read surface inventory passed (#{result.fetch("files").length} declared service files for static review)."
