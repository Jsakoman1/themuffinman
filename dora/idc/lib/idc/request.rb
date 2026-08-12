# frozen_string_literal: true

require "time"
require "yaml"

module Idc
  class Request
    SOURCE_KINDS = %w[owner_input local_document dora_read_envelope external_research_receipt].freeze
    CLAIM_STATUSES = %w[owner_confirmed source_fact external_research assumption open_question missing_context conflict alternative scenario advisory_recommendation].freeze
    AUTHORITY_FIELDS = %w[canonical_decision decision_write master_plan_write task_lifecycle_write evidence_write verified_status git_action codex_invocation shell_command network_request dora_write].freeze

    def self.load!(path, label:)
      fail!("#{label} path is invalid") unless path.is_a?(String) && !path.strip.empty? && File.file?(path)
      value = YAML.load_file(path)
      fail!("#{label} must be a YAML mapping") unless value.is_a?(Hash)
      value
    rescue Psych::Exception
      fail!("#{label} YAML is invalid")
    end

    def self.validate!(request:, manifest:, dossier:)
      validate_request!(request)
      source_ids, expected_ids = validate_manifest!(manifest, allowed_kinds: request.fetch("allowed_source_kinds"))
      validate_dossier!(dossier, source_ids: source_ids, expected_ids: expected_ids)
      true
    end

    def self.validate_request!(request)
      validate_profile!(request, "idc_research_request", %w[kind version id goal scope questions allowed_source_kinds], "request")
      fail!("request id is invalid") unless identifier?(request.fetch("id"))
      fail!("request goal is invalid") unless non_empty_string?(request.fetch("goal"))
      fail!("request scope is invalid") unless non_empty_string?(request.fetch("scope"))
      fail!("request questions must be a list") unless request.fetch("questions").is_a?(Array) && request.fetch("questions").all? { |item| non_empty_string?(item) }
      kinds = request.fetch("allowed_source_kinds")
      fail!("request allowed_source_kinds must be a non-empty unique list") unless kinds.is_a?(Array) && !kinds.empty? && kinds.uniq.length == kinds.length && (kinds - SOURCE_KINDS).empty?
    end
    private_class_method :validate_request!

    def self.validate_manifest!(manifest, allowed_kinds:)
      validate_profile!(manifest, "idc_source_manifest", %w[kind version id sources expected_evidence], "manifest")
      fail!("manifest id is invalid") unless identifier?(manifest.fetch("id"))
      source_ids = Array(manifest.fetch("sources")).map do |source|
        mapping!(source, "source"); require_fields!(source, %w[id allowed_kind locator revision_or_digest observed_at], "source"); reject_fields!(source, %w[confidence freshness], "source"); reject_authority!(source, "source")
        fail!("source id is invalid") unless identifier?(source.fetch("id"))
        fail!("source allowed_kind is not allowed by the request") unless allowed_kinds.include?(source.fetch("allowed_kind"))
        fail!("source locator is invalid") unless locator?(source.fetch("locator")); fail!("source revision_or_digest is invalid") unless non_empty_string?(source.fetch("revision_or_digest")); utc_time!(source.fetch("observed_at"), "source observed_at")
        source.fetch("id")
      end
      fail!("source ids must be unique") unless source_ids.uniq.length == source_ids.length
      expected_ids = Array(manifest.fetch("expected_evidence")).map do |entry|
        mapping!(entry, "expected_evidence"); require_fields!(entry, %w[id expected_proof disposition], "expected_evidence"); reject_authority!(entry, "expected_evidence")
        fail!("expected_evidence id is invalid") unless identifier?(entry.fetch("id")); fail!("expected_evidence proof is invalid") unless non_empty_string?(entry.fetch("expected_proof")); fail!("expected_evidence disposition is invalid") unless entry.fetch("disposition") == "not_provided"
        entry.fetch("id")
      end
      fail!("expected_evidence ids must be unique") unless expected_ids.uniq.length == expected_ids.length
      [source_ids, expected_ids]
    end
    private_class_method :validate_manifest!

    def self.validate_dossier!(dossier, source_ids:, expected_ids:)
      validate_profile!(dossier, "idc_advisory_dossier", %w[kind version id request_ref manifest_ref claims promotion_proposal], "dossier")
      fail!("dossier id is invalid") unless identifier?(dossier.fetch("id")); fail!("dossier request_ref is invalid") unless identifier?(dossier.fetch("request_ref")); fail!("dossier manifest_ref is invalid") unless identifier?(dossier.fetch("manifest_ref"))
      claim_ids = Array(dossier.fetch("claims")).map { |claim| validate_claim!(claim, source_ids: source_ids, expected_ids: expected_ids); claim.fetch("id") }
      fail!("claim ids must be unique") unless claim_ids.uniq.length == claim_ids.length
      conditions = dossier["stop_conditions"]
      fail!("stop_conditions must be a list") if conditions && (!conditions.is_a?(Array) || !conditions.all? { |condition| non_empty_string?(condition) })
      proposal = dossier.fetch("promotion_proposal")
      validate_profile!(proposal, "idc_promotion_proposal", %w[kind version dossier_ref proposed_text owner_action], "promotion proposal")
      fail!("promotion proposal is invalid") unless proposal.fetch("dossier_ref") == dossier.fetch("id") && non_empty_string?(proposal.fetch("proposed_text")) && proposal.fetch("owner_action") == "owner_review_required"
    end
    private_class_method :validate_dossier!

    def self.validate_claim!(claim, source_ids:, expected_ids:)
      mapping!(claim, "claim"); require_fields!(claim, %w[id status source_refs wording], "claim"); reject_authority!(claim, "claim")
      fail!("claim id is invalid") unless identifier?(claim.fetch("id")); fail!("claim status is invalid") unless CLAIM_STATUSES.include?(claim.fetch("status")); fail!("claim wording is invalid") unless non_empty_string?(claim.fetch("wording"))
      refs = claim.fetch("source_refs")
      fail!("claim source_refs must be a unique list") unless refs.is_a?(Array) && refs.uniq.length == refs.length && refs.all? { |ref| source_ids.include?(ref) }
      source_required = %w[owner_confirmed source_fact external_research assumption conflict alternative scenario advisory_recommendation]
      fail!("claim status requires source_refs") if source_required.include?(claim.fetch("status")) && refs.empty?
      assessment = claim["assessment"]
      if assessment
        mapping!(assessment, "claim assessment"); reject_authority!(assessment, "claim assessment"); fail!("claim assessment has unsupported fields") unless (assessment.keys - %w[confidence freshness]).empty?
        fail!("claim confidence is invalid") if assessment.key?("confidence") && !%w[high medium low].include?(assessment.fetch("confidence")); utc_time!(assessment.fetch("freshness"), "claim freshness") if assessment.key?("freshness")
      end
      if claim.fetch("status") == "missing_context"
        basis = claim["missing_context_basis"]; mapping!(basis, "missing_context basis"); reject_authority!(basis, "missing_context basis")
        case basis.fetch("kind", nil)
        when "manifest_expected_evidence" then fail!("missing_context expected evidence is unknown") unless expected_ids.include?(basis["expected_evidence_ref"])
        when "source_explicit_absence" then fail!("missing_context source is not cited") unless source_ids.include?(basis["source_ref"]) && refs.include?(basis["source_ref"]); fail!("missing_context excerpt is invalid") unless non_empty_string?(basis["source_excerpt"])
        else fail!("missing_context cannot be inferred")
        end
      elsif claim.fetch("status") == "open_question" && claim.key?("missing_context_basis")
        fail!("open_question cannot declare missing_context_basis")
      end
    end
    private_class_method :validate_claim!

    def self.validate_profile!(value, kind, fields, label)
      mapping!(value, label); fail!("#{label} kind is invalid") unless value.fetch("kind", nil) == kind && value.fetch("version", nil) == 1; require_fields!(value, fields, label); reject_authority!(value, label)
    end
    private_class_method :validate_profile!

    def self.mapping!(value, label); fail!("#{label} must be a mapping") unless value.is_a?(Hash); end
    private_class_method :mapping!
    def self.require_fields!(value, fields, label); missing = fields.reject { |field| value.key?(field) }; fail!("#{label} is missing #{missing.join(", ")}") unless missing.empty?; end
    private_class_method :require_fields!
    def self.reject_fields!(value, fields, label); found = fields.select { |field| value.key?(field) }; fail!("#{label} contains #{found.join(", ")}") unless found.empty?; end
    private_class_method :reject_fields!
    def self.reject_authority!(value, label); reject_fields!(value, AUTHORITY_FIELDS, label); end
    private_class_method :reject_authority!
    def self.identifier?(value); value.is_a?(String) && value.match?(%r{\A[a-z][a-z0-9_-]*\z}); end
    private_class_method :identifier?
    def self.locator?(value); non_empty_string?(value) && (value.start_with?("https://", "http://") || (!value.start_with?("/") && !value.split("/").include?(".."))); end
    private_class_method :locator?
    def self.non_empty_string?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :non_empty_string?
    def self.utc_time!(value, label); fail!("#{label} is invalid") unless value.is_a?(String) && Time.iso8601(value).utc.iso8601 == value; rescue ArgumentError; fail!("#{label} is invalid"); end
    private_class_method :utc_time!
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
