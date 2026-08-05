#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/repository_map"

def config(root, document)
  path = File.join(root, "map.yaml")
  File.write(path, YAML.dump(document))
  path
end

Dir.mktmpdir("dora-repository-map") do |sandbox|
  java_root = File.join(sandbox, "java")
  vue_root = File.join(sandbox, "vue")
  FileUtils.mkdir_p(File.join(java_root, "backend")); File.write(File.join(java_root, "backend/App.java"), "@SpringBootApplication class App {}")
  FileUtils.mkdir_p(File.join(vue_root, "frontend")); File.write(File.join(vue_root, "frontend/App.vue"), "<template/>")
  java = Dora::RepositoryMap.emit!(config(java_root, {"kind" => "dora_repository_map", "version" => 1, "source_roots" => [{"id" => "backend", "path" => "backend", "plugins" => ["java_spring"]}], "relationships" => [{"from" => "backend", "to" => "docs", "kind" => "documented_by"}]}), project_root: java_root)
  vue = Dora::RepositoryMap.emit!(config(vue_root, {"kind" => "dora_repository_map", "version" => 1, "source_roots" => [{"id" => "frontend", "path" => "frontend", "plugins" => ["typescript_vue"]}], "relationships" => []}), project_root: vue_root)
  abort "java map failed" unless java.fetch("sources").first.fetch("kind") == "spring_boot_application"
  abort "vue map failed" unless vue.fetch("sources").first.fetch("kind") == "vue_component"
  abort "maps leaked roots" unless java.fetch("sources").first.fetch("path").start_with?("backend/") && vue.fetch("sources").first.fetch("path").start_with?("frontend/")
end

puts "Dora repository-map test passed (two configured stacks)."
