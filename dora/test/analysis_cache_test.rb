#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require_relative "../lib/dora/analysis_cache"

Dir.mktmpdir("dora-analysis-cache") do |root|
  calls = 0
  first = Dora::AnalysisCache.fetch!(cache_root: root, key: "source-analysis", input: {"paths" => ["src/a.rb"]}) { calls += 1; {"findings" => ["a"]} }
  second = Dora::AnalysisCache.fetch!(cache_root: root, key: "source-analysis", input: {"paths" => ["src/a.rb"]}) { calls += 1; {"findings" => ["wrong"]} }
  changed = Dora::AnalysisCache.fetch!(cache_root: root, key: "source-analysis", input: {"paths" => ["src/b.rb"]}) { calls += 1; {"findings" => ["b"]} }
  abort "first source analysis did not run" if first.dig("cache", "hit")
  abort "unchanged source analysis did not hit cache" unless second.dig("cache", "hit") && second.fetch("value") == {"findings" => ["a"]}
  abort "changed source input did not invalidate cache" if changed.dig("cache", "hit") || calls != 2
end

puts "Dora analysis cache test passed (uncached result matches cached result and input changes invalidate it)."
