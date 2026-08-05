#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/backlog_todo"

Dir.mktmpdir("dora-backlog-todo") do |sandbox|
  alpha_backlog = File.join(sandbox, "alpha.md")
  beta_backlog = File.join(sandbox, "beta.md")
  alpha_source = File.join(sandbox, "alpha.rb")
  beta_source = File.join(sandbox, "beta.rb")
  alpha_config = File.join(sandbox, "alpha.yaml")
  beta_config = File.join(sandbox, "beta.yaml")
  File.write(alpha_backlog, "- ALPHA-101: pending\n")
  File.write(beta_backlog, "- BETA-202: pending\n")
  File.write(alpha_source, "# TODO(ALPHA-101): follow up\n")
  File.write(beta_source, "# FIXME(BETA-202): follow up\n")
  File.write(alpha_config, YAML.dump({"kind" => "dora_backlog", "version" => 1, "sources" => [alpha_backlog]}))
  File.write(beta_config, YAML.dump({"kind" => "dora_backlog", "version" => 1, "sources" => [beta_backlog]}))
  abort "alpha linkage failed" unless Dora::BacklogTodo.check!(alpha_config, source_files: [alpha_source]).fetch("todo_ids") == ["ALPHA-101"]
  abort "beta linkage failed" unless Dora::BacklogTodo.check!(beta_config, source_files: [beta_source]).fetch("todo_ids") == ["BETA-202"]
end
puts "Dora backlog TODO test passed (two project backlogs)."
