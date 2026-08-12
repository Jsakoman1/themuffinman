# frozen_string_literal: true

require "time"
require "yaml"
require_relative "dora_envelope"

module IDC
  # Validates and renders local, advisory-only material. This class has no Dora,
  # Git, shell, network, database, or Codex integration.
  class Dossier
    PROFILES = {
      "research_dossier" => %w[facts alternatives risks assumptions open_questions recommendations],
      "greenfield_product_delivery_baseline" => %w[
        problem_and_outcome users_and_journeys v1_scope domain_data_state
        architecture_options quality_operations risks_and_questions first_vertical_slice
      ]
    }.freeze
    SOURCE_KINDS = %w[manual_owner_note owner_record local_file dora_read_envelope external_url synthetic_fixture].freeze
    CONFIDENCES = %w[low medium high].freeze
    FRESHNESS = %w[current stale unknown conflicting].freeze
    STATUSES = %w[
      owner_confirmed source_fact assumption external_research open_question alternative
      conflict scenario missing_context advisory_recommendation
    ].freeze
    OWNER_EVIDENCE_KINDS = %w[manual_owner_note owner_record].freeze
    REQUIRED_KEYS = %w[
      kind version id profile title objective observed_at sources claims sections
      promotion_proposals authority_boundary
    ].freeze

    attr_reader :data

    def self.load!(path)
      new(load_yaml!(path)).tap(&:validate!)
    end

    def self.load_yaml!(path)
      YAML.safe_load(File.read(path), permitted_classes: [], aliases: false)
    rescue Errno::ENOENT, Psych::Exception => error
      raise ValidationError, "cannot load dossier: #{error.message}"
    end

    def self.render(dossier)
      dossier.render
    end

    def initialize(data)
      @data = data
    end

    def validate!
      assert_hash!(@data, "dossier")
      assert_exact_keys!(@data, REQUIRED_KEYS, "dossier")
      assert_equal!(@data["kind"], "idc_dossier", "dossier kind")
      assert_equal!(@data["version"], 1, "dossier version")
      assert_identifier!(@data["id"], "dossier id")
      raise ValidationError, "dossier profile is invalid" unless PROFILES.key?(@data["profile"])
      assert_text!(@data["title"], "dossier title")
      assert_text!(@data["objective"], "dossier objective")
      assert_timestamp!(@data["observed_at"], "dossier observed_at")
      validate_sources!
      validate_claims!
      validate_sections!
      validate_promotion_proposals!
      assert_text!(@data["authority_boundary"], "authority_boundary")
      @data = canonicalize(@data)
      self
    end

    def render
      source_by_id = @data.fetch("sources").to_h { |source| [source.fetch("id"), source] }
      claim_by_id = @data.fetch("claims").to_h { |claim| [claim.fetch("id"), claim] }
      lines = ["# #{@data.fetch("title")}", "", "> Advisory-only IDC output. It is not a Dora decision, plan, evidence record, or verification claim.", "", "## Objective", "", @data.fetch("objective"), "", "## Authority boundary", "", @data.fetch("authority_boundary"), "", "## Source register", "", "| ID | Kind | Revision/digest | Observed | Confidence | Freshness |", "| --- | --- | --- | --- | --- | --- |"]
      @data.fetch("sources").each do |source|
        lines << "| #{source.fetch("id")} | #{source.fetch("kind")} | #{source.fetch("revision_or_digest")} | #{source.fetch("observed_at")} | #{source.fetch("confidence")} | #{source.fetch("freshness")} |"
      end
      lines.concat(context_alert_lines(source_by_id, claim_by_id))
      @data.fetch("sections").each do |section, claim_ids|
        lines.concat(["", "## #{humanize(section)}", ""])
        claim_ids.each do |claim_id|
          claim = claim_by_id.fetch(claim_id)
          lines << "- [#{claim.fetch("status").upcase} | confidence: #{claim.fetch("confidence")}] #{claim.fetch("statement")} (sources: #{claim.fetch("source_ids").join(", ")})"
        end
      end
      lines.concat(["", "## Promotion proposals", ""])
      @data.fetch("promotion_proposals").each do |proposal|
        lines << "- [OWNER ACTION REQUIRED → #{proposal.fetch("target").upcase}] #{proposal.fetch("statement")} (sources: #{proposal.fetch("source_ids").join(", ")})"
      end
      lines << ""
      lines << "Promotion is manual: only an explicit owner confirmation through Dora's existing authorized workflow can create canonical Dora state."
      lines.join("\n") + "\n"
    end

    private

    def validate_sources!
      assert_array!(@data["sources"], "sources")
      raise ValidationError, "sources must not be empty" if @data["sources"].empty?
      ids = @data["sources"].map do |source|
        assert_hash!(source, "source")
        assert_exact_keys!(source, %w[id kind locator revision_or_digest observed_at confidence freshness], "source")
        assert_identifier!(source["id"], "source id")
        raise ValidationError, "source kind is invalid" unless SOURCE_KINDS.include?(source["kind"])
        assert_text!(source["locator"], "source locator")
        assert_text!(source["revision_or_digest"], "source revision_or_digest")
        assert_timestamp!(source["observed_at"], "source observed_at")
        assert_enum!(source["confidence"], CONFIDENCES, "source confidence")
        assert_enum!(source["freshness"], FRESHNESS, "source freshness")
        source["id"]
      end
      raise ValidationError, "source ids are duplicated" unless ids.uniq.length == ids.length
    end

    def validate_claims!
      assert_array!(@data["claims"], "claims")
      raise ValidationError, "claims must not be empty" if @data["claims"].empty?
      source_by_id = @data["sources"].to_h { |source| [source["id"], source] }
      ids = @data["claims"].map do |claim|
        assert_hash!(claim, "claim")
        assert_exact_keys!(claim, %w[id status statement source_ids confidence], "claim")
        assert_identifier!(claim["id"], "claim id")
        assert_enum!(claim["status"], STATUSES, "claim status")
        assert_text!(claim["statement"], "claim statement")
        assert_array!(claim["source_ids"], "claim source_ids")
        raise ValidationError, "claim source_ids must not be empty" if claim["source_ids"].empty?
        raise ValidationError, "claim source_ids are duplicated" unless claim["source_ids"].uniq.length == claim["source_ids"].length
        claim["source_ids"].each { |source_id| raise ValidationError, "claim references an unknown source" unless source_by_id.key?(source_id) }
        assert_enum!(claim["confidence"], CONFIDENCES, "claim confidence")
        if claim["status"] == "owner_confirmed" && !claim["source_ids"].any? { |source_id| OWNER_EVIDENCE_KINDS.include?(source_by_id.fetch(source_id).fetch("kind")) }
          raise ValidationError, "owner_confirmed claim requires an owner-record source"
        end
        claim["id"]
      end
      raise ValidationError, "claim ids are duplicated" unless ids.uniq.length == ids.length
    end

    def validate_sections!
      assert_hash!(@data["sections"], "sections")
      expected = PROFILES.fetch(@data.fetch("profile"))
      assert_exact_keys!(@data["sections"], expected, "profile sections")
      claim_ids = @data["claims"].map { |claim| claim["id"] }.to_h { |id| [id, true] }
      referenced = []
      @data["sections"].each do |section, ids|
        assert_array!(ids, "#{section} claims")
        raise ValidationError, "#{section} claims must not be empty" if ids.empty?
        raise ValidationError, "#{section} claim references are duplicated" unless ids.uniq.length == ids.length
        ids.each { |id| raise ValidationError, "#{section} references an unknown claim" unless claim_ids.key?(id) }
        referenced.concat(ids)
      end
      raise ValidationError, "every claim must be placed in one profile section" unless referenced.sort == claim_ids.keys.sort
    end

    def validate_promotion_proposals!
      assert_array!(@data["promotion_proposals"], "promotion_proposals")
      source_ids = @data["sources"].map { |source| source["id"] }.to_h { |id| [id, true] }
      @data["promotion_proposals"].each do |proposal|
        assert_hash!(proposal, "promotion proposal")
        assert_exact_keys!(proposal, %w[id statement target source_ids owner_action_required], "promotion proposal")
        assert_identifier!(proposal["id"], "promotion proposal id")
        assert_text!(proposal["statement"], "promotion proposal statement")
        assert_enum!(proposal["target"], %w[product_brief domain_library decision_log master_plan], "promotion proposal target")
        assert_array!(proposal["source_ids"], "promotion proposal source_ids")
        raise ValidationError, "promotion proposal source_ids must not be empty" if proposal["source_ids"].empty?
        proposal["source_ids"].each { |id| raise ValidationError, "promotion proposal references an unknown source" unless source_ids.key?(id) }
        raise ValidationError, "promotion proposal must require owner action" unless proposal["owner_action_required"] == true
      end
    end

    def context_alert_lines(source_by_id, claim_by_id)
      alerts = @data.fetch("sources").map do |source|
        "- [SOURCE #{source.fetch("freshness").upcase}] #{source.fetch("id")} is #{source.fetch("freshness")}." unless source.fetch("freshness") == "current"
      end.compact
      alerts.concat(@data.fetch("claims").map do |claim|
        "- [#{claim.fetch("status").upcase}] #{claim.fetch("statement")} (sources: #{claim.fetch("source_ids").join(", ")})" if %w[conflict missing_context open_question].include?(claim.fetch("status"))
      end.compact)
      ["", "## Context alerts", "", *(alerts.empty? ? ["- No stale, conflicting, missing, or open-question claim was supplied."] : alerts.sort)]
    end

    def canonicalize(data)
      normalized = Marshal.load(Marshal.dump(data))
      normalized["sources"] = normalized.fetch("sources").sort_by { |source| source.fetch("id") }
      normalized["claims"] = normalized.fetch("claims").sort_by { |claim| claim.fetch("id") }
      normalized["sections"] = PROFILES.fetch(normalized.fetch("profile")).to_h do |section|
        [section, normalized.fetch("sections").fetch(section).sort]
      end
      normalized["promotion_proposals"] = normalized.fetch("promotion_proposals").sort_by { |proposal| proposal.fetch("id") }
      deep_freeze(normalized)
    end

    def humanize(value)
      value.tr("_", " ").split.map(&:capitalize).join(" ")
    end

    def assert_hash!(value, label)
      raise ValidationError, "#{label} must be a mapping" unless value.is_a?(Hash)
    end

    def assert_array!(value, label)
      raise ValidationError, "#{label} must be a list" unless value.is_a?(Array)
    end

    def assert_exact_keys!(value, expected, label)
      raise ValidationError, "#{label} keys are invalid" unless value.keys.sort == expected.sort
    end

    def assert_equal!(actual, expected, label)
      raise ValidationError, "#{label} is invalid" unless actual == expected
    end

    def assert_identifier!(value, label)
      raise ValidationError, "#{label} is invalid" unless value.is_a?(String) && value.match?(/\A[a-z0-9][a-z0-9-]*\z/)
    end

    def assert_text!(value, label)
      raise ValidationError, "#{label} is missing" unless value.is_a?(String) && !value.strip.empty?
    end

    def assert_timestamp!(value, label)
      Time.iso8601(value)
    rescue ArgumentError, TypeError
      raise ValidationError, "#{label} must be ISO-8601"
    end

    def assert_enum!(value, permitted, label)
      raise ValidationError, "#{label} is invalid" unless permitted.include?(value)
    end

    def deep_freeze(value)
      case value
      when Hash then value.each { |key, item| deep_freeze(key); deep_freeze(item) }.freeze
      when Array then value.each { |item| deep_freeze(item) }.freeze
      else value.freeze
      end
    end
  end
end
