#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"

REPO_ROOT = File.expand_path("..", __dir__)
JAVA_ROOT = File.join(REPO_ROOT, "apps", "themuffinman", "src", "main", "java")
OUTPUT_PATH = File.join(REPO_ROOT, "docs", "generated", "automation-read-model-inventory.json")

def extract_record_fields(source)
  match = source.match(/public\s+record\s+\w+\s*\((.*?)\)\s*\{/m)
  return [] unless match

  match[1]
    .split(",")
    .map(&:strip)
    .reject(&:empty?)
    .map { |entry| entry.split.last.to_s.gsub(/[^\w]/, "") }
end

def extract_class_fields(source)
  source.scan(/^\s*private\s+(?:final\s+)?(?:static\s+final\s+)?[\w.<>, ?@"]+\s+(\w+)\s*;/).flatten
end

entries = []

Dir.glob(File.join(JAVA_ROOT, "**", "*.java")).sort.each do |file_path|
  source = File.read(file_path)
  package_name = source[/^package\s+([\w.]+);/, 1]
  type_name = source[/public\s+(?:record|class)\s+(\w+)/, 1]
  next unless package_name && type_name
  next unless type_name.end_with?("DTO", "Request", "Response")

  fields = extract_record_fields(source)
  fields = extract_class_fields(source) if fields.empty?
  next if fields.empty?

  entries << {
    "javaClass" => "#{package_name}.#{type_name}",
    "fields" => fields.uniq
  }
end

FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
File.write(OUTPUT_PATH, JSON.pretty_generate(entries) + "\n")
puts "Generated #{OUTPUT_PATH}"
