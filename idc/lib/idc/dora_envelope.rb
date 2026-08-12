# frozen_string_literal: true

require "time"
require "yaml"

module IDC
  class ValidationError < StandardError; end

  # A manually supplied, sanitized description of selected Dora material.
  # It deliberately has no path back to Dora and never resolves the references it contains.
  class DoraEnvelope
    REQUIRED_KEYS = %w[
      kind version id project_id observed_at source_revision sanitized
      accepted_decisions open_decisions relevant_artifacts authority_boundary
    ].freeze

    attr_reader :data

    def self.load!(path)
      new(load_yaml!(path)).tap(&:validate!)
    end

    def self.load_yaml!(path)
      YAML.safe_load(File.read(path), permitted_classes: [], aliases: false)
    rescue Errno::ENOENT, Psych::Exception => error
      raise ValidationError, "cannot load Dora envelope: #{error.message}"
    end

    def initialize(data)
      @data = data
    end

    def validate!
      assert_hash!(@data, "envelope")
      assert_exact_keys!(@data, REQUIRED_KEYS, "envelope")
      assert_equal!(@data["kind"], "idc_dora_read_envelope", "envelope kind")
      assert_equal!(@data["version"], 1, "envelope version")
      assert_identifier!(@data["id"], "envelope id")
      assert_identifier!(@data["project_id"], "envelope project_id")
      assert_timestamp!(@data["observed_at"], "envelope observed_at")
      assert_text!(@data["source_revision"], "envelope source_revision")
      raise ValidationError, "envelope must be explicitly sanitized" unless @data["sanitized"] == true
      assert_array!(@data["accepted_decisions"], "accepted_decisions")
      assert_array!(@data["open_decisions"], "open_decisions")
      assert_array!(@data["relevant_artifacts"], "relevant_artifacts")
      assert_text!(@data["authority_boundary"], "authority_boundary")
      @data["accepted_decisions"].each { |entry| validate_decision!(entry, "accepted decision") }
      @data["open_decisions"].each { |entry| validate_decision!(entry, "open decision") }
      @data["relevant_artifacts"].each { |entry| validate_artifact!(entry) }
      @data = deep_freeze(@data)
      self
    end

    private

    def validate_decision!(entry, label)
      assert_hash!(entry, label)
      assert_exact_keys!(entry, %w[id statement reference], label)
      assert_identifier!(entry["id"], "#{label} id")
      assert_text!(entry["statement"], "#{label} statement")
      assert_text!(entry["reference"], "#{label} reference")
    end

    def validate_artifact!(entry)
      assert_hash!(entry, "relevant artifact")
      assert_exact_keys!(entry, %w[id reference revision_or_digest role], "relevant artifact")
      assert_identifier!(entry["id"], "relevant artifact id")
      assert_text!(entry["reference"], "relevant artifact reference")
      assert_text!(entry["revision_or_digest"], "relevant artifact revision_or_digest")
      assert_text!(entry["role"], "relevant artifact role")
    end

    def assert_hash!(value, label)
      raise ValidationError, "#{label} must be a mapping" unless value.is_a?(Hash)
    end

    def assert_array!(value, label)
      raise ValidationError, "#{label} must be a list" unless value.is_a?(Array)
    end

    def assert_exact_keys!(value, expected, label)
      actual = value.keys.sort
      raise ValidationError, "#{label} keys are invalid" unless actual == expected.sort
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

    def deep_freeze(value)
      case value
      when Hash then value.each { |key, item| deep_freeze(key); deep_freeze(item) }.freeze
      when Array then value.each { |item| deep_freeze(item) }.freeze
      else value.freeze
      end
    end
  end
end
