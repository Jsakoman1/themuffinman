# frozen_string_literal: true

require "yaml"

module Dora
  class ChangeCheck
    def self.check!(config_path, paths)
      config = YAML.load_file(config_path)
      fail!("change impact config kind is invalid") unless config["kind"] == "dora_change_impact" && config["version"].to_i == 1
      rules = Array(config["rules"])
      fail!("change impact config has no rules") if rules.empty?
      rules.each { |rule| validate_rule!(rule) }
      fail!("changed paths must be a non-empty list") unless paths.is_a?(Array) && !paths.empty? && paths.all? { |path| safe_relative_path?(path) }
      matched = rules.select { |rule| rule_matches?(rule, paths) }
      {
        "kind" => "dora_change_check", "version" => 1,
        "changed_paths" => paths.uniq.sort,
        "classifications" => matched.map { |rule| rule.fetch("id") },
        "validations" => matched.flat_map { |rule| rule.fetch("validations") }.uniq,
        "documentation" => matched.flat_map { |rule| rule.fetch("documentation") }.uniq,
        "runtime_evidence" => matched.flat_map { |rule| rule.fetch("runtime_evidence") }.uniq,
        "decisions" => matched.flat_map { |rule| rule.fetch("decisions") }.uniq,
        "unmatched_paths" => paths.reject { |path| matched.any? { |rule| Array(rule["path_prefixes"]).any? { |prefix| path.start_with?(prefix) } } }.uniq.sort
      }.freeze
    rescue Psych::Exception => error
      fail!("change impact YAML is invalid: #{error.message}")
    end

    def self.rule_matches?(rule, paths)
      rule.is_a?(Hash) && Array(rule["path_prefixes"]).any? { |prefix| safe_relative_path?(prefix) && paths.any? { |path| path.start_with?(prefix) } }
    end
    private_class_method :rule_matches?

    def self.validate_rule!(rule)
      %w[id path_prefixes validations documentation runtime_evidence decisions].each do |field|
        value = rule[field]
        fail!("change impact rule is missing #{field}") unless field == "id" ? value.is_a?(String) && !value.empty? : value.is_a?(Array)
      end
    end
    private_class_method :validate_rule!

    def self.safe_relative_path?(path); path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?(".."); end
    private_class_method :safe_relative_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
