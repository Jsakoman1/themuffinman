# frozen_string_literal: true

module Dora
  # Pure, transient classification for a structured owner request. It is deliberately
  # not a natural-language classifier: Codex/owner must supply a bounded request before
  # any local IDC render can be considered.
  class IdcTriage
    IDENTIFIER = /\A[a-z][a-z0-9-]{0,63}\z/.freeze
    REQUEST_SHAPES = %w[bounded_delivery wide_research greenfield_discovery].freeze
    PROFILES = %w[none research_dossier greenfield_product_delivery_baseline].freeze
    AUTHORIZATIONS = %w[not_granted authorize_local_idc_render].freeze
    SOURCE_SCOPE = "explicit_owner_selected_only"
    AUTHORIZATION_SCOPE = "current_request_only"
    AUTHORITY_BOUNDARY = "This transient advisory triage cannot select or read sources, render a dossier, persist state, create or amend Dora decisions/plans/evidence/verified status, invoke a process, shell, Git, network, Bridge, or Codex, or promote a conclusion."

    def self.evaluate!(request:)
      normalized = normalize!(request)
      outcome = if normalized.fetch("request_shape") == "bounded_delivery"
                  "NO_IDC_NEEDED"
                elsif normalized.fetch("owner_authorization") == "authorize_local_idc_render"
                  "IDC_OWNER_AUTHORIZED_LOCAL_RENDER"
                else
                  "IDC_OWNER_CONFIRMATION_REQUIRED"
                end
      readback(outcome: outcome, profile: normalized.fetch("profile"))
    end

    def self.normalize!(request)
      fail!("IDC triage request is invalid") unless request.is_a?(Hash) && request.keys.sort == %w[authorization_scope id kind owner_authorization profile request_shape source_scope version]
      fail!("IDC triage request kind is invalid") unless request.fetch("kind") == "dora_idc_triage_request" && request.fetch("version") == 1
      fail!("IDC triage request id is invalid") unless request.fetch("id").is_a?(String) && request.fetch("id").match?(IDENTIFIER)
      shape = request.fetch("request_shape")
      profile = request.fetch("profile")
      authorization = request.fetch("owner_authorization")
      fail!("IDC triage request shape is invalid") unless REQUEST_SHAPES.include?(shape)
      fail!("IDC triage profile is invalid") unless PROFILES.include?(profile)
      fail!("IDC triage source scope is invalid") unless request.fetch("source_scope") == SOURCE_SCOPE
      fail!("IDC triage authorization scope is invalid") unless request.fetch("authorization_scope") == AUTHORIZATION_SCOPE
      fail!("IDC triage owner authorization is invalid") unless AUTHORIZATIONS.include?(authorization)
      fail!("bounded delivery cannot authorize IDC rendering") unless shape != "bounded_delivery" || (profile == "none" && authorization == "not_granted")
      fail!("wide research requires research_dossier") unless shape != "wide_research" || profile == "research_dossier"
      fail!("greenfield discovery requires greenfield_product_delivery_baseline") unless shape != "greenfield_discovery" || profile == "greenfield_product_delivery_baseline"

      request.slice("id", "request_shape", "profile", "source_scope", "authorization_scope", "owner_authorization").freeze
    end

    def self.readback(outcome:, profile:)
      next_action, confirmation_required = case outcome
                                           when "NO_IDC_NEEDED"
                                             ["Continue with the existing bounded Dora delivery workflow.", false]
                                           when "IDC_OWNER_CONFIRMATION_REQUIRED"
                                             ["Ask the owner whether to authorize one local IDC render for this request.", true]
                                           when "IDC_OWNER_AUTHORIZED_LOCAL_RENDER"
                                             ["The local owner/Codex may invoke the fixed Dora IDC render wrapper with explicit inputs.", false]
                                           else
                                             fail!("IDC triage outcome is invalid")
                                           end
      {"kind" => "dora_idc_triage_readback", "version" => 1, "read_only" => true, "disposition" => "advisory", "outcome" => outcome, "profile" => profile, "next_action" => next_action, "owner_confirmation_required" => confirmation_required, "authority_boundary" => AUTHORITY_BOUNDARY}.freeze
    end
    private_class_method :readback

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
