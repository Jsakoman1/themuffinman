#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../local_tooling_common"

options = ARGV.each_with_object({}) do |arg, parsed|
  key, value = arg.split("=", 2)
  parsed[key] = value if value
end

max_lines = (options["max_lines"] || options["max-lines"] || "80").to_i
aggressive = LocalToolingCommon.truthy?(options["aggressive"])
input =
  if options["file"] && !options["file"].to_s.empty?
    File.read(options["file"])
  else
    STDIN.read
  end

puts LocalToolingCommon.clean_text_output(input, max_lines: max_lines, aggressive: aggressive)
