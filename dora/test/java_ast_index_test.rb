#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "tmpdir"
require_relative "../lib/dora/plugins/java_ast_index"

Dir.mktmpdir("dora-java-ast-index") do |root|
  FileUtils.mkdir_p(File.join(root, "backend", "src", "example"))
  File.write(File.join(root, "backend", "src", "example", "Sample.java"), <<~JAVA)
    package example;
    import java.util.List;
    public class Sample {
      public String name() { return "sample"; }
    }
  JAVA

  index = Dora::Plugins::JavaAstIndex.index!(root: root, source_roots: [{"id" => "backend", "path" => "backend/src"}])
  file = index.fetch("files").first
  abort "portable Java AST index did not retain the project-relative path" unless file.fetch("path") == "backend/src/example/Sample.java"
  abort "portable Java AST index did not retain package data" unless file.fetch("package") == "example"
  abort "portable Java AST index did not retain method symbols" unless file.fetch("symbols").any? { |symbol| symbol == {"kind" => "method", "name" => "name"} }
  abort "portable Java AST index unexpectedly names MuffinMan" if JSON.generate(index).downcase.include?("muffinman")
end

puts "Dora Java AST index test passed (portable JDK parser)."
