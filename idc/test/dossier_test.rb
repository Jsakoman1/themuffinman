# frozen_string_literal: true

require "digest"
require "tmpdir"
require "yaml"
require_relative "../lib/idc/dossier"

ROOT = File.expand_path("../..", __dir__)

def assert(message, &block)
  raise "assertion failed: #{message}" unless block.call
end

def assert_raises(message, klass = IDC::ValidationError)
  yield
  raise "assertion failed: #{message}"
rescue klass
  true
end

def valid_dossier
  {
    "kind" => "idc_dossier",
    "version" => 1,
    "id" => "contract-fixture",
    "profile" => "research_dossier",
    "title" => "Contract fixture",
    "objective" => "Prove deterministic advisory validation.",
    "observed_at" => "2026-08-12T09:00:00Z",
    "sources" => [
      {"id" => "owner-note", "kind" => "manual_owner_note", "locator" => "owner supplied fixture", "revision_or_digest" => "owner-note-v1", "observed_at" => "2026-08-12T09:00:00Z", "confidence" => "high", "freshness" => "current"},
      {"id" => "old-source", "kind" => "synthetic_fixture", "locator" => "fixture", "revision_or_digest" => "fixture-v1", "observed_at" => "2026-07-01T09:00:00Z", "confidence" => "low", "freshness" => "stale"}
    ],
    "claims" => [
      {"id" => "goal", "status" => "owner_confirmed", "statement" => "The owner wants a reversible comparison.", "source_ids" => ["owner-note"], "confidence" => "high"},
      {"id" => "option", "status" => "alternative", "statement" => "Option A is available for review.", "source_ids" => ["old-source"], "confidence" => "medium"},
      {"id" => "risk", "status" => "conflict", "statement" => "The supplied material conflicts on operating cost.", "source_ids" => ["old-source"], "confidence" => "low"},
      {"id" => "assumption", "status" => "assumption", "statement" => "The comparison currency is stable.", "source_ids" => ["owner-note"], "confidence" => "low"},
      {"id" => "question", "status" => "open_question", "statement" => "The owner must choose the preferred constraint.", "source_ids" => ["owner-note"], "confidence" => "medium"},
      {"id" => "recommendation", "status" => "advisory_recommendation", "statement" => "Inspect the conflicting cost before choosing.", "source_ids" => ["old-source"], "confidence" => "low"}
    ],
    "sections" => {
      "facts" => ["goal"], "alternatives" => ["option"], "risks" => ["risk"],
      "assumptions" => ["assumption"], "open_questions" => ["question"], "recommendations" => ["recommendation"]
    },
    "promotion_proposals" => [
      {"id" => "review-goal", "statement" => "Owner may promote the reviewed goal through Dora.", "target" => "decision_log", "source_ids" => ["owner-note"], "owner_action_required" => true}
    ],
    "authority_boundary" => "This is advisory-only and cannot write Dora."
  }
end

dossier = IDC::Dossier.new(valid_dossier).validate!
first_render = dossier.render
second_render = IDC::Dossier.new(valid_dossier).validate!.render
assert("same input renders identically") { first_render == second_render }
assert("staleness is visible") { first_render.include?("[SOURCE STALE] old-source") }
assert("conflict is visible") { first_render.include?("[CONFLICT]") }
assert("open question is visible") { first_render.include?("[OPEN_QUESTION]") }
assert("owner confirmation is labelled") { first_render.include?("[OWNER_CONFIRMED | confidence: high]") }
assert("promotion remains owner-gated") { first_render.include?("OWNER ACTION REQUIRED") && first_render.include?("Promotion is manual") }

malformed = valid_dossier
malformed["claims"][0]["source_ids"] = ["old-source"]
assert_raises("external or synthetic material cannot silently become owner-confirmed") { IDC::Dossier.new(malformed).validate! }

malformed = valid_dossier
malformed["sources"][0].delete("observed_at")
assert_raises("incomplete source cannot become confirmed") { IDC::Dossier.new(malformed).validate! }

envelope = {
  "kind" => "idc_dora_read_envelope", "version" => 1, "id" => "envelope-fixture", "project_id" => "example",
  "observed_at" => "2026-08-12T09:00:00Z", "source_revision" => "revision-1", "sanitized" => true,
  "accepted_decisions" => [], "open_decisions" => [], "relevant_artifacts" => [],
  "authority_boundary" => "Manually supplied and read-only."
}
IDC::DoraEnvelope.new(envelope).validate!
unsanitized_envelope = Marshal.load(Marshal.dump(envelope))
unsanitized_envelope["sanitized"] = false
assert_raises("unsanitized Dora input is rejected") { IDC::DoraEnvelope.new(unsanitized_envelope).validate! }

dora_targets = %w[
  dora/lib/dora/decision_log.rb
  dora/lib/dora/project_memory.rb
  docs/work/idc-pilot-master.yaml
].map { |path| [path, Digest::SHA256.file(File.join(ROOT, path)).hexdigest] }.to_h
IDC::Dossier.new(valid_dossier).validate!.render
assert("IDC contract does not mutate Dora targets") do
  dora_targets.all? { |path, digest| Digest::SHA256.file(File.join(ROOT, path)).hexdigest == digest }
end

runtime_source = File.read(File.join(ROOT, "idc/lib/idc/dossier.rb")) + File.read(File.join(ROOT, "idc/lib/idc/dora_envelope.rb"))
forbidden = ["File.write", "File.delete", "system(", "Open3", "Net::HTTP", "`", "bin/dora", ".dora/"]
assert("IDC has no automatic Dora write/read or runtime execution path") { forbidden.none? { |token| runtime_source.include?(token) } }

puts "IDC dossier contract tests passed"
