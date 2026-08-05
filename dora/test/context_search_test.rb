#!/usr/bin/env ruby
# frozen_string_literal: true
require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/context_search"
Dir.mktmpdir("dora-context-search") do |sandbox|
  %w[alpha beta].each { |name| FileUtils.mkdir_p(File.join(sandbox, name, "src")) }
  File.write(File.join(sandbox, "alpha/src/a.txt"), "needle\n"); File.write(File.join(sandbox, "beta/src/b.txt"), "needle\n")
  alpha = File.join(sandbox, "alpha.yaml"); beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_context_search", "version" => 1, "roots" => [File.join(sandbox, "alpha/src")], "exclusions" => []}))
  File.write(beta, YAML.dump({"kind" => "dora_context_search", "version" => 1, "roots" => [File.join(sandbox, "beta/src")], "exclusions" => []}))
  abort "alpha search leaked project scope" unless Dora::ContextSearch.search!(alpha, "needle") == [File.join(sandbox, "alpha/src/a.txt")]
  abort "beta search leaked project scope" unless Dora::ContextSearch.search!(beta, "needle") == [File.join(sandbox, "beta/src/b.txt")]
end
puts "Dora context search test passed (two bounded project roots)."
