# frozen_string_literal: true

require "time"

module Dora
  class ImplementationQualityProfile
    OBLIGATION_CLASSES = %w[permission schema api ui test documentation runtime].freeze

    def self.project!(profile:)
      fail!("implementation quality profile is invalid") unless profile.is_a?(Hash) && profile.keys.sort == %w[exclusions id kind obligations source_references version] && profile["kind"] == "dora_implementation_quality_profile" && profile["version"].to_i == 1
      fail!("implementation quality profile id is invalid") unless identifier?(profile["id"])
      sources = references!(profile.fetch("source_references"), "source_references")
      obligations = classes!(profile.fetch("obligations"), "obligations")
      exclusions = classes!(profile.fetch("exclusions"), "exclusions", allow_empty: true)
      fail!("implementation quality profile overlaps obligations and exclusions") unless (obligations & exclusions).empty?
      {"kind" => "dora_implementation_quality_profile_projection", "version" => 1, "observed_at" => Time.now.utc.iso8601, "source_references" => sources, "read_only" => true, "disposition" => "advisory", "id" => profile.fetch("id"), "obligations" => obligations, "exclusions" => exclusions, "completion_boundary" => "This profile declares opt-in existing obligation classes only; it cannot select or change a task contract, execute a test, record evidence, approve runtime, security, or release readiness, mutate work status or decisions, invoke GitHub, mutate a consumer project, or start a runner or remote agent."}.freeze
    end

    def self.classes!(items, label, allow_empty: false)
      fail!("implementation quality profile #{label} must be a list") unless items.is_a?(Array) && (allow_empty || !items.empty?) && items.all? { |item| OBLIGATION_CLASSES.include?(item) }
      fail!("implementation quality profile #{label} must be unique") unless items.uniq.length == items.length
      items.sort
    end
    private_class_method :classes!

    def self.references!(references, label)
      valid = references.is_a?(Array) && !references.empty? && references.all? { |reference| reference.is_a?(String) && !reference.empty? && !reference.start_with?("/") && !reference.split("#", 2).first.split("/").include?("..") }
      fail!("implementation quality profile #{label} must be a non-empty list") unless valid
      references.uniq.sort
    end
    private_class_method :references!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
