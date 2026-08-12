#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require "yaml"

ROOT = File.expand_path("../..", __dir__)
SCHEMA_PATH = File.join(ROOT, "idc", "idc-v0-contract.schema.yaml")

class IdcV0ContractValidator
  AUTHORITY_FIELDS = %w[
    canonical_decision decision_write master_plan_write task_lifecycle_write evidence_write
    verified_status git_action codex_invocation shell_command network_request dora_write
  ].freeze

  def initialize(schema)
    @schema = schema
  end

  def validate!(request:, manifest:, dossier:)
    validate_profile!(request, "research_request")
    validate_request!(request)
    validate_manifest!(manifest)
    validate_dossier!(dossier, manifest)
    true
  end

  private

  def validate_profile!(value, profile_name)
    profile = @schema.fetch("profiles").fetch(profile_name)
    fail!("#{profile_name} is invalid") unless value.is_a?(Hash) && value["kind"] == profile.fetch("kind") && value["version"] == 1
    require_fields!(value, profile.fetch("required_fields"), profile_name)
    reject_authority_fields!(value, profile_name)
    value
  end

  def validate_manifest!(manifest)
    profile = @schema.fetch("profiles").fetch("source_manifest")
    validate_profile!(manifest, "source_manifest")
    source_ids = []
    Array(manifest.fetch("sources")).each do |source|
      require_hash!(source, "source")
      require_fields!(source, profile.fetch("source_required_fields"), "source")
      reject_fields!(source, profile.fetch("source_forbidden_fields"), "source")
      reject_authority_fields!(source, "source")
      fail!("source allowed_kind is invalid") unless @schema.dig("profiles", "research_request", "allowed_source_kinds").include?(source.fetch("allowed_kind"))
      fail!("source id is invalid") unless identifier?(source.fetch("id"))
      fail!("source locator is invalid") unless locator?(source.fetch("locator"))
      fail!("source revision_or_digest is invalid") unless non_empty_string?(source.fetch("revision_or_digest"))
      utc_time!(source.fetch("observed_at"), "source observed_at")
      source_ids << source.fetch("id")
    end
    fail!("source ids must be unique") unless source_ids.uniq.length == source_ids.length

    expected_ids = []
    Array(manifest.fetch("expected_evidence")).each do |entry|
      require_hash!(entry, "expected_evidence")
      require_fields!(entry, profile.fetch("expected_evidence_required_fields"), "expected_evidence")
      reject_authority_fields!(entry, "expected_evidence")
      fail!("expected_evidence id is invalid") unless identifier?(entry.fetch("id"))
      fail!("expected_evidence proof is invalid") unless non_empty_string?(entry.fetch("expected_proof"))
      fail!("expected_evidence disposition is invalid") unless profile.fetch("expected_evidence_dispositions").include?(entry.fetch("disposition"))
      expected_ids << entry.fetch("id")
    end
    fail!("expected_evidence ids must be unique") unless expected_ids.uniq.length == expected_ids.length
  end

  def validate_request!(request)
    allowed_kinds = request.fetch("allowed_source_kinds")
    configured_kinds = @schema.dig("profiles", "research_request", "allowed_source_kinds")
    fail!("request allowed_source_kinds must be a non-empty list") unless allowed_kinds.is_a?(Array) && !allowed_kinds.empty? && (allowed_kinds - configured_kinds).empty?
    fail!("request goal is invalid") unless non_empty_string?(request.fetch("goal"))
    fail!("request scope is invalid") unless non_empty_string?(request.fetch("scope"))
    fail!("request questions must be a list") unless request.fetch("questions").is_a?(Array)
  end

  def validate_dossier!(dossier, manifest)
    profile = @schema.fetch("profiles").fetch("dossier")
    validate_profile!(dossier, "dossier")
    source_ids = Array(manifest.fetch("sources")).map { |source| source.fetch("id") }
    expected_ids = Array(manifest.fetch("expected_evidence")).map { |entry| entry.fetch("id") }

    claim_ids = []
    Array(dossier.fetch("claims")).each do |claim|
      require_hash!(claim, "claim")
      require_fields!(claim, profile.fetch("claim_required_fields"), "claim")
      reject_authority_fields!(claim, "claim")
      fail!("claim id is invalid") unless identifier?(claim.fetch("id"))
      fail!("claim status is invalid") unless profile.fetch("claim_statuses").include?(claim.fetch("status"))
      fail!("claim wording is invalid") unless non_empty_string?(claim.fetch("wording"))
      validate_claim_references!(claim, source_ids)
      validate_assessment!(claim, profile.fetch("claim_assessment_fields"))
      validate_missing_context!(claim, expected_ids, source_ids) if claim.fetch("status") == "missing_context"
      fail!("open_question must not assert missing_context basis") if claim.fetch("status") == "open_question" && claim.key?("missing_context_basis")
      claim_ids << claim.fetch("id")
    end
    fail!("claim ids must be unique") unless claim_ids.uniq.length == claim_ids.length
    validate_promotion_proposal!(dossier.fetch("promotion_proposal"), dossier.fetch("id"))
  end

  def validate_claim_references!(claim, source_ids)
    refs = claim.fetch("source_refs")
    fail!("claim source_refs must be a list") unless refs.is_a?(Array) && refs.all? { |ref| identifier?(ref) }
    fail!("claim source_refs are unknown") unless (refs - source_ids).empty?
  end

  def validate_assessment!(claim, allowed_fields)
    assessment = claim["assessment"]
    return unless assessment

    require_hash!(assessment, "claim assessment")
    reject_authority_fields!(assessment, "claim assessment")
    fail!("claim assessment has an unsupported field") unless (assessment.keys - allowed_fields).empty?
    fail!("claim confidence is invalid") if assessment.key?("confidence") && !%w[high medium low].include?(assessment.fetch("confidence"))
    utc_time!(assessment.fetch("freshness"), "claim freshness") if assessment.key?("freshness")
  end

  def validate_missing_context!(claim, expected_ids, source_ids)
    basis = claim["missing_context_basis"]
    require_hash!(basis, "missing_context basis")
    reject_authority_fields!(basis, "missing_context basis")
    kind = basis["kind"]
    case kind
    when "manifest_expected_evidence"
      fail!("missing_context manifest evidence is unknown") unless expected_ids.include?(basis["expected_evidence_ref"])
    when "source_explicit_absence"
      source_ref = basis["source_ref"]
      fail!("missing_context absence source is unknown") unless source_ids.include?(source_ref) && claim.fetch("source_refs").include?(source_ref)
      fail!("missing_context absence excerpt is invalid") unless non_empty_string?(basis["source_excerpt"])
    else
      fail!("missing_context basis is invalid")
    end
  end

  def validate_promotion_proposal!(proposal, dossier_id)
    profile = @schema.fetch("profiles").fetch("promotion_proposal")
    validate_profile!(proposal, "promotion_proposal")
    fail!("promotion proposal dossier_ref is invalid") unless proposal.fetch("dossier_ref") == dossier_id
    fail!("promotion proposal text is invalid") unless non_empty_string?(proposal.fetch("proposed_text"))
    fail!("promotion proposal owner action is invalid") unless profile.fetch("owner_actions").include?(proposal.fetch("owner_action"))
  end

  def require_hash!(value, label)
    fail!("#{label} must be a mapping") unless value.is_a?(Hash)
  end

  def require_fields!(value, fields, label)
    missing = fields.reject { |field| value.key?(field) }
    fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?
  end

  def reject_fields!(value, fields, label)
    found = fields.select { |field| value.key?(field) }
    fail!("#{label} contains #{found.join(", ")}") unless found.empty?
  end

  def reject_authority_fields!(value, label)
    reject_fields!(value, AUTHORITY_FIELDS, label)
  end

  def identifier?(value)
    value.is_a?(String) && value.match?(%r{\A[a-z][a-z0-9_-]*\z})
  end

  def locator?(value)
    return false unless non_empty_string?(value)

    value.start_with?("https://", "http://") || (!value.start_with?("/") && !value.split("/").include?(".."))
  end

  def non_empty_string?(value)
    value.is_a?(String) && !value.strip.empty?
  end

  def utc_time!(value, label)
    fail!("#{label} is invalid") unless value.is_a?(String) && Time.iso8601(value).utc.iso8601 == value
  rescue ArgumentError
    fail!("#{label} is invalid")
  end

  def fail!(message)
    raise ArgumentError, message
  end
end

schema = YAML.load_file(SCHEMA_PATH)
abort "IDC contract schema kind is invalid" unless schema.slice("kind", "version", "disposition") == {"kind" => "idc_v0_contract_schema", "version" => 1, "disposition" => "advisory_only"}
abort "IDC contract schema must not require source confidence" if schema.dig("profiles", "source_manifest", "source_required_fields").include?("confidence")
abort "IDC contract schema has the wrong missing_context rule" unless schema.fetch("missing_context_rule").include?("never a basis")

validator = IdcV0ContractValidator.new(schema)
request = {
  "kind" => "idc_research_request", "version" => 1, "id" => "land-review",
  "goal" => "Compare three owner-selected property options.", "scope" => "Advisory only.",
  "questions" => ["Which documents remain required before a decision?"],
  "allowed_source_kinds" => ["owner_input", "local_document"]
}
manifest = {
  "kind" => "idc_source_manifest", "version" => 1, "id" => "land-sources",
  "sources" => [{"id" => "owner-brief", "allowed_kind" => "owner_input", "locator" => "owner-input/land.txt", "revision_or_digest" => "sha256:brief", "observed_at" => "2026-08-12T10:00:00Z"}],
  "expected_evidence" => [{"id" => "land-registry-extract", "expected_proof" => "Current official land-registry extract", "disposition" => "not_provided"}]
}
promotion = {"kind" => "idc_promotion_proposal", "version" => 1, "dossier_ref" => "land-dossier", "proposed_text" => "Owner may review this advisory dossier.", "owner_action" => "owner_review_required"}
dossier = {
  "kind" => "idc_advisory_dossier", "version" => 1, "id" => "land-dossier", "request_ref" => "land-review", "manifest_ref" => "land-sources",
  "claims" => [
    {"id" => "owner-goal", "status" => "owner_confirmed", "source_refs" => ["owner-brief"], "wording" => "The owner wants a decision-ready comparison.", "assessment" => {"confidence" => "high", "freshness" => "2026-08-12T10:00:00Z"}},
    {"id" => "registry-gap", "status" => "missing_context", "source_refs" => [], "wording" => "A current registry extract was not supplied.", "missing_context_basis" => {"kind" => "manifest_expected_evidence", "expected_evidence_ref" => "land-registry-extract"}},
    {"id" => "utility-question", "status" => "open_question", "source_refs" => [], "wording" => "Utility capacity remains unknown."}
  ],
  "promotion_proposal" => promotion
}

abort "valid IDC contract was rejected" unless validator.validate!(request: request, manifest: manifest, dossier: dossier)

invalid_cases = []
missing_identity = Marshal.load(Marshal.dump(manifest))
missing_identity.fetch("sources").first.delete("id")
invalid_cases << [request, missing_identity, dossier, "structurally incomplete source"]

source_confidence = Marshal.load(Marshal.dump(manifest))
source_confidence.fetch("sources").first["confidence"] = "high"
invalid_cases << [request, source_confidence, dossier, "source confidence"]

unfounded_missing = Marshal.load(Marshal.dump(dossier))
unfounded_missing.fetch("claims")[1].delete("missing_context_basis")
invalid_cases << [request, manifest, unfounded_missing, "unfounded missing_context"]

search_inferred_missing = Marshal.load(Marshal.dump(dossier))
search_inferred_missing.fetch("claims")[1]["missing_context_basis"] = {"kind" => "filesystem_search_not_found"}
invalid_cases << [request, manifest, search_inferred_missing, "filesystem inferred missing_context"]

authority_claim = Marshal.load(Marshal.dump(dossier))
authority_claim.fetch("claims").first["verified_status"] = true
invalid_cases << [request, manifest, authority_claim, "authority-bearing claim"]

invalid_cases.each do |candidate_request, candidate_manifest, candidate_dossier, label|
  begin
    validator.validate!(request: candidate_request, manifest: candidate_manifest, dossier: candidate_dossier)
    abort "#{label} was accepted"
  rescue ArgumentError
    # Expected: contract input must fail closed without guessing or promotion.
  end
end

source_absence = Marshal.load(Marshal.dump(dossier))
source_absence.fetch("claims")[1]["source_refs"] = ["owner-brief"]
source_absence.fetch("claims")[1]["missing_context_basis"] = {"kind" => "source_explicit_absence", "source_ref" => "owner-brief", "source_excerpt" => "No current registry extract was supplied."}
abort "cited explicit absence was rejected" unless validator.validate!(request: request, manifest: manifest, dossier: source_absence)

source = File.read(SCHEMA_PATH)
{
  "File.write" => /File\.write/,
  "Open3" => /\bOpen3\b/,
  "system invocation" => /\bsystem\s*\(/,
  "Net::HTTP" => /Net::HTTP/,
  "URI.open" => /URI\.open/,
  "Codex invocation" => /codex_invocation:\s*true/,
  "Bridge execution" => /bridge_execution:\s*true/,
  "work start" => /work-start/,
  "work verify" => /work-verify/
}.each do |forbidden, pattern|
  abort "schema exposes forbidden capability #{forbidden}" if source.match?(pattern)
end

puts "IDC v0 contract test passed (provenance/assessment separation, evidenced gaps, and no authority)."
