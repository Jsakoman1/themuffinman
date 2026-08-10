# frozen_string_literal: true

require "yaml"
require_relative "change_impact"

module Dora
  class ChangeCheck
    def self.check!(config_path, paths)
      impact = ChangeImpact.assess!(config_path, paths)
      {
        "kind" => "dora_change_check", "version" => 1,
        "changed_paths" => impact.fetch("changed_paths"), "classifications" => impact.fetch("classifications"),
        "validations" => impact.fetch("validations"), "documentation" => impact.fetch("documentation"),
        "runtime_evidence" => impact.fetch("runtime_evidence"), "decisions" => impact.fetch("decisions"), "companion_findings" => impact.fetch("companion_findings"), "unmatched_paths" => impact.fetch("unmatched_paths")
      }.freeze
    rescue Psych::Exception => error
      fail!("change impact YAML is invalid: #{error.message}")
    end

    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
