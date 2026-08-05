#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/plugins/spring_configuration_drift"

Dir.mktmpdir("dora-spring-config") do |root|
  FileUtils.mkdir_p(File.join(root, "config"))
  File.write(File.join(root, "config/application.properties"), "app.token=${APP_TOKEN}\napp.mode=local\ndatabase.password=${DATABASE_PASSWORD}\n")
  result = Dora::Plugins::SpringConfigurationDrift.analyze!(root: root, properties_path: "config/application.properties", property_prefixes: ["app."])
  abort "configuration property parsing is wrong" unless result.fetch("properties").length == 3
  abort "unmapped declared properties are wrong" unless result.fetch("unmapped").map { |property| property["name"] } == ["app.mode"]

  begin
    Dora::Plugins::SpringConfigurationDrift.analyze!(root: root, properties_path: "../outside.properties", property_prefixes: ["app."])
    abort "unsafe configuration path passed"
  rescue ArgumentError => error
    abort "wrong unsafe configuration failure" unless error.message.include?("invalid")
  end
end

puts "Dora Spring configuration drift plugin test passed (declared properties root and redacted parsing)."
