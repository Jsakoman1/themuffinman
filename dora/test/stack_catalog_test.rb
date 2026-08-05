#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../lib/dora/stack_catalog"

catalog = Dora::StackCatalog.load!
abort "stack catalog omitted buildable starter" unless Dora::StackCatalog.find!("spring-vue-buildable").fetch("starter_pack") == "starters/spring-vue-buildable.yaml"
abort "stack catalog does not state exclusions" unless catalog.fetch("stacks").all? { |stack| !stack.fetch("exclusions").empty? }
abort "stack catalog leaked product data" if catalog.to_s.downcase.include?("muffinman")
puts "Dora stack catalog test passed (declared neutral starters retain technical capabilities and product exclusions)."
