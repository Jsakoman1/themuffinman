# frozen_string_literal: true

require_relative "request"

module Idc
  class Dossier
    def self.render(request:, manifest:, dossier:)
      Request.validate!(request: request, manifest: manifest, dossier: dossier)
      lines = ["# IDC Advisory Dossier: #{dossier.fetch("id")}", "", "Advisory-only. This dossier is not a Dora decision, plan, evidence record, or verified status.", ""]
      section(lines, "Request")
      lines << "- Goal: #{request.fetch("goal").strip}"; lines << "- Scope: #{request.fetch("scope").strip}"; request.fetch("questions").sort.each { |question| lines << "- Owner question: #{question.strip}" }; lines << ""
      section(lines, "Source provenance")
      manifest.fetch("sources").sort_by { |source| source.fetch("id") }.each { |source| lines << "- `#{source.fetch("id")}` [#{source.fetch("allowed_kind")}]: #{source.fetch("locator")} (revision/digest: #{source.fetch("revision_or_digest")}; observed_at: #{source.fetch("observed_at")})" }
      manifest.fetch("expected_evidence").sort_by { |entry| entry.fetch("id") }.each { |entry| lines << "- Expected evidence `#{entry.fetch("id")}`: #{entry.fetch("expected_proof")} (#{entry.fetch("disposition")})" }; lines << ""
      section(lines, "Claims")
      dossier.fetch("claims").sort_by { |claim| claim.fetch("id") }.each do |claim|
        lines << "### #{claim.fetch("id")} — #{claim.fetch("status")}"
        lines << claim.fetch("wording").strip
        lines << "Source refs: #{claim.fetch("source_refs").empty? ? "none" : claim.fetch("source_refs").sort.join(", ")}"
        lines << "Assessment: #{claim.fetch("assessment").sort.map { |key, value| "#{key}=#{value}" }.join(", ")}" if claim["assessment"]
        lines << "Missing-context basis: #{claim.fetch("missing_context_basis").sort.map { |key, value| "#{key}=#{value}" }.join(", ")}" if claim["missing_context_basis"]
        lines << ""
      end
      section(lines, "Visible uncertainty")
      uncertain = dossier.fetch("claims").select { |claim| %w[conflict missing_context open_question assumption].include?(claim.fetch("status")) }.sort_by { |claim| [claim.fetch("status"), claim.fetch("id")] }
      uncertain.empty? ? lines << "- none" : uncertain.each { |claim| lines << "- #{claim.fetch("status")}: #{claim.fetch("id")}" }; lines << ""
      section(lines, "Stop conditions")
      conditions = Array(dossier["stop_conditions"]).sort; conditions.empty? ? lines << "- none declared" : conditions.each { |condition| lines << "- #{condition}" }; lines << ""
      section(lines, "Promotion proposal")
      proposal = dossier.fetch("promotion_proposal"); lines << proposal.fetch("proposed_text").strip; lines << "Owner action: #{proposal.fetch("owner_action")}"; lines << ""; lines << "Traceability: request=#{dossier.fetch("request_ref")}; manifest=#{dossier.fetch("manifest_ref")}; dossier=#{dossier.fetch("id")}."
      lines.join("\n") + "\n"
    end

    def self.section(lines, title); lines << "## #{title}"; end
    private_class_method :section
  end
end
