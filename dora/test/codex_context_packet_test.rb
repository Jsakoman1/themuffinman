#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/codex_context_packet"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
packet = YAML.load_file(File.join(ROOT, "test/fixtures/codex-context-packet.yaml"))

result = Dora::CodexContextPacket.build!(packet)
abort "context packet changed allowed paths" unless result.fetch("allowed_paths") == packet.fetch("allowed_paths").sort
abort "context packet lost an explicit proof obligation" unless result.fetch("proof_obligations").map { |row| row.fetch("id") }.sort == %w[item-runtime item-unit]
abort "context packet lost omissions" unless result.fetch("omitted").include?("completion-conclusion")

%w[citations allowed_paths confirmed proof_obligations].each do |field|
  invalid = Marshal.load(Marshal.dump(packet))
  invalid.delete(field)
  begin
    Dora::CodexContextPacket.build!(invalid)
    abort "context packet accepted missing #{field}"
  rescue ArgumentError => error
    abort error.message unless error.message.downcase.include?(field.downcase)
  end
end

Dir.mktmpdir("dora-codex-context-packet") do |root|
  path = File.join(root, "packet.yaml")
  File.write(path, YAML.dump(packet))
  output, status = Open3.capture2e(CLI, "codex-context", path, "--format", "json", chdir: ROOT)
  abort "codex-context command failed: #{output}" unless status.success?
  envelope = JSON.parse(output)
  abort "codex-context command did not emit task context" unless envelope.dig("payload", "task", "id") == "record-item" && envelope.fetch("citations") == ["packet.yaml"]
end

puts "Dora Codex context packet test passed (cited declared context fails closed when required task guidance is absent)."
