#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/spring_mapper_usage"

Dir.mktmpdir("dora-spring-mapper") do |root|
  FileUtils.mkdir_p(File.join(root, "src/mapper"))
  FileUtils.mkdir_p(File.join(root, "src/service"))
  File.write(File.join(root, "src/mapper/ItemMapper.java"), "public class ItemMapper { public String toDto() { return \"\"; } }")
  File.write(File.join(root, "src/service/ItemService.java"), "class ItemService { private final ItemMapper mapper; void read() { mapper.toDto(); } }")
  result = Dora::Plugins::SpringMapperUsage.analyze!(root: root, mapper_glob: "src/mapper/*.java", source_root: "src")
  abort "mapper usage discovery is wrong" unless result == [{"mapper" => "ItemMapper", "path" => "src/mapper/ItemMapper.java", "methods" => ["toDto"], "usage_count" => 1, "callers" => [{"mapper" => "ItemMapper", "method" => "toDto", "file" => "src/service/ItemService.java"}]}]
end

puts "Dora Spring mapper usage plugin test passed (declared mapper and source roots)."
