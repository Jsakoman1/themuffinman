#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"
require "yaml"

require_relative "../lib/dora/atomic_plan_contract"

DORA_BIN = File.expand_path("../bin/dora", __dir__)

def valid_plan
  {
    "kind" => "work",
    "version" => 1,
    "id" => "consumer-plan",
    "tasks" => [{"id" => "one", "title" => "One", "observable_outcome" => "One independently checked outcome", "dependencies" => [], "evidence_boundary" => ["unit test"], "paths" => ["lib/one.rb"], "required_paths" => ["lib/one.rb"], "validation" => "ruby test/one_test.rb"}]
  }
end

Dir.mktmpdir("dora-atomic-plan-contract") do |root|
  path = File.join(root, "plan.yaml")
  File.write(path, YAML.dump(valid_plan))
  result = Dora::AtomicPlanContract.validate!(path)
  abort "valid contract result is wrong" unless result == {"id" => "consumer-plan", "tasks" => 1}

  stdout, stderr, status = Open3.capture3(DORA_BIN, "plan-contract", path)
  abort "CLI did not validate a portable plan: #{stderr}" unless status.success? && stdout.include?("consumer-plan")

  invalid = valid_plan
  invalid["tasks"][0]["validation"] = "make work-verify plan=plan.yaml"
  File.write(path, YAML.dump(invalid))
  begin
    Dora::AtomicPlanContract.validate!(path)
    abort "recursive validation passed"
  rescue ArgumentError => error
    abort "wrong recursive validation failure" unless error.message.include?("recursive")
  end

  invalid = valid_plan
  invalid["tasks"][0]["paths"] = ["../outside.rb"]
  invalid["tasks"][0]["required_paths"] = ["../outside.rb"]
  File.write(path, YAML.dump(invalid))
  begin
    Dora::AtomicPlanContract.validate!(path)
    abort "unsafe path passed"
  rescue ArgumentError => error
    abort "wrong unsafe path failure" unless error.message.include?("unsafe")
  end
end

puts "Dora atomic plan contract test passed (portable CLI and invalid-plan rejection)."
