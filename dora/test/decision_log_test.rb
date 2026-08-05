#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/decision_log"

Dir.mktmpdir("dora-decision-log") do |root|
  log = {"kind" => "dora_decision_log", "version" => 1, "entries" => [{"id" => "DEC-2", "decision" => "Keep one group owner.", "status" => "accepted", "domain_references" => ["domain-library.yaml#group"], "plan_references" => ["docs/work/group.yaml#owner"], "evidence_references" => ["docs/audit-output/group-test.txt"]}, {"id" => "DEC-1", "decision" => "Start notes as drafts.", "status" => "proposed", "domain_references" => ["domain-library.yaml#note"], "plan_references" => ["docs/work/note.yaml#draft"], "evidence_references" => ["docs/audit-output/note-test.txt"]}]}
  path = File.join(root, "decisions.yaml")
  File.write(path, YAML.dump(log))
  result = Dora::DecisionLog.load!(path)
  abort "decision log did not retain stable ordering" unless result.fetch("entries").map { |entry| entry.fetch("id") } == %w[DEC-1 DEC-2]
  abort "decision log lost evidence reference" unless result.fetch("entries").first.fetch("evidence_references") == ["docs/audit-output/note-test.txt"]
  abort "decision log claimed release approval" unless result.fetch("completion_boundary").include?("does not prove")
end

puts "Dora decision log test passed (stable decisions link declared domain, plan, and evidence references)."
