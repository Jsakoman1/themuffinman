#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/capability_proof_packet"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
matrix = YAML.load_file(File.join(ROOT, "templates/capability-proof-matrix.yaml"))
matrix.merge!("capability" => "record-item", "assertions" => [{"id" => "item-created", "statement" => "An authorized actor can record an item.", "confirmed" => true}], "obligations" => [{"id" => "item-unit", "assertion_id" => "item-created", "evidence_class" => "unit", "status" => "required", "boundary" => "A project-owned unit test must prove this assertion."}, {"id" => "item-browser", "assertion_id" => "item-created", "evidence_class" => "browser_runtime", "status" => "unresolved", "boundary" => "Browser execution requires explicit approval."}])

packet = Dora::CapabilityProofPacket.build!(matrix)
abort "proof packet lost required obligation" unless packet.fetch("obligations").map { |row| row.fetch("id") }.sort == %w[item-browser item-unit]
abort "proof packet did not expose browser approval gate" unless packet.fetch("approval_gates").map { |gate| gate.fetch("obligation_id") } == ["item-browser"]
invalid = Marshal.load(Marshal.dump(matrix)); invalid.fetch("obligations")[0]["assertion_id"] = "unknown"
begin
  Dora::CapabilityProofPacket.build!(invalid); abort "proof packet accepted unknown assertion"; rescue ArgumentError => error; abort error.message unless error.message.include?("unknown"); end

Dir.mktmpdir("dora-proof-packet") do |root|
  path = File.join(root, "proof.yaml"); File.write(path, YAML.dump(matrix))
  output, status = Open3.capture2e(CLI, "proof-packet", path, "--format", "json", chdir: ROOT)
  abort "proof-packet command failed: #{output}" unless status.success?
  abort "proof-packet command did not retain approval gate" unless JSON.parse(output).dig("payload", "approval_gates", 0, "obligation_id") == "item-browser"
end

puts "Dora capability proof packet test passed (declared proof remains unresolved and approval-gated where appropriate)."
