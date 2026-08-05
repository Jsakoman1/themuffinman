#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/stack_catalog"

stack = Dora::StackCatalog.find!("spring-vue-postgres-buildable")
abort "Postgres starter path is wrong" unless stack.fetch("starter_pack") == "starters/spring-vue-postgres-buildable.yaml"
abort "Postgres catalog entry must describe Flyway" unless stack.fetch("capabilities").join(" ").downcase.include?("flyway")
exclusions = stack.fetch("exclusions").join(" ").downcase
abort "Postgres catalog entry lacks product boundary" unless exclusions.include?("product domain")
abort "Postgres catalog entry leaks MuffinMan" if stack.values.flatten.join(" ").downcase.include?("muffinman")

puts "Dora Postgres stack catalog test passed (the neutral technical starter is registered with explicit boundaries)."
