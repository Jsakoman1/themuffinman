#!/usr/bin/env ruby
# frozen_string_literal: true
require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/java_spring"
Dir.mktmpdir("dora-java-spring") do |sandbox|
  FileUtils.mkdir_p(File.join(sandbox, "src"))
  File.write(File.join(sandbox, "src/App.java"), "@SpringBootApplication class App {}")
  File.write(File.join(sandbox, "src/Service.java"), "class Service {}")
  rows = Dora::Plugins::JavaSpring.discover(sandbox)
  abort "Java/Spring plugin discovery failed" unless rows == [{"path" => "src/App.java", "kind" => "spring_boot_application"}, {"path" => "src/Service.java", "kind" => "java_source"}]
end
puts "Dora Java/Spring source-map plugin test passed."
