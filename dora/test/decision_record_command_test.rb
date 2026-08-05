#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
args = ["--id", "DEC-1", "--decision", "Use PostgreSQL.", "--status", "accepted", "--domain-ref", "docs/domain.yaml#storage", "--plan-ref", "docs/work/storage.yaml#select", "--evidence-ref", "docs/evidence/storage.json"]
Dir.mktmpdir("dora-decision-record") do |root|
  log = File.join(root, "decisions.yaml")
  output, status = Open3.capture2e(CLI, "decision-record", log, *args, "--format", "json", chdir: ROOT)
  abort "decision append failed: #{output}" unless status.success? && JSON.parse(output).dig("payload", "record", "id") == "DEC-1"
  second = args.each_slice(2).flat_map { |key, value| key == "--id" ? [key, "DEC-2"] : [key, value] }
  output, status = Open3.capture2e(CLI, "decision-record", log, *second, chdir: ROOT)
  abort "second decision append failed: #{output}" unless status.success?
  abort "prior decision was not preserved" unless YAML.load_file(log).fetch("entries").map { |entry| entry.fetch("id") }.sort == %w[DEC-1 DEC-2]
  _output, duplicate = Open3.capture2e(CLI, "decision-record", log, *args, chdir: ROOT)
  abort "duplicate decision id was accepted" if duplicate.success?
end
puts "Dora decision record command test passed (cited append-only records preserve prior decisions and reject duplicates)."
