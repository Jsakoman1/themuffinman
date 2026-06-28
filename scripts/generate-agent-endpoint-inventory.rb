#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"

REPO_ROOT = File.expand_path("..", __dir__)
JAVA_ROOT = File.join(REPO_ROOT, "apps", "themuffinman", "src", "main", "java", "com", "themuffinman", "app")
OUTPUT_PATH = File.join(REPO_ROOT, "docs", "generated", "agent-endpoint-inventory.json")
HTTP_ANNOTATIONS = {
  "GetMapping" => "GET",
  "PostMapping" => "POST",
  "PutMapping" => "PUT",
  "PatchMapping" => "PATCH",
  "DeleteMapping" => "DELETE"
}.freeze

def normalize_path(value)
  collapsed = value.gsub(%r{/+}, "/")
  collapsed = collapsed.sub(%r{/\z}, "")
  return "/" if collapsed.empty?

  collapsed.start_with?("/") ? collapsed : "/#{collapsed}"
end

def extract_mapping_argument(line)
  match = line.match(/\(\s*"([^"]*)"\s*\)/)
  return match[1] if match

  ""
end

entries = []

Dir.glob(File.join(JAVA_ROOT, "**", "*Controller.java")).sort.each do |file_path|
  source = File.read(file_path)
  package_name = source[/^package\s+([\w.]+);/, 1]
  class_name = source[/class\s+(\w+)/, 1]
  controller_class = "#{package_name}.#{class_name}"
  class_path = source[/@RequestMapping\(\s*"([^"]*)"\s*\)/, 1] || ""
  pending_annotation = nil

  source.each_line do |line|
    stripped = line.strip

    HTTP_ANNOTATIONS.each_key do |annotation|
      next unless stripped.start_with?("@#{annotation}")

      pending_annotation = [annotation, extract_mapping_argument(stripped)]
    end

    next unless pending_annotation

    method_name = stripped[/public\s+[^\(]+\s+(\w+)\s*\(/, 1]
    next unless method_name

    annotation, method_path = pending_annotation
    entries << {
      "controllerClass" => controller_class,
      "handlerMethod" => method_name,
      "httpMethod" => HTTP_ANNOTATIONS.fetch(annotation),
      "path" => normalize_path("#{class_path}/#{method_path}".gsub(%r{//+}, "/"))
    }
    pending_annotation = nil
  end
end

FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
File.write(OUTPUT_PATH, JSON.pretty_generate(entries) + "\n")
puts "Generated #{OUTPUT_PATH}"
